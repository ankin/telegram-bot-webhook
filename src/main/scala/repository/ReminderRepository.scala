package repository

import cats.effect.IO
import doobie.util.transactor.Transactor
import fs2.Stream
import doobie.implicits._
import model.Reminder
import doobie.implicits.javatime._


class ReminderRepository(transactor: Transactor[IO]) {

  def getRemindersByChatId(chatId: Int): Stream[IO, Reminder] = {
    sql"SELECT * FROM reminder where chatId = $chatId".query[Reminder].stream.transact(transactor)
  }

  def getReminderByChatIdAndUserId(chatId: Int, userId: Int): Stream[IO, Reminder] = {
    sql"SELECT * FROM reminder where chatId = $chatId and userId = $userId".query[Reminder].stream.transact(transactor)
  }

  def create(reminder: Reminder): IO[Long] = {
    sql"""INSERT into reminder (remindAt, description, chatId, createdBy, createdAt)
          VALUES (${reminder.remindAt}, ${reminder.description}, ${reminder.chatId}, ${reminder.createdBy}, ${reminder.createdAt})"""
      .update.withUniqueGeneratedKeys[Long]("id").transact(transactor)
  }

}
