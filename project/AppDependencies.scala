import sbt.*

object AppDependencies {

  private val mongoVersion = "2.6.0"
  private val bootstrapVersion = "9.14.0"
  private val pekkoVersion = "1.1.4"

  val compile: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"         %% "bootstrap-backend-play-30"       % bootstrapVersion,
    "uk.gov.hmrc.mongo"   %% "hmrc-mongo-play-30"              % mongoVersion,
    "org.leadpony.justify" % "justify"                         % "3.1.0",
    "org.leadpony.joy"     % "joy-classic"                     % "2.1.0",
    "org.typelevel"       %% "cats-core"                       % "2.13.0",
    "org.apache.commons"   % "commons-text"                    % "1.13.1"
  )

  val test: Seq[ModuleID] = Seq(
    "uk.gov.hmrc.mongo"      %% "hmrc-mongo-test-play-30"  % mongoVersion,
    "org.scalatest"          %% "scalatest"                % "3.2.19",
    "uk.gov.hmrc"            %% "bootstrap-test-play-30"   % bootstrapVersion,
    "org.mockito"             % "mockito-core"             % "5.18.0",
    "org.scalatestplus"      %% "mockito-4-11"             % "3.2.18.0",
    "org.scalacheck"         %% "scalacheck"               % "1.18.1",
    "org.scalatestplus"      %% "scalacheck-1-17"          % "3.2.18.0",
    "io.github.wolfendale"   %% "scalacheck-gen-regexp"    % "1.1.0",
    "org.jsoup"               % "jsoup"                    % "1.21.1",
    "org.apache.pekko"       %% "pekko-testkit"            % pekkoVersion,
    "org.apache.pekko"       %% "pekko-stream-testkit"     % pekkoVersion
  ).map(_ % "test")

  val overrides: Seq[ModuleID] = Seq(
    "org.apache.pekko" %% "pekko-actor-typed" % pekkoVersion,
    "org.apache.pekko" %% "pekko-serialization-jackson" % pekkoVersion,
    "org.apache.pekko" %% "pekko-slf4j" % pekkoVersion
  )
}
