package forex.programs.rates

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import cats.implicits.catsSyntaxEitherId
import forex.UnitSpec
import forex.domain.{Currency, Price, Rate, Timestamp}
import forex.programs.rates.Protocol.GetRatesRequest
import forex.programs.rates.errors.Error
import forex.services.RatesService
import org.mockito.Mockito.when
import org.mockito.Mockito.verifyNoInteractions
import org.scalatestplus.mockito.MockitoSugar
import forex.cache.CacheStorage
import forex.programs.rates.errors.Error.RateLookupFailed
import forex.services.rates.errors.Error.OneFrameLookupFailed

class ProgramSpec extends UnitSpec with MockitoSugar {

  val errorMsg = "This is the error message"

  val mockRateService: RatesService[IO] = mock[RatesService[IO]]
  val mockCache: CacheStorage[IO] = mock[CacheStorage[IO]]
  val program = new Program[IO](mockRateService, mockCache)

  behavior of "get"

  it should "return a standard Rate for a request of the same currency" in {
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

  it should "return a cached Rate for a request with different currencies" in {
    // Arrange
    val request: GetRatesRequest = GetRatesRequest(Currency.USD, Currency.JPY)
    val pair: Rate.Pair = Rate.Pair(Currency.USD, Currency.JPY)
    val rate: Rate = Rate(pair, Price(0.777), Timestamp.now)

    when(mockCache.get(pair)).thenReturn(Option(rate))
    when(mockRateService.get(pair)).thenReturn(IO.pure(rate.asRight[Error]))

    // Act
    val result = program.get(request).unsafeRunSync()

    // Assert
    assert(result.isRight)
    assert(result.contains(rate))
    verifyNoInteractions(mockRateService)
  }

  it should "return an un-cached Rate for a request with different currencies" in {
    // Arrange
    val request: GetRatesRequest = GetRatesRequest(Currency.USD, Currency.JPY)
    val pair: Rate.Pair = Rate.Pair(Currency.USD, Currency.JPY)
    val rate: Rate = Rate(pair, Price(2), Timestamp.now)

    when(mockCache.get(pair)).thenReturn(Option.empty)
    when(mockRateService.get(pair)).thenReturn(IO.pure(rate.asRight[Error]))

    // Act
    val result = program.get(request).unsafeRunSync()

    // Assert
    assert(result.isRight)
    assert(result.contains(rate))
  }

  it should "return an Error for a failed request" in {
    // Arrange
    val request: GetRatesRequest = GetRatesRequest(Currency.USD, Currency.JPY)
    val pair: Rate.Pair = Rate.Pair(Currency.USD, Currency.JPY)
    val oneFrameError = OneFrameLookupFailed(errorMsg)
    val expectedError = RateLookupFailed(errorMsg)

    when(mockCache.get(pair)).thenReturn(Option.empty)
    when(mockRateService.get(pair)).thenReturn(IO.pure(oneFrameError.asLeft[Rate]))

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
