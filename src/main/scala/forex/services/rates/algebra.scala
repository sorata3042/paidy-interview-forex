package forex.services.rates

import forex.domain.Rate
import forex.services.rates.errors.Error.OneFrameLookupFailed

trait Algebra[F[_]] {

  /** Obtain the [[Rate]] from OneFrameService for the given 
    * [[Rate.Pair]] or a [[OneFrameLookupFailed]] if the OneFrameService
    * call doesn't succeed.
    *
    * @param pair the currency pair
    * @return a [[Rate]]
    */
  def get(pair: Rate.Pair): F[Either[OneFrameLookupFailed, Rate]]

  /** Obtain all [[Rate]] from OneFrameService for the each possible
    * [[Rate.Pair]] or a [[OneFrameLookupFailed]] if the OneFrameService
    * call doesn't succeed.
    *
    * @return a list of [[Rate]]
    */
  def getAll(): F[Either[OneFrameLookupFailed, List[Rate]]]

}
