package repository

import cats.effect.IO
import doobie.util.transactor.Transactor
import fs2.Stream
import doobie.implicits._
import model.{Reminder, ReminderNotFound}
import doobie.implicits.javatime._

import java.time.LocalDate


class ReminderRepository(transactor: Transactor[IO]) {

  def getReminders(chatId: Int): Stream[IO, Reminder] = {
    sql"SELECT * FROM reminder where chatId = $chatId".query[Reminder].stream.transact(transactor)
  }

  def getReminders(chatId: Int, userId: Int): Stream[IO, Reminder] = {
    sql"SELECT * FROM reminder where chatId = $chatId and createdBy = $userId".query[Reminder].stream.transact(transactor)
  }

  def create(reminder: Reminder): IO[Long] = {
    sql"""INSERT into reminder (remindAt, description, chatId, createdBy, createdAt)
          VALUES (${reminder.remindAt}, ${reminder.description}, ${reminder.chatId}, ${reminder.createdBy}, ${reminder.createdAt})"""
      .update.withUniqueGeneratedKeys[Long]("id").transact(transactor)
  }

  def delete(chatId: Int, userId: Int, remindAt: LocalDate): IO[Either[model.ReminderNotFound.type, Int]] = {
    sql"DELETE FROM reminder where chatId = $chatId and createdBy = $userId and remindAt = $remindAt".update.run.transact(transactor).map{affectedRows =>
      if (affectedRows > 0) {
        Right(affectedRows)
      } else {
        Left(ReminderNotFound)
      }
    }
  }


}
