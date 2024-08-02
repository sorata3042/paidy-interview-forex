package forex.domain

import io.circe.{ Decoder, Encoder }
import java.time.OffsetDateTime

case class Timestamp(value: OffsetDateTime) extends AnyVal

object Timestamp {

  def now: Timestamp =
    Timestamp(OffsetDateTime.now)

  implicit val encodeTimestamp: Encoder[Timestamp] = Encoder.forProduct1("value")(_.value)

}
