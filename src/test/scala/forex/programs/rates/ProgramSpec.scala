package forex.programs.rates

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import cats.implicits.catsSyntaxEitherId
import forex.UnitSpec
import forex.cache.CacheStorage
import forex.domain.{ Currency, Price, Rate, Timestamp }
import forex.domain.Rate.Pair
import forex.services.RatesService
import forex.programs.rates.Protocol.GetRatesRequest
import forex.programs.rates.errors.Error
import forex.programs.rates.errors.Error.RateLookupFailed
import forex.services.rates.errors.Error.OneFrameLookupFailed
import org.mockito.Mockito.when
import org.mockito.Mockito.verifyNoInteractions
import org.scalatest.BeforeAndAfter
import org.scalatestplus.mockito.MockitoSugar

class ProgramSpec extends UnitSpec with MockitoSugar with BeforeAndAfter {

  val mockRateService: RatesService[IO] = mock[RatesService[IO]]
  val mockCache: CacheStorage[IO] = mock[CacheStorage[IO]]
  val program = new Program[IO](mockRateService, mockCache)

  before {
    val checkPair: Rate.Pair = Rate.Pair(Currency.JPY, Currency.USD)
    val checkRate: Rate = Rate(checkPair, Price(1), Timestamp.now)
    when(mockCache.get(checkPair)).thenReturn(Option(checkRate))
    when(mockRateService.getAll()).thenReturn(IO.pure(List(checkRate).asRight[Error]))
  }

  it should "return a standard Rate when the request is of the same currency" in {
    // Arrange
    val request: GetRatesRequest = GetRatesRequest(Currency.USD, Currency.USD)
    val pair: Rate.Pair = Rate.Pair(Currency.USD, Currency.USD)
    val expectedRate: Rate = Rate(pair, Price(1), Timestamp.now)

    // Act
    val result = program.get(request).unsafeRunSync()

    // Assert
    result match {
      case Left(_) => fail("The Either should be a Right")
      case Right(rate) => {
        assert(rate.pair == expectedRate.pair)
        assert(rate.price.equals(expectedRate.price))
      }
    }
    verifyNoInteractions(mockCache)
    verifyNoInteractions(mockRateService)
  }

  it should "return a Rate has a from JPY currency" in {
    // Arrange
    val fromJpyRequest: GetRatesRequest = GetRatesRequest(Currency.JPY, Currency.CAD)
    val cachedPair: Rate.Pair = Rate.Pair(Currency.JPY, Currency.CAD)
    val cachedRate: Rate = Rate(cachedPair, Price(0.777), Timestamp.now)

    when(mockCache.get(cachedPair)).thenReturn(Option(cachedRate))

    // Act
    val result = program.get(fromJpyRequest).unsafeRunSync()

    // Assert
    assert(result.isRight)
    assert(result.contains(cachedRate))
    verifyNoInteractions(mockRateService)
  }

  it should "return a Rate when the request has a to JPY currency" in {
    // Arrange
    val toJpyRequest: GetRatesRequest = GetRatesRequest(Currency.AUD, Currency.JPY)
    val pair: Pair = Rate.Pair(Currency.JPY, Currency.AUD)
    val cachedRate: Rate = Rate(pair, Price(10), Timestamp.now)
    val expectedToJpyPair: Pair = Rate.Pair(Currency.AUD, Currency.JPY)
    // 1 JPY = 10 AUD so 1 AUD = 0.1 JPY
    val expectedToJpyRate: Rate = Rate(expectedToJpyPair, Price(BigDecimal(0.1)), cachedRate.timestamp)  

    when(mockCache.get(pair)).thenReturn(Option(cachedRate))

    // Act
    val result = program.get(toJpyRequest).unsafeRunSync()

    // Assert
    assert(result.isRight)
    assert(result.contains(expectedToJpyRate))
    verifyNoInteractions(mockRateService)
  }

  it should "return a Rate when the request has two non-JPY currencies" in {
    // Arrange
    val request: GetRatesRequest = GetRatesRequest(Currency.CAD, Currency.AUD)
    val currentTimestamp = Timestamp.now
    val fromPair: Pair = Rate.Pair(Currency.JPY, Currency.CAD)
    val cachedFromRate: Rate = Rate(fromPair, Price(4), currentTimestamp)
    val toPair: Pair = Rate.Pair(Currency.JPY, Currency.AUD)
    val cachedToRate: Rate = Rate(toPair, Price(2), currentTimestamp)
    val expectedPair: Pair = Pair(Currency.CAD, Currency.AUD)
    // 1 JPY = 4 CAD & 1 JPY = 2 AUD so 1 CAD = 0.5 AUD
    val expectedRate: Rate = Rate(expectedPair, Price(0.5), currentTimestamp)

    when(mockCache.get(fromPair)).thenReturn(Option(cachedFromRate))
    when(mockCache.get(toPair)).thenReturn(Option(cachedToRate))

    // Act
    val result = program.get(request).unsafeRunSync()

    // Assert
    assert(result.isRight)
    assert(result.contains(expectedRate))
  }

  it should "return a Rate when the cache is initially empty and refreshes" in {
    // Arrange
    val request: GetRatesRequest = GetRatesRequest(Currency.JPY, Currency.USD)
    val cachedPair: Rate.Pair = Rate.Pair(Currency.JPY, Currency.USD)
    val cachedRate: Rate = Rate(cachedPair, Price(0.987654321), Timestamp.now)

    // cache check call returns empty, later cache call returns a rate
    when(mockCache.get(cachedPair)).thenReturn(Option.empty).thenReturn(Option(cachedRate))
    when(mockRateService.getAll()).thenReturn(IO.pure(List(cachedRate).asRight[Error]))

    // Act
    val result = program.get(request).unsafeRunSync()

    // Assert
    assert(result.isRight)
    assert(result.contains(cachedRate))
  }

  it should "return an exception for a failed request when the cache is empty and one-frame client errors" in {
    // Arrange
    val request: GetRatesRequest = GetRatesRequest(Currency.JPY, Currency.USD)
    val cacheCheckPair: Rate.Pair = Rate.Pair(Currency.JPY, Currency.USD)
    val errorMsg = "This is the error message"
    val oneFrameError = OneFrameLookupFailed(errorMsg)
    val expectedError = RateLookupFailed(errorMsg)

    when(mockCache.get(cacheCheckPair)).thenReturn(Option.empty) // empty cache
    when(mockRateService.getAll()).thenReturn(IO.pure(oneFrameError.asLeft[Rate])) // failed one-frame call

    // Act
    val result = program.get(request).unsafeRunSync()

    // Assert
    result match {
      case Left(error) => {
        assert(error == expectedError)
      }
      case Right(_) => fail("The Either should be a Left")
    }
  }

}
