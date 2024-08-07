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
import scala.collection.mutable.ListBuffer

class OneFrameClient[F[_]: Async](
  client: Client[F],
  config: OneFrameConfig,
) extends Algebra[F] {

  val noParamUrl = s"http://${config.host}:${config.port}/rates?"

  override def get(pair: Rate.Pair): F[Either[OneFrameLookupFailed, Rate]] = {
    val fromCurrency : Currency = pair.from
    val toCurrency : Currency = pair.to
    val params: String = buildRateFetchParam(fromCurrency, toCurrency)

    val request: Request[F] = Request[F](
        Method.GET,
        uri = Uri.fromString(s"$noParamUrl$params").getOrElse(Uri()),
        headers = Headers(Header.Raw(CIString("token"), config.token))
    )

    client.expect[List[Rate]](request).attempt.map {
        case Right(rates) => Right(rates.head)
        case Left(exception) => OneFrameLookupFailed(s"Unable to obtain Rate for pair, ${pair}").asLeft[Rate]
    }
  }

  override def getAll(): F[Either[OneFrameLookupFailed, List[Rate]]] = {
    val currencyPairs: List[List[Currency]] = buildAllCurrencyPairs()

    var params: String = ""
    for (currencyPair <- currencyPairs) {
      params = params.concat(buildRateFetchParam(currencyPair.head, currencyPair.last))
    }

    val request: Request[F] = Request[F](
        Method.GET,
        uri = Uri.fromString(s"$noParamUrl" + params).getOrElse(Uri()),
        headers = Headers(Header.Raw(CIString("token"), config.token))
    )

    client.expect[List[Rate]](request).attempt.map {
        case Right(rates) => Right(rates)
        case Left(exception) => {
          OneFrameLookupFailed(s"Unable to obtain Rate for all pairs, $exception")
            .asLeft[List[Rate]]
        }
    }
  }

  private def buildRateFetchParam(fromCurrency: Currency, toCurrency: Currency): String = {
      s"pair=${fromCurrency.toString}${toCurrency.toString}&"
  }

  // obtains all Currency values and returns all possible permutation pairs
  private def buildAllCurrencyPairs(): List[List[Currency]] = {
    val currencyList: List[Currency] = Currency.values.toList
    val currencyPairCombinations: List[Set[Currency]] = currencyList.combinations(2).map(_.toSet).toList
    var currencyPairPermutations: ListBuffer[List[Currency]] = new ListBuffer[List[Currency]]
    for (currencySet <- currencyPairCombinations) {
      for (currencyPair <- currencySet.toList.permutations.toList) {
        currencyPairPermutations += currencyPair
      }
    }
    currencyPairPermutations.toList
  }

}
