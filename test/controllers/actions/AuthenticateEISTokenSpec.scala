/*
 * Copyright 2023 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package controllers.actions

import config.AppConfig
import config.IncomingAuthConfig
import org.mockito.Mockito.when
import org.scalacheck.Gen
import org.scalatest.Assertion
import org.scalatest.OptionValues
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks
import play.api.http.Status.UNAUTHORIZED
import play.api.mvc.BodyParsers
import play.api.mvc.Result
import play.api.test.FakeHeaders
import play.api.test.FakeRequest

import scala.concurrent.ExecutionContext.Implicits.global

class AuthenticateEISTokenSpec extends AnyFreeSpec with Matchers with OptionValues with ScalaFutures with MockitoSugar with ScalaCheckDrivenPropertyChecks {

  val enabledConfig  = IncomingAuthConfig(enabled = true, Seq("ABC", "123"))
  val disabledConfig = IncomingAuthConfig(enabled = false, Seq("ABC", "123"))

  val mockParsers = mock[BodyParsers.Default]

  "When authentication is disabled" - {

    "ensure we always return no result with no headers, allowing the action to proceed" in {
      val mockAppConfig = mock[AppConfig]
      when(mockAppConfig.incomingAuth).thenReturn(disabledConfig)
      val sut = new AuthenticateEISTokenImpl(mockAppConfig, mockParsers)

      val request = FakeRequest("POST", "/")

      whenReady[Option[Result], Assertion](sut.filter(request)) {
        _ must not be defined
      }
    }

    "ensure we always return no result with a gibberish authorisation header, allowing the action to proceed" in forAll(Gen.alphaNumStr) {
      token =>
        val mockAppConfig = mock[AppConfig]
        when(mockAppConfig.incomingAuth).thenReturn(disabledConfig)
        val sut = new AuthenticateEISTokenImpl(mockAppConfig, mockParsers)

        val request = FakeRequest("POST", "/", FakeHeaders(Seq("Authentication" -> token)), "")

        whenReady[Option[Result], Assertion](sut.filter(request)) {
          _ must not be defined
        }
    }

    "ensure we always return no result with a good authorisation header, allowing the action to proceed" in {
      val mockAppConfig = mock[AppConfig]
      when(mockAppConfig.incomingAuth).thenReturn(disabledConfig)
      val sut = new AuthenticateEISTokenImpl(mockAppConfig, mockParsers)

      val request = FakeRequest("POST", "/", FakeHeaders(Seq("Authorization" -> "Bearer ABC")), "")

      whenReady[Option[Result], Assertion](sut.filter(request)) {
        _ must not be defined
      }
    }

  }

  "When authentication is enabled" - {

    "ensure we always return unauthorised with no headers" in {
      val mockAppConfig = mock[AppConfig]
      when(mockAppConfig.incomingAuth).thenReturn(enabledConfig)
      val sut = new AuthenticateEISTokenImpl(mockAppConfig, mockParsers)

      val request = FakeRequest("POST", "/")

      whenReady[Option[Result], Assertion](sut.filter(request)) {
        case Some(value) => value.header.status mustEqual UNAUTHORIZED
        case None        => fail("Should have returned a result")
      }
    }

    "ensure we always return unauthorized with a gibberish authorisation header" in forAll(Gen.alphaNumStr) {
      token =>
        val mockAppConfig = mock[AppConfig]
        when(mockAppConfig.incomingAuth).thenReturn(enabledConfig)
        val sut = new AuthenticateEISTokenImpl(mockAppConfig, mockParsers)

        val request = FakeRequest("POST", "/", FakeHeaders(Seq("Authorization" -> token)), "")

        whenReady[Option[Result], Assertion](sut.filter(request)) {
          case Some(value) => value.header.status mustEqual UNAUTHORIZED
          case None        => fail("Should have returned a result")
        }
    }

    "ensure we always return unauthorized with a bad authorisation header" in forAll(Gen.alphaNumStr.map(_.toLowerCase)) {
      token =>
        val mockAppConfig = mock[AppConfig]
        when(mockAppConfig.incomingAuth).thenReturn(enabledConfig)
        val sut = new AuthenticateEISTokenImpl(mockAppConfig, mockParsers)

        val request = FakeRequest("POST", "/", FakeHeaders(Seq("Authorization" -> s"Bearer $token")), "")

        whenReady[Option[Result], Assertion](sut.filter(request)) {
          case Some(value) => value.header.status mustEqual UNAUTHORIZED
          case None        => fail("Should have returned a result")
        }
    }

    "ensure we always return no result with a good authorisation header, allowing the action to proceed" in {
      val mockAppConfig = mock[AppConfig]
      when(mockAppConfig.incomingAuth).thenReturn(enabledConfig)
      val sut = new AuthenticateEISTokenImpl(mockAppConfig, mockParsers)

      val request = FakeRequest("POST", "/", FakeHeaders(Seq("Authorization" -> "Bearer ABC")), "")

      whenReady[Option[Result], Assertion](sut.filter(request)) {
        _ must not be defined
      }
    }

  }

}
