val Http4sVersion = "0.18.22"
val Specs2Version = "4.1.0"
val LogbackVersion = "1.2.3"
val doobieVersion = "0.5.4"
val jsonSchemaValidatorVersion = "2.2.11"
val circeVersion = "0.12.3"
val circeJacksonVersion = "0.12.0"

lazy val root = (project in file("."))
  .settings(
    organization := "gclaramunt",
    name := "snowplow-json-validator",
    version := "0.0.1-SNAPSHOT",
    scalaVersion := "2.12.8",
    libraryDependencies ++= Seq(

      "com.github.java-json-tools" % "json-schema-validator" % jsonSchemaValidatorVersion,

      "org.http4s"      %% "http4s-blaze-server" % Http4sVersion,
      "org.http4s"      %% "http4s-circe"        % Http4sVersion,
      "org.http4s"      %% "http4s-dsl"          % Http4sVersion,

      "io.circe"        %% "circe-core"     % circeVersion,
      "io.circe"        %% "circe-generic"  % circeVersion,
      "io.circe"        %% "circe-parser"   % circeVersion,

      "io.circe"        %% "circe-jackson29"  % circeJacksonVersion,

      "org.tpolecat" %% "doobie-core"     % doobieVersion,
      "com.h2database" % "h2" % "1.4.197",
      "org.tpolecat" %% "doobie-h2"        % doobieVersion,

      "org.typelevel" %% "cats-effect" % "0.10.1",

      "org.specs2"     %% "specs2-core"          % Specs2Version % "test",
      "org.tpolecat" %% "doobie-specs2"    % doobieVersion % "test", // Specs2 support for typechecking statements.
      "ch.qos.logback"  %  "logback-classic"     % LogbackVersion
    ),
    addCompilerPlugin("org.spire-math" %% "kind-projector"     % "0.9.6"),
    addCompilerPlugin("com.olegpy"     %% "better-monadic-for" % "0.2.4")
  )

scalacOptions += "-Ypartial-unification"

  /*
  val startH2Task = TaskKey[Unit]("start-h2", "Starts H2 DB")
  val stopH2Task = TaskKey[Unit]("stop-h2", "Stops H2 DB")
  val h2tasks:Seq[Setting[_]] = Seq(startH2Task := {
    org.h2.tools.Server.createTcpServer().start();
    org.h2.tools.Server.createWebServer().start    // this starts the "web tool"
  }, stopH2Task :={
    org.h2.tools.Server.shutdownTcpServer("tcp://localhost:9092","",true,true);
} )
*/