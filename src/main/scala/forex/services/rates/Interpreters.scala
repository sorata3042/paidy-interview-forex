package forex.services.rates

import cats.Applicative
import cats.effect.kernel.Async
import forex.config.OneFrameConfig
import forex.services.rates.interpreters.OneFrameClient
import org.http4s.client.Client

object Interpreters {

  def oneFrameClient[F[_]: Async](
    client: Client[F],
    config: OneFrameConfig
  ): Algebra[F] = new OneFrameClient[F](client, config)

}
