import play.core.PlayVersion._
import sbt._

object AppDependencies {

  private val mongoVersion = "1.3.0"
  private val bootstrapVersion = "7.19.0"
  private val akkaVersion = "2.6.20"

  val compile: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"         %% "bootstrap-backend-play-28"       % bootstrapVersion,
    "uk.gov.hmrc.mongo"   %% "hmrc-mongo-play-28"              % mongoVersion,
    "org.leadpony.justify" % "justify"                         % "3.1.0",
    "org.leadpony.joy"     % "joy-classic"                     % "2.1.0",
    "org.typelevel"       %% "cats-core"                       % "2.9.0"
  )

  val test: Seq[ModuleID] = Seq(
    "uk.gov.hmrc.mongo"      %% "hmrc-mongo-test-play-28"  % mongoVersion,
    "org.scalatest"          %% "scalatest"                % "3.2.15",
    "uk.gov.hmrc"            %% "bootstrap-test-play-28"   % bootstrapVersion,
    "com.typesafe.play"      %% "play-test"                % current,
    "org.mockito"             % "mockito-core"             % "5.2.0",
    "org.scalatestplus"      %% "mockito-4-6"              % "3.2.15.0",
    "org.scalacheck"         %% "scalacheck"               % "1.17.0",
    "org.scalatestplus"      %% "scalacheck-1-17"          % "3.2.15.0",
    "io.github.wolfendale"   %% "scalacheck-gen-regexp"    % "1.1.0",
    "org.pegdown"             % "pegdown"                  % "1.6.0",
    "org.jsoup"               % "jsoup"                    % "1.15.3",
    "com.typesafe.akka"      %% "akka-stream-testkit"      % akkaVersion,
    "com.vladsch.flexmark"    % "flexmark-all"             % "0.62.2"
  ).map(_ % "test, it")

  def apply(): Seq[ModuleID] = compile ++ test
}
