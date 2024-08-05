package forex.domain

import forex.UnitSpec
import io.circe.Json
import io.circe.syntax.EncoderOps

class CurrencySpec extends UnitSpec {

  val json = Json.fromString("USD")
  val currency = Currency.USD

  behavior of "Currency Codec"

  it should "decode JSON to expected Currency" in {
    // Act
    val actualCurrency = json.as[Currency]

    // Assert
    assert(actualCurrency === Right(currency))
  }

  it should "encode Currency to expected JSON" in {
    // Act
    val actualJson = currency.asJson

    // Assert
    assert(actualJson === json)
  }

}
