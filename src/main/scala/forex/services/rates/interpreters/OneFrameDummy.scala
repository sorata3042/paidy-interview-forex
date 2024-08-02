package forex.services.rates.interpreters

import cats.Applicative
import cats.syntax.applicative.catsSyntaxApplicativeId
import cats.syntax.either.catsSyntaxEitherId
import forex.domain.{ Price, Rate, Timestamp }
import forex.services.rates.Algebra
import forex.services.rates.errors.Error

class OneFrameDummy[F[_]: Applicative] extends Algebra[F] {

  override def get(pair: Rate.Pair): F[Error Either Rate] =
    Rate(pair, Price(BigDecimal(100)), Timestamp.now).asRight[Error].pure[F]

}
