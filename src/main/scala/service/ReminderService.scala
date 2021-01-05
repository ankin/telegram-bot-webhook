package service

import cats.effect.IO
import com.typesafe.scalalogging.StrictLogging
import model.{CommandCreateReminder, Reminder, SendMessage}
import repository.ReminderRepository
import service.ReminderService.{ReminderParseError, commandStr, reminderDateFormatter}

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import scala.util.Try

// /nagadai 13.02 Valentine's Day

class ReminderService(repository: ReminderRepository) extends StrictLogging {


  def createReminder(command: CommandCreateReminder): IO[SendMessage] = {
    parseReminder(command) match {
      case Left(error) =>
        IO.pure(logger.error("Failed to create reminder", error.msg)) *>
          IO.pure(SendMessage(chatId = command.chatId, text = "Нажаль, створити нагадування не вдалось :/")) // TODO
      case Right(reminder) =>
        for {
          id <- repository.create(reminder)
          _ <- IO.pure(logger.info(s"Successfully created reminder [$reminder] with id=$id"))
          result <- IO.pure(SendMessage(chatId = command.chatId, text = s"Успішно створено нагадування ${reminder.remindAt.getYear}.${reminder.remindAt.getMonth}.${reminder.remindAt.getDayOfMonth} ${reminder.description}"))
        } yield result
    }

  }

  private def parseReminder(command: CommandCreateReminder): Either[ReminderParseError, Reminder] = {
    val idx = command.text.indexOf(commandStr)
    val textWithoutPrefix = command.text.substring(idx)
    val args = textWithoutPrefix.split(" ")
    if (args.length < 2) {
      Left(ReminderParseError("Not enough arguments"))
    } else {
      val dateArg = args(1)
      Try(LocalDate.parse(dateArg, reminderDateFormatter))
        .map(Right(_))
        .getOrElse(Left(ReminderParseError(s"Failed to parse date $dateArg")))
        .map { remindAt =>
          val description = args(2) // TODO handle quotes if it's multiple words
          Reminder(remindAt = remindAt, description = description, chatId = command.chatId, createdBy = command.userId)
        }
    }

  }


}

object ReminderService {
  val commandStr = "/nagadai"
  val reminderDateFormat = "dd.MM.yyyy" // TODO support date without year and take current year
  val reminderDateFormatter = DateTimeFormatter.ofPattern(reminderDateFormat)

  case class ReminderParseError(msg: String)
}