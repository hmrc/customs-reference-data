import sbt.*

object AppDependencies {

  private val mongoVersion     = "2.12.0"
  private val bootstrapVersion = "10.7.0"
  private val pekkoVersion     = "1.0.3"

  val compile: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"         %% "bootstrap-backend-play-30"       % bootstrapVersion,
    "uk.gov.hmrc.mongo"   %% "hmrc-mongo-play-30"              % mongoVersion,
    "org.leadpony.justify" % "justify"                         % "3.1.0",
    "org.leadpony.joy"     % "joy-classic"                     % "2.1.0",
    "org.typelevel"       %% "cats-core"                       % "2.13.0",
    "org.apache.commons"   % "commons-text"                    % "1.15.0"
  )

  val test: Seq[ModuleID] = Seq(
    "uk.gov.hmrc.mongo"      %% "hmrc-mongo-test-play-30"  % mongoVersion,
    "org.scalatest"          %% "scalatest"                % "3.2.20",
    "uk.gov.hmrc"            %% "bootstrap-test-play-30"   % bootstrapVersion,
    "org.mockito"             % "mockito-core"             % "5.23.0",
    "org.scalatestplus"      %% "mockito-4-11"             % "3.2.18.0",
    "org.scalacheck"         %% "scalacheck"               % "1.19.0",
    "org.scalatestplus"      %% "scalacheck-1-17"          % "3.2.18.0",
    "io.github.wolfendale"   %% "scalacheck-gen-regexp"    % "1.1.0",
    "org.jsoup"               % "jsoup"                    % "1.22.1",
    "org.apache.pekko"       %% "pekko-testkit"            % pekkoVersion,
    "org.apache.pekko"       %% "pekko-stream-testkit"     % pekkoVersion
  ).map(_ % "test")
}
