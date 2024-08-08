package forex.services.rates.interpreters

import cats.effect.kernel.Async
import cats.implicits.{ catsSyntaxEitherId, catsSyntaxApplicativeError, catsSyntaxApplicativeId, toFunctorOps }
import cats.syntax.all.toArrowChoiceOps
import forex.config.OneFrameConfig
import forex.domain.{ Currency, Price, Rate, Timestamp }
import forex.http.jsonDecoder
import forex.services.rates.Algebra
import forex.services.rates.errors.Error
import forex.services.rates.errors.Error.OneFrameLookupFailed
import org.http4s.{ Headers, Header, Method, Request, Response, Uri }
import org.http4s.circe.CirceEntityDecoder.circeEntityDecoder
import org.http4s.client.Client
import org.http4s.implicits.uri
import org.typelevel.ci.CIString

/** Client to call One-Frame service to obtain [[Rate]] information.
  *
  * @param client
  * @param config
  */
class OneFrameClient[F[_]: Async](client: Client[F], config: OneFrameConfig) extends Algebra[F] {

  val noParamUrl = s"http://${config.host}:${config.port}/rates?"

  override def get(pair: Rate.Pair): F[Either[OneFrameLookupFailed, Rate]] = {
    val params: String = buildRateFetchParam(pair.from, pair.to)

    val request: Request[F] = buildRequest(params)

    client.expect[List[Rate]](request).attempt.map {
        case Right(rates) => Right(rates.head)
        case Left(exception) => OneFrameLookupFailed(s"Unable to obtain Rate for pair, ${pair}").asLeft[Rate]
    }
  }

  override def getAll(): F[Either[OneFrameLookupFailed, List[Rate]]] = {
    // using JPY as default currency intermediary
    val defaultFromCurrency: Currency = Currency.JPY
    // obtain all currencies other than the default intermediary
    val currencies: List[Currency] = Currency.values.toList.filter(_ != defaultFromCurrency)

    var params: String = ""
    for (toCurrency <- currencies) {
      params = params.concat(buildRateFetchParam(defaultFromCurrency, toCurrency))
    }

    val request: Request[F] = buildRequest(params)

    client.expect[List[Rate]](request).attempt.map {
        case Right(rates) => {
          Right(rates)
        }
        case Left(exception) => {
          OneFrameLookupFailed(s"Unable to obtain Rate from One-Frame, $exception")
            .asLeft[List[Rate]]
        }
    }
  }

  private def buildRateFetchParam(fromCurrency: Currency, toCurrency: Currency): String = {
      s"pair=${fromCurrency.toString}${toCurrency.toString}&"
  }

  private def buildRequest(params: String): Request[F] = {
    Request[F](
        Method.GET,
        uri = Uri.fromString(s"$noParamUrl$params").getOrElse(Uri()),
        headers = Headers(Header.Raw(CIString("token"), config.token))
    )
  }

}
