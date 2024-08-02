package forex.http.rates

import forex.domain.{ Currency, Price, Timestamp }
import io.circe.{ Decoder, Encoder }
import io.circe.derivation.Configuration
import io.circe.generic.semiauto.{ deriveDecoder, deriveEncoder }

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

  implicit val responseEncoder: Encoder[GetApiResponse] = deriveEncoder[GetApiResponse]

}
