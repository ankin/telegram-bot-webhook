package service

import api.WebhookApi
import cats.effect.IO
import config.Webhook
import io.circe.Json
import model.{Reminder, ReminderNotFound, SendMessage}
import org.http4s.circe._
import org.http4s.dsl.io._
import org.http4s.implicits._
import org.http4s.{Request, Response, Status, Uri}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import repository.ReminderRepository

import java.time.LocalDate
import java.util.UUID
import java.util.concurrent.atomic.AtomicLong
import scala.collection.mutable

class WebhookReminderSpec extends AnyWordSpec with Matchers {

  private val token = UUID.randomUUID().toString
  private val db = mutable.HashMap[Long, Reminder]()
  private val reminderRepository: ReminderRepository = new ReminderRepository(null) {
    val ids = new AtomicLong(0L)

    override def create(reminder: Reminder): IO[Long] = {
      for {
        id <- IO(ids.incrementAndGet())
        _ <- IO(db.put(id, reminder))
      } yield id
    }

    override def delete(chatId: Int, userId: Int, remindAt: LocalDate): IO[Either[model.ReminderNotFound.type, Int]] = {
      db.find {
        case (_, reminder) => reminder.chatId == chatId && reminder.createdBy == userId && reminder.remindAt == remindAt
      } match {
        case Some((key, _)) => IO(db.remove(key)) *> IO.pure(Right(1))
        case None => IO.pure(Left(ReminderNotFound))
      }
    }

    override def getReminders(chatId: Int, userId: Int): fs2.Stream[IO, Reminder] = {
      val iter = IO {
        db.filter {
          case (_, reminder) => reminder.chatId == chatId && reminder.createdBy == userId
        }.values.toSeq
      }

      fs2.Stream.evalSeq(iter)
    }
  }

  private val reminderService = new ReminderService(reminderRepository)
  private val service = new WebhookApi(Webhook(token, "1111111"), reminderService).routes

  "WebhookReminderSpec" should {

    "handle /nagadai+, nagadai? and /nagadai-" in {
      val createCmdJson = jsonWithCommand("/nagadai+ 13.02.2021 Valentines Day")
      val createCmdResponse = serve(Request[IO](POST, Uri.unsafeFromString(s"/webhook/$token")).withEntity(createCmdJson))
      createCmdResponse.status shouldBe Status.Ok
      createCmdResponse.as[Json].unsafeRunSync().as[SendMessage] match {
        case Right(SendMessage(_, chatId, text)) =>
          chatId shouldBe 1111111
          text shouldBe s"Успішно створено нагадування 13.02.2021 Valentines Day"
          db.size shouldBe 1
        case Left(_) => fail()
      }


      val showCommandJson = jsonWithCommand("/nagadai?")
      val showCmdResponse = serve(Request[IO](POST, Uri.unsafeFromString(s"/webhook/$token")).withEntity(showCommandJson))
      showCmdResponse.status shouldBe Status.Ok
      showCmdResponse.as[Json].unsafeRunSync().as[SendMessage] match {
        case Right(SendMessage(_, chatId, text)) =>
          chatId shouldBe 1111111
          text shouldBe s"Знайдено наступні нагадування:\n13.02.2021: Valentines Day"
          db.size shouldBe 1
        case Left(_) => fail()
      }

      val deleteCmdJson = jsonWithCommand("/nagadai- 13.02.2021")
      val deleteCmdResponse = serve(Request[IO](POST, Uri.unsafeFromString(s"/webhook/$token")).withEntity(deleteCmdJson))
      deleteCmdResponse.status shouldBe Status.Ok
      deleteCmdResponse.as[Json].unsafeRunSync().as[SendMessage] match {
        case Right(SendMessage(_, chatId, text)) =>
          chatId shouldBe 1111111
          text shouldBe s"Успішно видалено 1 нагадуваннь за 13.02.2021"
          db.size shouldBe 0
        case Left(_) => fail()
      }
    }
  }

  private def jsonWithCommand(command: String): Json = {
    import io.circe.parser._
    parse(
      s"""
       {
        "update_id":10000,
          "message":{
            "date":1441645532,
            "chat":{
               "last_name":"Test Lastname",
               "id":1111111,
               "first_name":"Test",
               "username":"Test",
               "type": "group"
            },
            "message_id":1365,
            "from":{
               "last_name":"Test Lastname",
               "id":1111111,
               "first_name":"Test",
               "username":"Test",
               "is_bot": false
            },
            "entities": [{"type" : "bot_command"}],
            "text":"$command"
          }
        }
    """).getOrElse(null)
  }


  private def serve(request: Request[IO]): Response[IO] = {
    service.orNotFound(request).unsafeRunSync()
  }
}
