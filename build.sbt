
lazy val commonSettings = Seq(
  name := "telegram-bot-webhook",
  version := "1.0-SNAPSHOT",
  scalaVersion := "2.13.4",
  scalacOptions ++= Seq(
    "-deprecation",
    "-Xfatal-warnings",
    "-Ywarn-value-discard",
    "-Xlint:missing-interpolator",
    "-Ymacro-annotations",
    "-unchecked",
    "-Wunused:imports"
  ),
)

lazy val Http4sVersion = "0.21.1"
lazy val ScaffeineVersion = "4.0.2"
lazy val RomeVersion = "1.15.0"
lazy val CirceVersion = "0.13.0"
lazy val DoobieVersion = "0.9.4"
lazy val PostgreSQLJDBCDriverVersion = "42.2.18"
lazy val FlywayVersion = "7.3.2"
lazy val PureConfigVersion = "0.12.3"
lazy val LogbackVersion = "1.2.3"
lazy val LogbackGCPVersion = "0.119.0-alpha"
lazy val ScalaLoggingVersion = "3.9.2"
lazy val ScalaTestVersion = "3.1.1"


lazy val root = (project in file("."))
  .configs(IntegrationTest)
  .settings(
    commonSettings,
    Defaults.itSettings,
    libraryDependencies ++= Seq(
      "org.http4s" %% "http4s-blaze-server" % Http4sVersion,
      "org.http4s" %% "http4s-circe" % Http4sVersion,
      "org.http4s" %% "http4s-dsl" % Http4sVersion,
      "org.http4s" %% "http4s-blaze-client" % Http4sVersion % "it,test",

      "com.rometools" % "rome" % RomeVersion,

      "com.github.blemale" %% "scaffeine" % ScaffeineVersion,

      "io.circe" %% "circe-generic" % CirceVersion,
      "io.circe" %% "circe-generic-extras" % CirceVersion,
      "io.circe" %% "circe-literal" % CirceVersion % "test",
      "io.circe" %% "circe-parser" % CirceVersion % "test",

      "org.tpolecat" %% "doobie-core" % DoobieVersion,
      "org.tpolecat" %% "doobie-hikari" % DoobieVersion,
      "org.tpolecat" %% "doobie-postgres" % DoobieVersion,

      "org.postgresql" % "postgresql" % PostgreSQLJDBCDriverVersion,

      "org.flywaydb" % "flyway-core" % FlywayVersion,

      "com.github.pureconfig" %% "pureconfig" % PureConfigVersion,
      "com.github.pureconfig" %% "pureconfig-cats-effect" % PureConfigVersion,

      "com.typesafe.scala-logging" %% "scala-logging" % ScalaLoggingVersion,
      "ch.qos.logback" % "logback-classic" % LogbackVersion,
      "com.google.cloud" % "google-cloud-logging-logback"% LogbackGCPVersion,

      "org.scalatest" %% "scalatest" % ScalaTestVersion % "it,test",
    )
  )

assemblyMergeStrategy in assembly := {
  case "META-INF/io.netty.versions.properties" => MergeStrategy.last
  case "module-info.class" => MergeStrategy.discard
  case x =>
    val oldStrategy = (assemblyMergeStrategy in assembly).value
    oldStrategy(x)
}