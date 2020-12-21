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
    "fire a request" in {
      val createJson =
        json"""
        {
        "update_id":646911460,
        "message":{
            "message_id":93,
            "from":{
                "id":10000123,
                "is_bot":false,
                "first_name":"Jiayu",
                "username":"jiayu",
                "language_code":"en-US"
            },
            "chat":{
                "id":100001234,
                "first_name":"Jiayu",
                "username":"jiayu",
                "type":"private"
            },
            "date":1509641174,
            "text":"eevee"
        }
    }
    """
      val response = serve(Request[IO](POST, Uri.unsafeFromString(s"/webhook/${token}")).withEntity(createJson))
      response.status shouldBe Status.Ok
      response.as[Json].unsafeRunSync().as[SendMessage] shouldBe Right(SendMessage(chatId = 100001234, text = "Hello there!"))
    }


  }

  private def serve(request: Request[IO]): Response[IO] = {
    service.orNotFound(request).unsafeRunSync()
  }
}
