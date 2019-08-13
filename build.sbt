import sbtrelease.ReleaseStateTransformations._

name := "secrets-manager-mock"

lazy val commonSettings =
  Seq(organization := "com.loyalty.testing", scalaVersion := "2.12.8")

scalacOptions += "-Ypartial-unification"

resolvers ++= Seq(
  Resolver.bintrayRepo("hseeberger", "maven")
)

lazy val root = (project in file("."))
  .configs(IntegrationTest)
  .settings(
    commonSettings,
    Defaults.itSettings,
    name := "secrets-manager-mock",
    resolvers += Resolver.jcenterRepo,
    fork in Test := true,
    parallelExecution in IntegrationTest := false,
    libraryDependencies ++= {

      val akkaVersion = "2.5.23"
      val akkaHttpVersion = "10.1.9"
      val circeVersion = "0.11.1"
      val akkaHttpCirceVersion = "1.27.0"
      val scalaXmlVersion = "1.1.1"
      val EnumeratumVersion = "1.5.13"
      val EnumeratumCirceVersion = "1.5.20"
      val NitriteVersion = "3.2.0"

      val akka = "com.typesafe.akka"
      val scalaTest = "org.scalatest"
      val scalaTestMock = "org.scalamock"
      val scalacheck = "org.scalacheck"
      val slf4j = "org.slf4j"
      val circe = "io.circe"
      val heikoseeberger = "de.heikoseeberger"
      val logback = "ch.qos.logback"
      val scalaLangModules = "org.scala-lang.modules"
      val Beachape = "com.beachape"
      val Enumeratum = "enumeratum"
      val EnumeratumCirce = "enumeratum-circe"
      val Dizitart = "org.dizitart"

      Seq(
        akka             %% "akka-http"                         % akkaHttpVersion,
        akka             %% "akka-http-xml"                     % akkaHttpVersion,
        akka 				     %% "akka-stream" 					            % akkaVersion,
        slf4j            %  "jcl-over-slf4j"                    % "1.7.25",
        logback          %  "logback-classic"                   % "1.2.3",
        circe            %% "circe-core"                        % circeVersion,
        circe            %% "circe-generic"                     % circeVersion,
        circe            %% "circe-parser"                      % circeVersion,
        circe            %% "circe-java8"                       % circeVersion,
        Beachape         %% Enumeratum                          % EnumeratumVersion,
        Beachape         %% EnumeratumCirce                     % EnumeratumCirceVersion,
        heikoseeberger   %% "akka-http-circe"                   % akkaHttpCirceVersion,
        scalaLangModules %% "scala-xml"                         % scalaXmlVersion,
        Dizitart         %  "nitrite"                           % NitriteVersion,
        akka             %% "akka-testkit"                      % akkaVersion                 % "it, test",
        scalaTest        %% "scalatest"                         % "3.0.8"                     % "it, test",
        scalacheck       %% "scalacheck"                        % "1.13.5"                    % "it, test",
        scalaTestMock    %% "scalamock-scalatest-support"       % "3.6.0"                     % "it, test",
        akka 				     %% "akka-http-testkit" 				        % akkaHttpVersion             % Test
      )
    }
  )
  .enablePlugins(JavaAppPackaging)

releaseIgnoreUntrackedFiles := true

releaseProcess := Seq[ReleaseStep](
  checkSnapshotDependencies,
  inquireVersions,
  setReleaseVersion,
  commitReleaseVersion,
  tagRelease,
  releaseStepTask(publish in Docker),
  setNextVersion,
  commitNextVersion,
  pushChanges
)

dockerRepository := Some("sfali23")
dockerBaseImage := "openjdk:8-jre-slim"
