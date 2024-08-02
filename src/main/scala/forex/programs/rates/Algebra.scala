package forex.programs.rates

import forex.domain.Rate
import forex.programs.rates.errors.Error

trait Algebra[F[_]] {
  def get(request: Protocol.GetRatesRequest): F[Error Either Rate]
}
