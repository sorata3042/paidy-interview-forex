package forex.cache.rates

import cats.effect.Async
import forex.config.CacheConfig
import forex.cache.rates.interpreters.InMemoryCache

object Interpreters {

  def inMemory[F[_]: Async](config: CacheConfig): Algebra[F] = new InMemoryCache[F](config)

}