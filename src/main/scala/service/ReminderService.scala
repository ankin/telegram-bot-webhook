package service

import cats.effect.IO
import com.typesafe.scalalogging.StrictLogging
import model.{CommandCreateReminder, CommandDeleteReminder, CommandReminder, CommandShowReminders, Reminder, SendMessage}
import repository.ReminderRepository
import service.ReminderService.{ReminderError, invalidDateError, notEnoughArgsError, reminderDateFormatter}

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import scala.util.Try

// /reminder_add 13.02.2021 Valentine's Day
// /reminder_del 13.02.2021
// /reminder_list

class ReminderService(repository: ReminderRepository) extends StrictLogging {


  def process(command: CommandReminder): IO[SendMessage] = {
    command match {
      case create: CommandCreateReminder => createReminder(create)
      case delete: CommandDeleteReminder => deleteReminder(delete)
      case show: CommandShowReminders => showReminders(show)
    }
  }

  private def createReminder(command: CommandCreateReminder): IO[SendMessage] = {
    parseDateAndDescription(command.text, CommandCreateReminder.command) match {
      case Left(error: ReminderError) =>
        IO.pure(logger.error("Failed to create reminder", error.msg)) *>
          IO.pure(SendMessage(chatId = command.chatId, text = error.msgLocal))
      case Right((remindAt, description)) =>
        val reminder = Reminder(remindAt = remindAt, description = description, chatId = command.chatId, createdBy = command.userId)
        for {
          id <- repository.create(reminder)
          _ <- IO.pure(logger.info(s"Successfully created reminder [$reminder] with id=$id"))
          result <- IO.pure(SendMessage(chatId = command.chatId, text = s"Успішно створено нагадування ${reminderDateFormatter.format(remindAt)} ${reminder.description}"))
        } yield result
    }

  }

  def deleteReminder(delete: CommandDeleteReminder): IO[SendMessage] = {
    parseDate(delete.text, CommandDeleteReminder.command) match {
      case Left(error: ReminderError) =>
        IO.pure(logger.error("Failed to create reminder", error.msg)) *>
          IO.pure(SendMessage(chatId = delete.chatId, text = error.msgLocal))
      case Right(remindAt) =>

        repository.delete(chatId = delete.chatId, userId = delete.userId, remindAt = remindAt).flatMap {
          case Right(nrDeleted) =>
            IO.pure(logger.info(s"Successfully deleted $nrDeleted reminders for date $remindAt")) *>
              IO.pure(SendMessage(chatId = delete.chatId, text = s"Успішно видалено $nrDeleted нагадуваннь за ${reminderDateFormatter.format(remindAt)}"))
          case Left(_) =>
            IO.pure(logger.info(s"Nothing to deleted for date $remindAt")) *>
              IO.pure(SendMessage(chatId = delete.chatId, text = s"Жодного нагадування за ${reminderDateFormatter.format(remindAt)} не видалено"))
        }
    }
  }

  def showReminders(show: CommandShowReminders): IO[model.SendMessage] = {
    repository.getReminders(chatId = show.chatId, userId = show.userId).compile.toList.map { reminders =>

      val remindersStr = reminders.map { reminder =>
        // TODO created by whom?
        s"${reminderDateFormatter.format(reminder.remindAt)}: ${reminder.description}"
      }.mkString("\n")

      SendMessage(chatId = show.chatId, text = s"Знайдено наступні нагадування:\n$remindersStr")
    }
  }

  private def parseDate(text: String, command: String): Either[ReminderError, LocalDate] = {
    val textWithoutPrefix = text.stripPrefix(command).drop(1)
    val args = textWithoutPrefix.split(" ")
    if (args.length < 1) {
      Left(ReminderError("Not enough arguments", notEnoughArgsError))
    } else {
      val dateArg = args(0)
      Try(LocalDate.parse(dateArg, reminderDateFormatter))
        .map(Right(_))
        .getOrElse(Left(ReminderError(s"Failed to parse date $dateArg", invalidDateError)))
    }
  }

  private def parseDateAndDescription(text: String, command: String): Either[ReminderError, (LocalDate, String)] = {
    val textWithoutPrefix = text.stripPrefix(command).drop(1)
    val args = textWithoutPrefix.split(" ")
    if (args.length < 2) {
      Left(ReminderError("Not enough arguments", notEnoughArgsError))
    } else {
      val dateArg = args(0)
      Try(LocalDate.parse(dateArg, reminderDateFormatter))
        .map(Right(_))
        .getOrElse(Left(ReminderError(s"Failed to parse date $dateArg", invalidDateError)))
        .map { remindAt =>
          val description = args.drop(1).mkString(" ")
          remindAt -> description
        }
    }
  }
}

object ReminderService {
  val reminderDateFormat = "dd.MM.yyyy" // TODO support date without year and take current year
  val reminderDateFormatter = DateTimeFormatter.ofPattern(reminderDateFormat)

  val invalidDateError = "Не вдалось створити нагадування :/ Неправильний формат дати"
  val notEnoughArgsError = "Не вдалось створити нагадування :/ Не достатньо параметрів"


  case class ReminderError(msg: String, msgLocal: String)

}