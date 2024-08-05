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
import org.scalatestplus.mockito.MockitoSugar

class ProgramSpec extends UnitSpec with MockitoSugar {

  val mockRateService: RatesService[IO] = mock[RatesService[IO]]
  val program = new Program[IO](mockRateService)

  behavior of "get"

  it should "return a Rate for a request with different currencies" in {
    // Arrange
    val request: GetRatesRequest = GetRatesRequest(Currency.USD, Currency.JPY)
    val pair: Rate.Pair = Rate.Pair(Currency.USD, Currency.JPY)
    val rate: Rate = Rate(pair, Price(0.777), Timestamp.now)

    when(mockRateService.get(pair)).thenReturn(IO.pure(rate.asRight[Error]))

    // Act
    val result = program.get(request).unsafeRunSync()

    // Assert
    assert(result.isRight)
    assert(result.contains(rate))
  }

  it should "return a Rate for a request of the same currency" in {
    // Arrange
    val request: GetRatesRequest = GetRatesRequest(Currency.USD, Currency.USD)
    val pair: Rate.Pair = Rate.Pair(Currency.USD, Currency.USD)
    val expectedRate: Rate = Rate(pair, Price(1), Timestamp.now)

    // Act
    val result = program.get(request).unsafeRunSync()

    // Assert
    result match {
      case Right(rate) => {
        assert(rate.pair == expectedRate.pair)
        assert(rate.price.equals(expectedRate.price))
      }
      case Left(_) => fail("The Either should be a Right")
    }

  }

}
