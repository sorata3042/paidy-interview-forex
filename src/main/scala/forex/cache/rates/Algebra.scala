package forex.cache.rates

import forex.domain.Rate

trait Algebra[F[_]] {

  /** Retrieve a [[Rate]] from cache if present.
    *
    * @param pair
    * @return [[Rate]]
    */
  def get(pair: Rate.Pair): Option[Rate]

  /** Upserts cache with a [[Rate]].
    *
    * @param rate
    */
  def put(rate: Rate): Unit

  /** Upserts cache with a list of [[Rate]].
    *
    * @param rates
    */
  def putAll(rates: List[Rate]): Unit

}