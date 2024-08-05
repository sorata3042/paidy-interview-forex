package forex.domain

import cats.effect.IO
import cats.effect.kernel.Sync
import io.circe.{ Decoder, Encoder, HCursor, Json }
import java.time.OffsetDateTime
import org.http4s.{ EntityDecoder, circe }

case class Rate(
    pair: Rate.Pair,
    price: Price,
    timestamp: Timestamp
)

object Rate {
  final case class Pair(
      from: Currency,
      to: Currency
  )

  object Pair {
    implicit val pairDecoder: Decoder[Pair] =
      Decoder.forProduct2("from", "to")(Pair.apply)

    implicit val pairEncoder: Encoder[Pair] =
      Encoder.forProduct2("from", "to")(pair => (pair.from, pair.to))
  }

  implicit val rateDecoder: Decoder[Rate] = Decoder.instance { cursor =>
    for {
      from <- cursor.get[String]("from")
      to <- cursor.get[String]("to")
      price <- cursor.get[BigDecimal]("price")
      timestamp <- cursor.get[OffsetDateTime]("time_stamp")
    } yield Rate(Pair(Currency.fromString(from), Currency.fromString(to)), Price(price), Timestamp(timestamp))
  }

  implicit val rateEncoder: Encoder[Rate] =
    Encoder.forProduct4("from", "to", "price", "time_stamp")(rate => (rate.pair.from, rate.pair.to, rate.price, rate.timestamp))

  implicit val rateEntityDecoder: EntityDecoder[IO, Rate] = circe.jsonOf[IO, Rate]
}
