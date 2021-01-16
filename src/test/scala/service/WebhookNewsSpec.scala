package service

import api.WebhookApi
import cats.effect.IO
import config.Webhook
import io.circe.Json
import io.circe.literal._
import model.SendMessage
import org.http4s.circe._
import org.http4s.dsl.io._
import org.http4s.implicits._
import org.http4s.{Request, Response, Status, Uri}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

import java.util.UUID

class WebhookNewsSpec extends AnyWordSpec with Matchers {

  private val token = UUID.randomUUID().toString
  private val service = new WebhookApi(Webhook(token, "1111111"), null).routes

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
      val response = serve(Request[IO](POST, Uri.unsafeFromString(s"/webhook/$token")).withEntity(createJson))
      response.status shouldBe Status.Ok
      response.as[Json].unsafeRunSync().as[SendMessage] shouldBe Right(SendMessage(chatId = 1111111, text = "Шо? '/start'\nЯ ще не знаю шо з тим робити :("))

    }

    "handle /news" in {
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
            "text":"/news"
          }
        }
    """
      val response = serve(Request[IO](POST, Uri.unsafeFromString(s"/webhook/$token")).withEntity(createJson))
      response.status shouldBe Status.Ok
      response.as[Json].unsafeRunSync().as[SendMessage] match {
        case Right(SendMessage(_, chatId, text)) =>
          chatId shouldBe 1111111
          text.contains("https://") shouldBe true
        case Left(_) => fail()
      }
    }

    "handle /news command within text" in {
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
            "text":"bla bla bla /news"
          }
        }
    """
      val response = serve(Request[IO](POST, Uri.unsafeFromString(s"/webhook/$token")).withEntity(createJson))
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
      val response = serve(Request[IO](POST, Uri.unsafeFromString(s"/webhook/$token")).withEntity(createJson))
      response.status shouldBe Status.Ok
      response.as[Json].unsafeRunSync().as[SendMessage] shouldBe Right(SendMessage(chatId = 1111111, text = "Я вмію тільки /news"))
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
      val response = serve(Request[IO](POST, Uri.unsafeFromString(s"/webhook/$token")).withEntity(createJson))
      response.status shouldBe Status.Ok
      response.as[Json].unsafeRunSync().as[SendMessage] shouldBe Right(SendMessage(chatId = 1111111, text = "Я вмію тільки /news"))
    }


    "handle broken json" in {
      val createJson = " {\"text:\"some text\"}"
      val response = serve(Request[IO](POST, Uri.unsafeFromString(s"/webhook/$token")).withEntity(createJson))
      response.status shouldBe Status.BadRequest
    }
  }


  private def serve(request: Request[IO]): Response[IO] = {
    service.orNotFound(request).unsafeRunSync()
  }
}
