import io.circe.generic.extras.{Configuration, ConfiguredJsonCodec}

package object model {

  case class DateOfBirth(dateOfBirth: String)


  implicit val customConfig = Configuration.default.withSnakeCaseMemberNames.withDefaults

  @ConfiguredJsonCodec case class Update(updateId: Int, message: Message, editMessage: Option[Message], channelPost: Option[Message], editedChannelPost: Option[Message])
  @ConfiguredJsonCodec case class Message(messageId: Integer, from: Option[User], forwardFrom: Option[User], chat: Chat, replyToMessage: Option[Message], text: Option[String])
  @ConfiguredJsonCodec case class User(id: Integer, isBot: Boolean, firstName: String, lastName: Option[String], username: Option[String])
  @ConfiguredJsonCodec case class Chat(id: Integer, `type`: String, title: Option[String])
}
