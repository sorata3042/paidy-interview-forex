package forex.programs.rates

import cats.Functor
import cats.data.EitherT
import cats.implicits.catsSyntaxEitherId
import cats.implicits.catsSyntaxApplicativeId
import forex.cache.CacheStorage
import forex.domain.{ Price, Rate, Timestamp }
import forex.programs.rates.errors.Error
import forex.programs.rates.errors.toProgramError
import forex.services.RatesService
import fs2.Compiler.Target.forSync
import cats.Applicative
import forex.services.rates.errors.Error.OneFrameLookupFailed
import cats.implicits.toFunctorOps

class Program[F[_]: Applicative](
    ratesService: RatesService[F],
    cache: CacheStorage[F]
) extends Algebra[F] {

  override def get(request: Protocol.GetRatesRequest): F[Either[Error, Rate]] =
    val pair = Rate.Pair(request.from, request.to)
    if (pair.from == pair.to) {
      return Rate(Rate.Pair(request.from, request.to), Price(1), Timestamp.now)
        .asRight[Error]
        .pure[F]
    }

    cache.get(pair).match {
        case Some(cachedRate) => cachedRate.asRight[Error].pure[F]
        case None => getAndCacheRate(pair)
    }

  // refresh cache and return new value
  private def getAndCacheRate(pair: Rate.Pair): F[Either[Error, Rate]] =
    val clientOutput = ratesService.get(pair)
    clientOutput.map {
      case Left(error: OneFrameLookupFailed) => Left(toProgramError(error))
      case Right(rate: Rate) => {
        cache.put(rate)
        Right(rate)
      }
    }

}

object Program {

  def apply[F[_]: Applicative](
      ratesService: RatesService[F],
      cache: CacheStorage[F]
  ): Algebra[F] = new Program[F](ratesService, cache)

}
