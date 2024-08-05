package forex.http.rates

import forex.domain.{ Currency, Price, Timestamp }
import io.circe.{ Decoder, Encoder }
import io.circe.derivation.Configuration
import io.circe.generic.semiauto.{ deriveDecoder, deriveEncoder }
import io.circe.Json
import java.time.format.DateTimeFormatter

object Protocol {

  implicit val configuration: Configuration = Configuration.default.withSnakeCaseMemberNames

  final case class GetApiRequest(
      from: Currency,
      to: Currency
  )

  final case class GetApiResponse(
      from: Currency,
      to: Currency,
      price: Price,
      timestamp: Timestamp
  )

  implicit val responseEncoder: Encoder[GetApiResponse] = (a: GetApiResponse) => Json.obj(
      ("from", Json.fromString(a.from.toString)),
      ("to", Json.fromString(a.to.toString)),
      ("price", Json.fromBigDecimal(a.price.value)),
      ("timestamp", Json.fromString(a.timestamp.value.toString))
  )

}
