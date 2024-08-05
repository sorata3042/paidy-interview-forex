package forex.domain

import forex.UnitSpec
import io.circe.Json
import io.circe.syntax.EncoderOps
import java.time.{ OffsetDateTime, ZonedDateTime, ZoneId }

class RateSpec extends UnitSpec {

  val zonedDatetime: ZonedDateTime = ZonedDateTime.parse("2024-08-05T02:03:57.592Z")
  val offsetDateTime: OffsetDateTime = zonedDatetime.withZoneSameInstant(ZoneId.of("UTC"))
    .toOffsetDateTime()
  val timestamp: Timestamp = Timestamp(offsetDateTime)

  val pair = Rate.Pair(Currency.USD, Currency.JPY)
  val rate = Rate(pair, Price(1), timestamp)

  val json = Json.obj(
    ("from", Json.fromString(Currency.USD.toString)),
    ("to", Json.fromString(Currency.JPY.toString)),
    ("price", Json.fromBigDecimal(1)),
    ("time_stamp", Json.fromString("2024-08-05T02:03:57.592Z"))
  )

  behavior of "Rate Codec"

  it should "decode JSON to expected Rate" in {
    // Act
    val actualRate = json.as[Rate]
    
    // Assert
    assert(actualRate === Right(rate))
  }

  it should "decode JSON with extra fields to expected Rate" in {
    // Arrange
    val jsonWithExtraFields = Json.obj(
      ("from", Json.fromString(Currency.USD.toString)),
      ("to", Json.fromString(Currency.JPY.toString)),
      ("bid", Json.fromBigDecimal(1)),
      ("ask", Json.fromBigDecimal(1)),
      ("price", Json.fromBigDecimal(1)),
      ("time_stamp", Json.fromString("2024-08-05T02:03:57.592Z"))
    )

    // Act
    val actualRate = jsonWithExtraFields.as[Rate]
    
    // Assert
    assert(actualRate === Right(rate))
  }

  it should "encode Rate to expected JSON" in {
    // Act
    val actualJson = rate.asJson

    // Assert
    assert(actualJson === json)
  }

}
