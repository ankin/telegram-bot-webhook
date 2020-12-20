package service

import cats.effect.IO
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder}
import model._
import org.http4s.HttpRoutes
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl
import repository.DateOfBirthRepository


class WebhookService(token: String, repository: DateOfBirthRepository) extends Http4sDsl[IO] {

  private implicit val encoder: Encoder[DateOfBirth] = deriveEncoder[DateOfBirth]
  private implicit val decoder: Decoder[DateOfBirth] = deriveDecoder[DateOfBirth]


  val routes = HttpRoutes.of[IO] {

    // TODO use token as url param
    case req@POST -> Root / "webhook" / "123" =>
      for {
        update <- req.decodeJson[Update]
        // TODO reaction on event
        // TODO logging
        response <- Ok()
      } yield response
  }

}
