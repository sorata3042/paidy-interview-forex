package forex.config

import pureconfig.ConfigReader
import pureconfig.generic.derivation.ConfigReaderDerivation.Default.derived
import scala.concurrent.duration.FiniteDuration

case class ApplicationConfig(
  cache: CacheConfig,
  http: HttpConfig,
  oneFrame: OneFrameConfig
) derives ConfigReader

case class CacheConfig(
  expireAfter: FiniteDuration
)

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
