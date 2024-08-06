package forex.cache.rates

import forex.UnitSpec
import forex.domain.{ Currency, Price, Rate, Timestamp }
import forex.cache.rates.interpreters.InMemoryCache
import cats.effect.IO
import cats.effect.unsafe.implicits.global
import forex.config.CacheConfig
import scala.concurrent.duration.FiniteDuration
import java.util.concurrent.TimeUnit
import cats.implicits.catsSyntaxApplicativeId

class InMemoryCacheSpec extends UnitSpec {
    
  val pair = Rate.Pair(Currency.USD, Currency.JPY)
  val rate: Rate = Rate(pair, Price(1), Timestamp.now)
  
  val mockCacheConfig: CacheConfig = CacheConfig(FiniteDuration(1, TimeUnit.SECONDS))
  val cache = new InMemoryCache[IO](mockCacheConfig)

  behavior of "InMemoryCache"

  it should "store and retrieve Rate in/from cache" in {
    // Act
    cache.put(rate)
    val result = cache.get(pair)

    // Assert
    assert(result.isDefined)
    assert(result.contains(rate))
  }

  it should "store and retrieve updated Rate in/from cache" in {
    // Arrange
    val updatedRate: Rate = Rate(pair, Price(0.5), Timestamp.now)
 
    // Act
    cache.put(rate)
    cache.put(updatedRate)
    val result = cache.get(pair)

    // Assert
    assert(result.isDefined)
    assert(!result.contains(rate))
    assert(result.contains(updatedRate))
  }

  it should "error on get after Rate expires from cache" in {
    // Act
    cache.put(rate)
    Thread.sleep(2000) // wait for 2 seconds
    val result = cache.get(pair)

    // Assert
    assert(result.isEmpty)
  }

  it should "error on get if desired Rate is not in cache" in {
    // Arrange
    val notFoundPair = Rate.Pair(Currency.USD, Currency.USD)
 
    // Act
    val result = cache.get(notFoundPair)

    // Assert
    assert(result.isEmpty)
  }

}
