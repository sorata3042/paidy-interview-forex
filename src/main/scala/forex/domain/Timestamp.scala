package forex.domain

import io.circe.{ Decoder, Encoder, HCursor, Json }
import java.time.OffsetDateTime

case class Timestamp(value: OffsetDateTime) extends AnyVal

object Timestamp {

  def now: Timestamp =
    Timestamp(OffsetDateTime.now)

  implicit val timestampDecoder: Decoder[Timestamp] = new Decoder[Timestamp] {
    final def apply(c: HCursor): Decoder.Result[Timestamp] =
      for {
        value <- c.value.as[OffsetDateTime]
      } yield {
        Timestamp(value)
      }
  }

  implicit val timestampEncoder: Encoder[Timestamp] = new Encoder[Timestamp] {
    final def apply(timestamp: Timestamp): Json =
      Json.fromString(timestamp.value.toString)
  }

}
