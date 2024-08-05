package forex.services.rates.interpreters

import cats.Applicative
import cats.syntax.functor.toFunctorOps
import cats.syntax.applicative.catsSyntaxApplicativeId
import cats.syntax.either.catsSyntaxEitherId
import cats.syntax.all.toArrowChoiceOps
import forex.config.OneFrameConfig
import forex.domain.{ Currency, Price, Rate, Timestamp }
import forex.services.rates.Algebra
import forex.services.rates.errors.Error
import forex.services.rates.errors.Error.OneFrameLookupFailed
import org.http4s.{ Request, Response }
import org.http4s._
import org.http4s.client.Client
import org.http4s.implicits.uri
import org.typelevel.ci.CIString
import forex.http.jsonDecoder
import io.circe.generic.auto._
import org.http4s.circe._
import cats.effect.kernel.Async
import org.http4s.circe.CirceEntityDecoder.circeEntityDecoder
import cats.implicits.catsSyntaxApplicativeError

class OneFrameClient[F[_]: Async](
  client: Client[F],
  config: OneFrameConfig,
) extends Algebra[F] {

  val noParamUrl = s"http://${config.host}:${config.port}/rates?"

  override def get(pair: Rate.Pair): F[Either[OneFrameLookupFailed, Rate]] = {
    val fromCurrency : String = pair.from.toString
    val toCurrency : String = pair.to.toString
    val params: String = buildRateFetchParam(fromCurrency, toCurrency)

    val request: Request[F] = Request[F](
        Method.GET,
        uri = Uri.fromString(s"$noParamUrl$params").getOrElse(Uri()),
        headers = Headers(Header.Raw(CIString("token"), config.token))
    )

    client.expect[List[Rate]](request).attempt.map {
        case Right(rates) => Right(rates.head)
        case Left(exception) => OneFrameLookupFailed(s"Unable to obtain Rate for pair, ${pair}, from").asLeft[Rate]
    }
  }

  private def buildRateFetchParam(fromCurrency: String, toCurrency: String): String = {
      s"pair=$fromCurrency$toCurrency"
  }

}
