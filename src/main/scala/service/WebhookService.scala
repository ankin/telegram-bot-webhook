package service

import cats.effect.IO
import com.typesafe.scalalogging.LazyLogging
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder}
import model._
import org.http4s.HttpRoutes
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl
import repository.DateOfBirthRepository
import io.circe.syntax._


class WebhookService(token: String, repository: DateOfBirthRepository) extends Http4sDsl[IO] with LazyLogging {


//  private implicit val encoder: Encoder[DateOfBirth] = deriveEncoder[DateOfBirth]
//  private implicit val decoder: Decoder[DateOfBirth] = deriveDecoder[DateOfBirth]


  val routes = HttpRoutes.of[IO] {
    case req@POST -> Root / "webhook" / token =>
      for {
        update <- req.decodeJson[Update]
        _ <- IO(logger.info(s"Received update message: [${update.asJson.noSpaces}]"))
        responseJson <- IO(SendMessage(chatId = update.message.chat.id, text = "Hello there!").asJson)
        response <- Ok(responseJson)
        _ <- IO(logger.info(s"Sending response [${response}] with body [${responseJson.noSpaces}]"))
      } yield response
  }

}
