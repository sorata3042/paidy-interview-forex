package forex.cache.rates.interpreters

import cats.effect.kernel.Async
import com.github.blemale.scaffeine.{ Cache, Scaffeine }
import forex.cache.rates.Algebra
import forex.config.CacheConfig
import forex.domain.{ Rate, Currency }
import scala.collection.mutable.Map

/** Accessor for Caffeine cache.
  * Rates are stored within the cache using the Pair as the key.
  *
  * @param config
  */
class InMemoryCache[F[_]: Async](config: CacheConfig) extends Algebra[F] {

  private val cache: Cache[String, Rate] = Scaffeine()
      .expireAfterWrite(config.expireAfter)
      .maximumSize(161) // total number of pairs that can be created with the intermediary currency
      .build[String, Rate]()

  override def get(pair: Rate.Pair): Option[Rate] =
    cache.getIfPresent(pair.toString)

  override def put(rate: Rate): Unit =
    cache.put(rate.pair.toString, rate)

  override def putAll(rates: List[Rate]): Unit =
    var rateMap: Map[String, Rate] = Map.empty[String, Rate]
    for (rate <- rates) {
      rateMap += (rate.pair.toString -> rate)
    }
    cache.putAll(rateMap.toMap)

  private implicit def pairToString(pair: Rate.Pair): String = pair.toString
}
