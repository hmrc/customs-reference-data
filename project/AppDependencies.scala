import play.core.PlayVersion.current
import sbt._

object AppDependencies {

  val compile = Seq(
    "uk.gov.hmrc"       %% "bootstrap-backend-play-27"       % "2.23.0",
    "org.reactivemongo" %% "play2-reactivemongo"             % "0.20.11-play27",
    "com.typesafe.play" %% "play-iteratees"                  % "2.6.1",
    "com.typesafe.play" %% "play-iteratees-reactive-streams" % "2.6.1"
  )

  val test = Seq(
    "org.scalatest"          %% "scalatest"                % "3.2.0",
    "org.scalatestplus"      %% "mockito-3-2"              % "3.1.2.0",
    "org.scalatestplus.play" %% "scalatestplus-play"       % "3.1.3",
    "org.scalatestplus"      %% "scalatestplus-scalacheck" % "3.1.0.0-RC2",
    "org.pegdown"             % "pegdown"                  % "1.6.0",
    "org.jsoup"               % "jsoup"                    % "1.10.3",
    "com.typesafe.play"      %% "play-test"                % current,
    "org.mockito"             % "mockito-core"             % "3.3.3",
    "org.scalacheck"         %% "scalacheck"               % "1.14.3",
    "wolfendale"             %% "scalacheck-gen-regexp"    % "0.1.1",
    "com.vladsch.flexmark"    % "flexmark-all"             % "0.35.10"
  ).map(_ % "test, it")

  def apply(): Seq[ModuleID] = compile ++ test
}
