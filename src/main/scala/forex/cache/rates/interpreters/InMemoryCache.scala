package forex.cache.rates.interpreters

import cats.effect.kernel.Async
import com.github.blemale.scaffeine.{ Cache, Scaffeine }
import forex.config.CacheConfig
import forex.domain.Rate
import forex.cache.rates.Algebra
import scala.collection.mutable.Map

class InMemoryCache[F[_]: Async](config: CacheConfig) extends Algebra[F] {

  private val cache: Cache[String, Rate] = Scaffeine()
      .expireAfterWrite(config.expireAfter)
      .maximumSize(72) // same size as the total number of pairs that can be created
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
