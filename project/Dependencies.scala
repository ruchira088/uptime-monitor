import sbt._

object Dependencies {
  val ScalaVersion = "3.2.0"
  val Http4sVersion = "0.23.16"
  val CirceVersion = "0.14.3"
  val TestContainersVersion = "1.17.5"

  lazy val http4sDsl = "org.http4s" %% "http4s-dsl" % Http4sVersion

  lazy val http4sEmberServer = "org.http4s" %% "http4s-ember-server" % Http4sVersion

  lazy val http4sEmberClient = "org.http4s" %% "http4s-ember-client" % Http4sVersion

  lazy val http4sCirce = "org.http4s" %% "http4s-circe" % Http4sVersion

  lazy val circeGeneric = "io.circe" %% "circe-generic" % CirceVersion

  lazy val circeParser = "io.circe" %% "circe-parser" % CirceVersion

  lazy val circeLiteral = "io.circe" %% "circe-literal" % CirceVersion

  lazy val doobie = "org.tpolecat" %% "doobie-core" % "1.0.0-RC2"

  lazy val doobieHikari = "org.tpolecat" %% "doobie-hikari" % "1.0.0-RC2"

  lazy val postgresql = "org.postgresql" % "postgresql" % "42.5.0"

  lazy val h2 = "com.h2database" % "h2" % "2.1.214"

  lazy val flywayCore = "org.flywaydb" % "flyway-core" % "9.6.0"

  lazy val catsEffect = "org.typelevel" %% "cats-effect" % "3.3.14"

  lazy val redis4CatsEffects = "dev.profunktor" %% "redis4cats-effects" % "1.2.0"

  lazy val jbcrypt = "org.mindrot" % "jbcrypt" % "0.4"

  lazy val testContainers = "org.testcontainers" % "testcontainers" % TestContainersVersion

  lazy val postgresqlTestContainer = "org.testcontainers" % "postgresql" % TestContainersVersion

  lazy val embeddedRedis = "com.github.kstyrc" % "embedded-redis" % "0.6"

  lazy val jodaTime = "joda-time" % "joda-time" % "2.12.0"

  lazy val pureconfig = "com.github.pureconfig" %% "pureconfig-core" % "0.17.1"

  lazy val logbackClassic = "ch.qos.logback" % "logback-classic" % "1.4.4"

  lazy val scalaLogging = "com.typesafe.scala-logging" %% "scala-logging" % "3.9.5"

  lazy val scalaTest = "org.scalatest" %% "scalatest" % "3.2.14"

  lazy val scalaTestPlusMockito = "org.scalatestplus" %% "mockito-4-6" % "3.2.14.0"

  lazy val pegdown = "org.pegdown" % "pegdown" % "1.6.0"
}
