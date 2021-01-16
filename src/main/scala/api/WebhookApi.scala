package api


import cats.effect.IO
import com.typesafe.scalalogging.StrictLogging
import config.Webhook
import io.circe.syntax._
import io.circe.{Decoder, Encoder}
import model._
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl
import org.http4s.{EntityDecoder, EntityEncoder, HttpRoutes, MalformedMessageBodyFailure}
import service.{ReminderService, RssFeedService}


class WebhookApi(webhookConfig: Webhook, reminderService: ReminderService) extends Http4sDsl[IO] with StrictLogging {

  implicit def circeJsonDecoder[A: Decoder]: EntityDecoder[IO, A] = jsonOf[IO, A]
  implicit def circeJsonEncoder[A: Encoder]: EntityEncoder[IO, A] = jsonEncoderOf[IO, A]


  val routes: HttpRoutes[IO] = HttpRoutes.of[IO] {
    case req@POST -> Root / "webhook" / this.webhookConfig.token =>

      req.decode[Update] { update =>
        if (!webhookConfig.allowedChatIds.contains(update.message.chat.id)) {
          // TODO add test
          logger.info(s"Unsupported chat with id=[${update.message.chat.id}]. Ignoring message")
          Forbidden()
        } else {
          val requestHandler = for {
            _ <- IO(logger.info(s"Received update message: [${update.asJson.noSpaces}]"))
            action <- extractAction(update)
            responseEntity <- createResponse(action, update.message.chat.id)
            response <- Ok(responseEntity)
            _ <- IO(logger.info(s"Sending response [$response]"))
          } yield response

          requestHandler.handleErrorWith {
            case e: MalformedMessageBodyFailure =>
              logger.error("Failed to parse request body", e)
              BadRequest("Invalid JSON")
            case e: Throwable =>
              logger.error("Unexpected error in webhook router", e)
              InternalServerError("Unexpected error occurred")
          }
        }
      }


  }


  private def extractAction(update: Update): IO[Action] = IO {
    (update.message.text, update.message.from) match {
      case (Some(text), Some(user)) =>
        update.message.entities match {
          case Some(msgEntities) if msgEntities.exists(_.`type` == "bot_command") && text.contains("/novyny") => CommandNews
          case Some(msgEntities) if msgEntities.exists(_.`type` == "bot_command") && text.startsWith("/nagadai+") => CommandCreateReminder(chatId = update.message.chat.id, userId = user.id, text = text)
          case Some(msgEntities) if msgEntities.exists(_.`type` == "bot_command") && text.startsWith("/nagadai-") => CommandDeleteReminder(chatId = update.message.chat.id, userId = user.id, text = text)
          case Some(msgEntities) if msgEntities.exists(_.`type` == "bot_command") && text.startsWith("/nagadai?") => CommandShowReminders(chatId = update.message.chat.id, userId = user.id)
          case Some(_) => ActionUnsupported
          case _ => TextMsg(text)
        }
      case _ => ActionUnsupported
    }
  }

  private def createResponse(action: Action, chatId: Int): IO[SendMessage] = {
    action match {
      case CommandNews =>
        for {
          news <- RssFeedService.top10RssEntries("http://www.tagesschau.de/xml/rss2/")
        } yield SendMessage(chatId = chatId, text = news.map(n => s"${n.title}\n${n.link}").mkString("\n\n"))
      case command: CommandReminder => reminderService.process(command)
      case TextMsg(text) => IO.pure(SendMessage(chatId = chatId, text = s"Шо? '$text'\nЯ ще не знаю шо з тим робити :("))
      case ActionUnsupported => IO.pure(SendMessage(chatId = chatId, text = s"Я вмію тільки /novyny"))
    }
  }

}
