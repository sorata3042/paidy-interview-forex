package forex.services.rates

import forex.UnitSpec
import cats.effect.IO
import cats.effect.unsafe.implicits.global
import forex.config.OneFrameConfig
import forex.domain.{Currency, Price, Rate, Timestamp}
import forex.services.rates.interpreters.OneFrameClient
import org.http4s.{EntityDecoder, Request}
import org.http4s.FormDataDecoder.formEntityDecoder
import org.http4s.client.Client
import org.mockito.Mockito.when
import org.mockito.ArgumentMatchers.any
import org.scalatestplus.mockito.MockitoSugar

class OneFrameSpec extends UnitSpec with MockitoSugar {

  val pair: Rate.Pair = Rate.Pair(Currency.USD, Currency.JPY)
  val rate: Rate = Rate(pair, Price(1), Timestamp.now)

  val mockClient: Client[IO] = mock[Client[IO]]
  val mockOneFrameConfig: OneFrameConfig = OneFrameConfig("host", 80, "token")
  val oneFrameClient = new OneFrameClient[IO](mockClient, mockOneFrameConfig)

  behavior of "get"

  it should "retrieve a rate successfully" in {
    // Arrange
    when(mockClient.expect[List[Rate]](any[Request[IO]])(any[EntityDecoder[IO, List[Rate]]]))
        .thenReturn(IO.pure(List(rate)))

    // Act
    val result = oneFrameClient.get(pair).unsafeRunSync()

    // Assert
    assert(result.isRight)
    assert(result.contains(rate))
  }

}
