package forex.programs.rates

import cats.Functor
import cats.data.EitherT
import cats.implicits.catsSyntaxEitherId
import cats.implicits.catsSyntaxApplicativeId
import forex.programs.rates.errors.Error
import forex.programs.rates.errors.toProgramError
import forex.domain.{ Price, Rate, Timestamp }
import forex.services.RatesService
import fs2.Compiler.Target.forSync
import cats.Applicative

class Program[F[_]: Applicative](
    ratesService: RatesService[F]
) extends Algebra[F] {

  override def get(request: Protocol.GetRatesRequest): F[Either[Error, Rate]] =
    val pair = Rate.Pair(request.from, request.to)
    if (pair.from == pair.to) {
      return Rate(Rate.Pair(request.from, request.to), Price(1), Timestamp.now)
        .asRight[Error]
        .pure[F]
    }

    EitherT(ratesService.get(pair)).leftMap(toProgramError(_)).value
}

object Program {

  def apply[F[_]: Applicative](
      ratesService: RatesService[F]
  ): Algebra[F] = new Program[F](ratesService)

}
