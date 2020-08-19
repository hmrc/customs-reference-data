import play.core.PlayVersion.current
import sbt._

object AppDependencies {

  val compile = Seq(
    "uk.gov.hmrc"       %% "bootstrap-backend-play-27"       % "2.23.0",
    "org.reactivemongo" %% "play2-reactivemongo"             % "0.20.11-play26",
    "com.typesafe.play" %% "play-iteratees"                  % "2.6.1",
    "com.typesafe.play" %% "play-iteratees-reactive-streams" % "2.6.1"
  )

  val test = Seq(
    "uk.gov.hmrc"            %% "bootstrap-test-play-27" % "2.23.0",
    "com.typesafe.play"      %% "play-test"              % current,
    "org.scalatest"          %% "scalatest"              % "3.2.0",
    "org.scalatestplus.play" %% "scalatestplus-play"     % "3.1.3",
    "com.vladsch.flexmark"    % "flexmark-all"           % "0.35.10",
    "org.scalatestplus"      %% "mockito-3-2"            % "3.1.2.0",
    "com.github.tomakehurst"  % "wiremock-standalone"    % "2.27.1",
    "org.scalacheck"          %% "scalacheck"            % "1.14.0"
  ).map(_ % "test, it")
}
