package service

import cats.effect.IO
import com.typesafe.scalalogging.LazyLogging
import io.circe.syntax._
import model._
import org.http4s.HttpRoutes
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl
import repository.DateOfBirthRepository
import rss.FeedParser


class WebhookService(token: String, repository: DateOfBirthRepository) extends Http4sDsl[IO] with LazyLogging {


  val routes: HttpRoutes[IO] = HttpRoutes.of[IO] {
    case req@POST -> Root / "webhook" / this.token =>
      for {
        update <- req.decodeJson[Update]
        _ <- IO(logger.info(s"Received update message: [${update.asJson.noSpaces}]"))
        action <- extractAction(update)
        responseEntity <- createResponse(action, update.message.chat.id)
        response <- Ok(responseEntity.asJson)
        _ <- IO(logger.info(s"Sending response [$response]"))
      } yield response
  }

  private def extractAction(update: Update): IO[Action] = IO {
    update.message.text match {
      case Some(text) =>
        update.message.entities match {
          case Some(msgEntities) if msgEntities.exists(_.`type` == "bot_command") && text == "/novyny" => CommandNews
          case Some(msgEntities) => ActionUnsupported
          case _ => TextMsg(text)
        }
      case None => ActionUnsupported
    }
  }

  private def createResponse(action: Action, chatId: Int): IO[SendMessage] = {
    action match {
      case CommandNews =>
        for {
          news <- FeedParser.getNewsEntries("http://www.tagesschau.de/xml/rss2/")
        } yield SendMessage(chatId = chatId, text = news.map(n => s"${n.title}\n${n.link}").mkString("\n\n"))
      case TextMsg(text) => IO(SendMessage(chatId = chatId, text = s"Шо? '$text'\nЯ ще не знаю шо з тим робити :("))
      case ActionUnsupported =>  IO(SendMessage(chatId = chatId, text = s"Я вмію тільки /novyny"))
    }
  }

  sealed trait Action
  final object ActionUnsupported extends Action

  sealed trait Command extends Action
  final object CommandNews extends Command

  sealed trait Text extends Action
  case class TextMsg(text: String) extends Text


}
