
lazy val commonSettings = Seq(
  name := "telegram-bot-webhook",
  version := "1.0-SNAPSHOT",
  scalaVersion := "2.13.1",
  scalacOptions ++= Seq(
    "-deprecation",
    "-Xfatal-warnings",
    "-Ywarn-value-discard",
    "-Xlint:missing-interpolator",
    "-Ymacro-annotations",
    "-unchecked"
  ),
)

lazy val Http4sVersion = "0.21.1"

//lazy val DoobieVersion = "0.8.8"

lazy val H2Version = "1.4.200"

lazy val FlywayVersion = "6.3.1"

lazy val CirceVersion = "0.13.0"

lazy val PureConfigVersion = "0.12.3"

lazy val LogbackVersion = "1.2.3"

//lazy val Log4CatsVersion = "1.1.1"
lazy val ScalaLoggingVersion = "3.9.2"

lazy val ScalaTestVersion = "3.1.1"

lazy val ScalaMockVersion = "4.4.0"

lazy val RomeVersion = "1.15.0"

lazy val root = (project in file("."))
  .configs(IntegrationTest)
  .settings(
    commonSettings,
    Defaults.itSettings,
    libraryDependencies ++= Seq(
      "org.http4s"            %% "http4s-blaze-server"  % Http4sVersion,
      "org.http4s"            %% "http4s-circe"         % Http4sVersion,
      "org.http4s"            %% "http4s-dsl"           % Http4sVersion,
      "org.http4s"            %% "http4s-blaze-client"  % Http4sVersion     % "it,test",

//      "io.chrisdavenport" %% "log4cats-core"            % Log4CatsVersion,
//      "io.chrisdavenport" %% "log4cats-slf4j"           % Log4CatsVersion,
      "com.typesafe.scala-logging" %% "scala-logging"   % ScalaLoggingVersion,

//      "org.tpolecat"          %% "doobie-core"          % DoobieVersion,
//      "org.tpolecat"          %% "doobie-h2"            % DoobieVersion,
//      "org.tpolecat"          %% "doobie-hikari"        % DoobieVersion,
//
//      "com.h2database"        %  "h2"                   % H2Version,

      "org.flywaydb"          %  "flyway-core"          % FlywayVersion,

      "io.circe"              %% "circe-generic"        % CirceVersion,
      "io.circe"              %% "circe-generic-extras" % CirceVersion,
      "io.circe"              %% "circe-literal"        % CirceVersion      % "it,test",
      "io.circe"              %% "circe-optics"         % CirceVersion      % "it",

      "com.github.pureconfig" %% "pureconfig"             % PureConfigVersion,
      "com.github.pureconfig" %% "pureconfig-cats-effect" % PureConfigVersion,

      "ch.qos.logback"        %  "logback-classic"      % LogbackVersion,


      "com.rometools" % "rome" % RomeVersion,

      "org.scalatest"         %% "scalatest"            % ScalaTestVersion  % "it,test",
      "org.scalamock"         %% "scalamock"            % ScalaMockVersion  % "test"
    )
  )

assemblyMergeStrategy in assembly := {
  case "META-INF/io.netty.versions.properties" => MergeStrategy.last
  case x =>
    val oldStrategy = (assemblyMergeStrategy in assembly).value
    oldStrategy(x)
}