package forex.domain

import forex.UnitSpec
import io.circe.Json
import io.circe.syntax.EncoderOps
import java.time.{ OffsetDateTime, ZonedDateTime, ZoneId }

class TimestampSpec extends UnitSpec {

  val json = Json.fromString("2024-08-05T02:03:57.592Z")

  val zonedDatetime: ZonedDateTime = ZonedDateTime.parse("2024-08-05T02:03:57.592Z")
  val offsetDateTime: OffsetDateTime = zonedDatetime.withZoneSameInstant(ZoneId.of("UTC"))
    .toOffsetDateTime()
  val timestamp: Timestamp = Timestamp(offsetDateTime)

  behavior of "Timestamp Codec"

  it should "decode JSON to expected Timestamp" in {
    // Act
    val actualCurrency = json.as[Timestamp]

    // Assert
    assert(actualCurrency === Right(timestamp))
  }

  it should "encode Timestamp to expected JSON" in {
    // Act
    val actualJson = timestamp.asJson

    // Assert
    assert(actualJson === json)
  }

}
