package service

import cats.effect.IO
import io.circe.Json
import io.circe.literal._
import model.SendMessage
import org.http4s.circe._
import org.http4s.dsl.io._
import org.http4s.implicits._
import org.http4s.{Request, Response, Status, Uri}
import org.scalamock.scalatest.MockFactory
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import repository.DateOfBirthRepository

import java.util.UUID

class WebhookSpec extends AnyWordSpec with MockFactory with Matchers {

  private val token = UUID.randomUUID().toString
  private val repository = stub[DateOfBirthRepository]
  private val service = new WebhookService(token, repository).routes

  "WebhookService" should {
    "handle text" in {
      val createJson =
        json"""
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
            "text":"/start"
          }
        }
    """
      val response = serve(Request[IO](POST, Uri.unsafeFromString(s"/webhook/${token}")).withEntity(createJson))
      response.status shouldBe Status.Ok
      response.as[Json].unsafeRunSync().as[SendMessage] shouldBe Right(SendMessage(chatId = 1111111, text = "Шо? '/start'\nЯ ще не знаю шо з тим робити :("))

    }

    "handle /новини" in {
      val createJson =
        json"""
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
            "text":"/новини"
          }
        }
    """
      val response = serve(Request[IO](POST, Uri.unsafeFromString(s"/webhook/${token}")).withEntity(createJson))
      response.status shouldBe Status.Ok
      response.as[Json].unsafeRunSync().as[SendMessage] match {
        case Right(SendMessage(_, chatId, text)) =>
          chatId shouldBe 1111111
          text.contains("https://") shouldBe true
        case Left(_) => fail()
      }
    }

    "handle /бла-бла" in {
      val createJson =
        json"""
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
            "text":"/бла-бла"
          }
        }
    """
      val response = serve(Request[IO](POST, Uri.unsafeFromString(s"/webhook/${token}")).withEntity(createJson))
      response.status shouldBe Status.Ok
      response.as[Json].unsafeRunSync().as[SendMessage] shouldBe Right(SendMessage(chatId = 1111111, text = "Я вмію тільки /новини"))
    }

    "handle bold text" in {
      val createJson =
        json"""
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
            "entities": [{"type" : "bold"}],
            "text":"some text"
          }
        }
    """
      val response = serve(Request[IO](POST, Uri.unsafeFromString(s"/webhook/${token}")).withEntity(createJson))
      response.status shouldBe Status.Ok
      response.as[Json].unsafeRunSync().as[SendMessage] shouldBe Right(SendMessage(chatId = 1111111, text = "Я вмію тільки /новини"))
    }

  }


  private def serve(request: Request[IO]): Response[IO] = {
    service.orNotFound(request).unsafeRunSync()
  }
}
