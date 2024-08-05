package forex.domain

import forex.UnitSpec
import io.circe.Json
import io.circe.syntax.EncoderOps

class PriceSpec extends UnitSpec {

  val json = Json.fromBigDecimal(0.8243869101616611)
  val price = Price(BigDecimal(0.8243869101616611))

  behavior of "Price Codec"

  it should "decode JSON to expected Price" in {
    // Act
    val actualPrice = json.as[Price]

    // Assert
    assert(actualPrice === Right(price))
  }

  it should "encode Price to expected JSON" in {
    // Act
    val actualJson = price.asJson

    // Assert
    assert(actualJson === json)
  }

}
