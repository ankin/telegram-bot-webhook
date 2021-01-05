import io.circe.generic.extras.{Configuration, ConfiguredJsonCodec}

import java.time.{LocalDate, LocalDateTime}
import java.util.Date

package object model {

  implicit val customConfig = Configuration.default.withSnakeCaseMemberNames.withDefaults

  // incoming
  @ConfiguredJsonCodec case class Update(
                                          updateId: Int,
                                          message: Message,
                                          editMessage: Option[Message],
                                          channelPost: Option[Message],
                                          editedChannelPost: Option[Message]
                                        )

  @ConfiguredJsonCodec case class Message(
                                           messageId: Int,
                                           from: Option[User],
                                           forwardFrom: Option[User],
                                           chat: Chat,
                                           replyToMessage: Option[Message],
                                           text: Option[String],
                                           entities: Option[List[MessageEntity]]
                                         )

  @ConfiguredJsonCodec case class User(
                                        id: Int,
                                        isBot: Boolean,
                                        firstName: String,
                                        lastName: Option[String],
                                        username: Option[String]
                                      )

  @ConfiguredJsonCodec case class Chat(
                                        id: Int,
                                        `type`: String,
                                        title: Option[String]
                                      )

  @ConfiguredJsonCodec case class MessageEntity(`type`: String)

  // outgoing
  @ConfiguredJsonCodec case class SendMessage(
                                               method: String = "sendMessage",
                                               chatId: Int,
                                               text: String
                                             )


  // internal

  sealed trait Action
  final object ActionUnsupported extends Action

  sealed trait Command extends Action
  final object CommandNews extends Command
  final case class CommandCreateReminder(chatId: Int, userId: Int, text: String) extends Command

  sealed trait Text extends Action
  case class TextMsg(text: String) extends Text


  // db
  case class Reminder(
                       id: Option[Long] = None,
                       remindAt: LocalDate,
                       description: String,
                       chatId: Int,
                       createdBy: Int,
                       createdAt: LocalDateTime = LocalDateTime.now(),
                       updatedBy: Option[Int] = None,
                       updateAt: Option[LocalDateTime] = None,
                       executedAt: Option[LocalDateTime] = None
                     )


}
