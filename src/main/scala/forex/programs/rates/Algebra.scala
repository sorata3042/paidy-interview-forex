package forex.programs.rates

import forex.domain.Rate
import forex.programs.rates.errors.Error

trait Algebra[F[_]] {

  /** Obtain the [[Rate]] from the given from and to [[Currency]] values.
    *
    * @param request the from and to [[Currency]] values 
    * @return a [[Rate]]
    */
  def get(request: Protocol.GetRatesRequest): F[Either[Error, Rate]]

}
