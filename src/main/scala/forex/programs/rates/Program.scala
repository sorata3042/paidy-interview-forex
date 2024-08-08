package forex.programs.rates

import cats.Applicative
import cats.data.EitherT
import cats.implicits.{ catsSyntaxEitherId, catsSyntaxApplicativeId, toFunctorOps }
import forex.cache.CacheStorage
import forex.domain.{ Currency, Price, Rate, Timestamp }
import forex.domain.Rate.Pair
import forex.programs.rates.errors.{ Error, toProgramError }
import forex.services.RatesService
import forex.services.rates.errors.Error.OneFrameLookupFailed
import fs2.Compiler.Target.forSync

/** Class for [[Rate]] retrieval logic.
  *
  * @param config
  */
class Program[F[_]: Applicative](ratesService: RatesService[F], cache: CacheStorage[F]) extends Algebra[F] {

  // using JPY as default currency intermediary
  val defaultCurrency: Currency = Currency.JPY

  override def get(request: Protocol.GetRatesRequest): F[Either[Error, Rate]] = {
    val requestPair = Rate.Pair(request.from, request.to)
    if (request.from == request.to) {
      return Rate(requestPair, Price(1), Timestamp.now)
        .asRight[Error]
        .pure[F]
    }

    // verify cache has an entry present
    cache.get(Pair(defaultCurrency, Currency.USD)).match {
        case Some(cachedRate) => calculatedDesiredRate(requestPair).asRight[Error].pure[F]
        case None => refreshCacheAndCalculateRate(requestPair)
    }
  }

  // get values from one-frame, populate cache, return newly calculated rate
  private def refreshCacheAndCalculateRate(requestPair: Pair): F[Either[Error, Rate]] = {
    ratesService.getAll().map {
      case Left(error: OneFrameLookupFailed) => Left(toProgramError(error))
      case Right(rates: List[Rate]) => {
        cache.putAll(rates)
        Right(calculatedDesiredRate(requestPair))
      }
    }
  }

  // calculates the desired rate w/ cached rates
  private def calculatedDesiredRate(requestPair: Pair): Rate = {
    // these are the rates stored within the cache
    if (requestPair.from == defaultCurrency) {
      cache.get(requestPair).get
    }
    // the inverse of the rates are stored within the cache
    else if (requestPair.to == defaultCurrency) {
      val cachedRate: Rate = cache.get(Pair(defaultCurrency, requestPair.from)).get
      Rate(requestPair, Price(BigDecimal(1)/cachedRate.price.value), cachedRate.timestamp)
    }
    // desired rate does not contain default transitional currency
    else {
      val fromRate: Rate = cache.get(Pair(defaultCurrency, requestPair.from)).get
      val toRate: Rate = cache.get(Pair(defaultCurrency, requestPair.to)).get
      Rate(requestPair, Price(toRate.price.value/fromRate.price.value), fromRate.timestamp)
    }
  }

}

object Program {

  def apply[F[_]: Applicative](
      ratesService: RatesService[F],
      cache: CacheStorage[F]
  ): Algebra[F] = new Program[F](ratesService, cache)

}
