import sbt._

object Dependencies {

  object Versions {
    val cats = "2.12.0"
    val catsEffect = "3.5.4"
    val circe = "0.14.9"
    val fs2 = "3.10.2"
    val http4s = "0.23.27"
    val http4sBlaze = "0.23.16"
    val pureConfig = "0.17.7"
    val logback = "1.5.6"
    val log4cats = "2.7.0"

    val scalaCheck = "1.18.0"
    val scalaTest = "3.2.19"
    val catsScalaCheck = "0.3.2"
    val scalaTestMockito = "3.2.19.0"
  }

  object Libraries {
    def circe(artifact: String): ModuleID = "io.circe" %% artifact % Versions.circe
    def http4s(artifact: String): ModuleID = "org.http4s" %% artifact % Versions.http4s
    def http4sBlaze(artifact: String): ModuleID = "org.http4s" %% artifact % Versions.http4sBlaze

    lazy val cats = "org.typelevel" %% "cats-core" % Versions.cats
    lazy val catsEffect = "org.typelevel" %% "cats-effect" % Versions.catsEffect
    lazy val circeCore = circe("circe-core")
    lazy val circeGeneric = circe("circe-generic")
    lazy val circeParser = circe("circe-parser")
    lazy val fs2 = "co.fs2" %% "fs2-core" % Versions.fs2
    lazy val http4sCirce = http4s("http4s-circe")
    lazy val http4sClient = http4sBlaze("http4s-blaze-client")
    lazy val http4sDsl = http4s("http4s-dsl")
    lazy val http4sServer = http4sBlaze("http4s-blaze-server")
    lazy val log4cats = "org.typelevel" %% "log4cats-slf4j" % Versions.log4cats
    lazy val logback = "ch.qos.logback" % "logback-classic" % Versions.logback
    lazy val pureConfig = "com.github.pureconfig" %% "pureconfig-core" % Versions.pureConfig

    // Test
    lazy val catsScalaCheck = "io.chrisdavenport" %% "cats-scalacheck" % Versions.catsScalaCheck
    lazy val circeTesting = circe("circe-testing")
    lazy val scalaCheck = "org.scalacheck" %% "scalacheck" % Versions.scalaCheck
    lazy val scalaTest = "org.scalatest" %% "scalatest" % Versions.scalaTest
    lazy val scalaTestMockito = "org.scalatestplus" %% "mockito-5-12" % Versions.scalaTestMockito
  }

}
