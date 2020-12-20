import cats.effect.{Blocker, ContextShift, IO, Resource}
import com.typesafe.config.ConfigFactory
import pureconfig._
import pureconfig.generic.auto._
import pureconfig.module.catseffect.syntax._


package object config {
  case class ServerConfig(host: String ,port: Int)
  case class Webhook(token: String)

  case class Config(server: ServerConfig, webhook: Webhook, dbFile: String)

  object Config {
    def load(configFile: String = "application.conf")(implicit cs: ContextShift[IO]): Resource[IO, Config] = {
      Blocker[IO].flatMap { blocker =>
        Resource.liftF(ConfigSource.fromConfig(ConfigFactory.load(configFile)).loadF[IO, Config](blocker))
      }
    }
  }
}
