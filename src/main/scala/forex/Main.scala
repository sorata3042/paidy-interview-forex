package forex

import cats.effect.{ ExitCode, IOApp, IO }
import cats.effect.kernel.{ Async, Sync, Temporal }
import cats.effect.unsafe.implicits.global
import forex.config.Config
import fs2.Stream
import org.http4s.blaze.server.BlazeServerBuilder
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger

object Main extends IOApp {
  override def run(args: List[String]): IO[ExitCode] = {
    implicit def logger[F[_]: Sync]: Logger[F] = Slf4jLogger.getLogger[F]

    val program = for {
      _ <- Logger[IO].info("Starting Forex application")
      exitCode <- new Application[IO].stream().compile.drain.as(ExitCode.Success)
      _ <- Logger[IO].info("Ending Forex application")
    } yield exitCode

    program
  }
}

class Application[F[_]: Async: Temporal] {

  def stream(): Stream[F, Unit] =
    for {
      config <- Config.stream("app")
      module = new Module[F](config)
      _ <- BlazeServerBuilder[F]
            .bindHttp(config.http.port, config.http.host)
            .withHttpApp(module.httpApp)
            .serve
    } yield ()

}
