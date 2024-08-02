package forex.domain

import io.circe.{ Decoder, Encoder }

case class Price(value: BigDecimal) extends AnyVal

object Price {

  def apply(value: Integer): Price =
    Price(BigDecimal(value))

  implicit val encodePrice: Encoder[Price] = Encoder.forProduct1("value")(_.value)

}
