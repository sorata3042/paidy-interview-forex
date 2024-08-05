package forex

import cats.effect.kernel.{ Async, Temporal }
import forex.config.ApplicationConfig
import forex.http.rates.RatesHttpRoutes 
import forex.programs.RatesProgram
import forex.services.{ RatesService, RatesServices }
import org.http4s.{ HttpApp, HttpRoutes}
import org.http4s.client.Client
import org.http4s.server.middleware.{ AutoSlash, Timeout }

class Module[F[_]: Temporal: Async](config: ApplicationConfig, client: Client[F]) {

  private val ratesService: RatesService[F] = RatesServices.oneFrameClient[F](client, config.oneFrame)

  private val ratesProgram: RatesProgram[F] = RatesProgram[F](ratesService)

  private val ratesHttpRoutes: HttpRoutes[F] = new RatesHttpRoutes[F](ratesProgram).routes

  type PartialMiddleware = HttpRoutes[F] => HttpRoutes[F]
  type TotalMiddleware = HttpApp[F] => HttpApp[F]

  private val routesMiddleware: PartialMiddleware = {
    { (http: HttpRoutes[F]) =>
      AutoSlash(http)
    }
  }

  private val appMiddleware: TotalMiddleware = { (http: HttpApp[F]) =>
    Timeout(config.http.timeout)(http)
  }

  private val http: HttpRoutes[F] = ratesHttpRoutes

  val httpApp: HttpApp[F] = appMiddleware(routesMiddleware(http).orNotFound)

}
