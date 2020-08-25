package base

import org.scalatest.OptionValues
import org.scalatest.concurrent.IntegrationPatience
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.mockito.MockitoSugar
import play.api.inject.guice.GuiceApplicationBuilder

class ItSpecBase extends AnyFreeSpec with Matchers with OptionValues with ScalaFutures with MockitoSugar {

  type AppFunction = GuiceApplicationBuilder => GuiceApplicationBuilder

  val baseApplicationBuilder: AppFunction = identity

}
