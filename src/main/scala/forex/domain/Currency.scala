package forex.domain

import io.circe.{ Decoder, Encoder }
import io.circe.generic.semiauto.{ deriveDecoder, deriveEncoder }

enum Currency(val value: String) {
  case AUD extends Currency("AUD")
  case CAD extends Currency("CAD")
  case CHF extends Currency("CHF")
  case EUR extends Currency("EUR")
  case GBP extends Currency("GBP")
  case NZD extends Currency("NZD")
  case JPY extends Currency("JPY")
  case SGD extends Currency("SGD")
  case USD extends Currency("USD")
}

object Currency {

  def parseValue(value: String): Either[String, Currency] =
    values
      .find(_.value == value)
      .toRight(s"$value is not a valid Currency")

  implicit val decodeCurrency: Decoder[Currency] = deriveDecoder[Currency]
  implicit val encodeCurrency: Encoder[Currency] = deriveEncoder[Currency]

}
