package forex.domain

import cats.Show
import io.circe.{ Decoder, Encoder, HCursor, Json }

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

  implicit val show: Show[Currency] = Show.show(c => c.toString)

  def fromString(s: String): Currency = s.toUpperCase match {
    case "AUD" => AUD
    case "CAD" => CAD
    case "CHF" => CHF
    case "EUR" => EUR
    case "GBP" => GBP
    case "NZD" => NZD
    case "JPY" => JPY
    case "SGD" => SGD
    case "USD" => USD
  }

  implicit val currencyDecoder: Decoder[Currency] = new Decoder[Currency] {
    final def apply(c: HCursor): Decoder.Result[Currency] =
      for {
        value <- c.value.as[String]
      } yield {
        Currency.fromString(value)
      }
  }

  implicit val currencyEncoder: Encoder[Currency] = new Encoder[Currency] {
    final def apply(currency: Currency): Json =
      Json.fromString(currency.toString)
  }
}
