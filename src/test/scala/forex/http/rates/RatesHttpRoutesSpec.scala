package forex.http.rates

import forex.UnitSpec
import cats.effect.IO
import cats.effect.unsafe.implicits.global
import forex.domain.{ Currency, Price, Rate, Timestamp }
import forex.http.jsonDecoder
import forex.programs.RatesProgram
import forex.programs.rates.Protocol.GetRatesRequest
import io.circe.Json
import io.circe.syntax.EncoderOps
import java.time.OffsetDateTime
import org.http4s.{ Method, EntityEncoder, Response, Request, Status }
import org.http4s.circe.CirceEntityEncoder
import org.http4s.implicits.uri
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar

class RatesHttpRoutesSpec extends UnitSpec with MockitoSugar {

  implicit val circeEntityEncoder: EntityEncoder[IO, Json] = CirceEntityEncoder.circeEntityEncoder

  val rate = 1
  val timestamp = Timestamp(OffsetDateTime.now())

  val mockratesProgram: RatesProgram[IO] = mock[RatesProgram[IO]]
  val httpRoutes = new RatesHttpRoutes[IO](mockratesProgram)

  behavior of "httpRoutes"

  it should "return a response for a request" in {
    // Arrange
    val request = Request[IO](Method.GET, uri"/rates?from=USD&to=JPY")
    val expectedResponse = s"""{"from":"USD","to":"JPY","price":$rate,"timestamp":"${timestamp.value.toString}"}"""
    when(mockratesProgram.get(GetRatesRequest(Currency.USD, Currency.JPY)))
        .thenReturn(IO(Right(Rate(Rate.Pair(Currency.USD, Currency.JPY), Price(BigDecimal(rate)), timestamp))))

    // Act
    val response = httpRoutes.routes.run(request).value.unsafeRunSync().getOrElse(Response.notFound)

    // Assert
    assert(response.status === Status.Ok)
    assert(response.as[String].unsafeRunSync().replaceAll("\t", "") === expectedResponse)
  }

}
