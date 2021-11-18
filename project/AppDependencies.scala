import play.core.PlayVersion
import play.core.PlayVersion.current
import sbt._

object AppDependencies {

  private val catsVersion = "2.1.1"

  val compile: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"         %% "bootstrap-backend-play-28"       % "5.14.0",
    "org.reactivemongo"   %% "play2-reactivemongo"             % "0.20.13-play28",
    "com.typesafe.play"   %% "play-iteratees"                  % "2.6.1",
    "com.typesafe.play"   %% "play-iteratees-reactive-streams" % "2.6.1",
    "org.leadpony.justify" % "justify"                         % "3.1.0",
    "org.leadpony.joy"     % "joy-classic"                     % "2.1.0",
    "org.typelevel"       %% "cats-core"                       % catsVersion
  )

  val test: Seq[ModuleID] = Seq(
    "org.scalatest"          %% "scalatest"                % "3.2.0",
    "org.scalatestplus"      %% "mockito-3-2"              % "3.1.2.0",
    "org.scalatestplus.play" %% "scalatestplus-play"       % "5.1.0",
    "org.scalatestplus"      %% "scalatestplus-scalacheck" % "3.1.0.0-RC2",
    "org.pegdown"             % "pegdown"                  % "1.6.0",
    "org.jsoup"               % "jsoup"                    % "1.10.3",
    "com.typesafe.play"      %% "play-test"                % current,
    "org.mockito"             % "mockito-core"             % "3.3.3",
    "org.scalacheck"         %% "scalacheck"               % "1.14.3",
    "wolfendale"             %% "scalacheck-gen-regexp"    % "0.1.2",
    "com.vladsch.flexmark"    % "flexmark-all"             % "0.35.10",
    "com.typesafe.akka"      %% "akka-stream-testkit"      % PlayVersion.akkaVersion,
    "com.typesafe.akka"      %% "akka-slf4j"               % PlayVersion.akkaVersion
  ).map(_ % "test, it")

  def apply(): Seq[ModuleID] = compile ++ test
}
