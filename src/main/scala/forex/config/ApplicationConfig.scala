package forex.config

import pureconfig.ConfigReader
import pureconfig.generic.derivation.ConfigReaderDerivation.Default.derived
import scala.concurrent.duration.FiniteDuration

case class ApplicationConfig(
  http: HttpConfig,
  oneFrame: OneFrameConfig
) derives ConfigReader

case class HttpConfig(
  host: String,
  port: Int,
  timeout: FiniteDuration,
)

case class OneFrameConfig(
  host: String,
  port: Int,
  token: String
)
