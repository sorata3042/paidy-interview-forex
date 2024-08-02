package forex.services.rates

import cats.Applicative
import forex.services.rates.interpreters.OneFrameDummy

object Interpreters {
  def dummy[F[_]: Applicative]: Algebra[F] = new OneFrameDummy[F]()
}
