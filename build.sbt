import play.sbt.routes.RoutesKeys
import scoverage.ScoverageKeys
import uk.gov.hmrc.sbtdistributables.SbtDistributablesPlugin.publishingSettings

val appName = "customs-reference-data"

val silencerVersion = "1.7.1"

lazy val microservice = Project(appName, file("."))
  .enablePlugins(play.sbt.PlayScala, SbtAutoBuildPlugin, SbtDistributablesPlugin)
  .disablePlugins(JUnitXmlReportPlugin)
  .settings(resolvers += Resolver.jcenterRepo)
  .settings(publishingSettings: _*)
  .settings(inConfig(Test)(testSettings): _*)
  .configs(IntegrationTest)
  .settings(RoutesKeys.routesImport += "models._")
  .settings(inConfig(IntegrationTest)(itSettings): _*)
  .settings(scoverageSettings: _*)
  .settings(
    // ***************
    // Use the silencer plugin to suppress warnings
    scalacOptions += "-P:silencer:pathFilters=routes",
    libraryDependencies ++= Seq(
      compilerPlugin("com.github.ghik" % "silencer-plugin" % silencerVersion cross CrossVersion.full),
      "com.github.ghik" % "silencer-lib" % silencerVersion % Provided cross CrossVersion.full
    )
    // ***************
  )
  .settings(
    majorVersion := 0,
    scalaVersion := "2.12.11",
    PlayKeys.playDefaultPort := 9492,
    libraryDependencies ++= AppDependencies.compile ++ AppDependencies.test,
    useSuperShell in ThisBuild := false,
    scalacOptions += "-Ypartial-unification",
    scalafmtOnCompile in ThisBuild := true
  )

lazy val scoverageSettings =
  Seq(
    ScoverageKeys.coverageExcludedPackages := """uk\.gov\.hmrc\.BuildInfo*;.*\.Routes;.*\.RoutesPrefix;.*\.Reverse[^.]*;testonly;config.*""",
    ScoverageKeys.coverageMinimum := 85.00,
    ScoverageKeys.coverageExcludedFiles := "<empty>;.*javascript.*;.*Routes.*;",
    ScoverageKeys.coverageFailOnMinimum := true,
    ScoverageKeys.coverageHighlighting := true,
    parallelExecution in Test := false
  )

lazy val testSettings = Seq(
  fork := true,
  javaOptions ++= Seq(
    "-Dconfig.resource=test.application.conf",
    "-Dlogger.resource=logback-test.xml"
  ),
  unmanagedResourceDirectories := Seq(
    baseDirectory.value / "test" / "resources"
  )
)

lazy val itSettings = Defaults.itSettings ++ Seq(
  unmanagedSourceDirectories := Seq(
    baseDirectory.value / "it"
  ),
  unmanagedResourceDirectories := Seq(
    baseDirectory.value / "it" / "resources"
  ),
  unmanagedSourceDirectories += baseDirectory.value / "test" / "generators",
  parallelExecution := false,
  fork := true,
  javaOptions ++= Seq(
    "-Dconfig.resource=it.application.conf",
    "-Dlogger.resource=logback-it.xml"
  )
)
