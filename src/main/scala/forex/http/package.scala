package forex

import cats.effect.kernel.Sync
import cats.effect.kernel.Temporal
import forex.http.rates.Protocol.configuration
import io.circe.{ Decoder, Encoder }
import io.circe.generic.semiauto.{ deriveDecoder, deriveEncoder }
import org.http4s.{ EntityDecoder, EntityEncoder }
import org.http4s.circe.{ jsonEncoderOf, jsonOf }

package object http {

  implicit def jsonDecoder[A <: Product: Decoder, F[_]: Sync: Temporal]: EntityDecoder[F, A] = jsonOf[F, A]
  implicit def jsonEncoder[A <: Product: Encoder, F[_]]: EntityEncoder[F, A] = jsonEncoderOf[F, A]

}
