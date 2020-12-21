import cats.effect._
import config.Config
import org.http4s.implicits._
import org.http4s.server.blaze.BlazeServerBuilder
import repository.DateOfBirthRepository
import service.WebhookService

object HttpServer {
  def create(configFile: String = "application.conf")(implicit contextShift: ContextShift[IO], concurrentEffect: ConcurrentEffect[IO], timer: Timer[IO]): IO[ExitCode] = {
    Config.load(configFile).use(create)
  }


  private def create(config: Config)(implicit concurrentEffect: ConcurrentEffect[IO], timer: Timer[IO]): IO[ExitCode] = {
    val repository = new DateOfBirthRepository(config.dbFile)
    for {
      exitCode <- BlazeServerBuilder[IO]
        .bindHttp(config.server.port, config.server.host)
        .withHttpApp(new WebhookService(config.webhook.token, repository).routes.orNotFound).serve.compile.lastOrError
    } yield exitCode
  }


}
