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

import base.SpecBase
import org.scalatest.Assertion
import org.scalatest.freespec.AnyFreeSpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.http.Status.*
import play.api.mvc.Result
import play.api.test.FakeHeaders
import play.api.test.FakeRequest

class ValidateAcceptHeaderSpec extends SpecBase with GuiceOneAppPerSuite {

  "ensure we return BadRequest when no Accept header" in {
    val sut = app.injector.instanceOf[ValidateAcceptHeaderImpl]

    val request = FakeRequest("POST", "/")

    whenReady[Option[Result], Assertion](sut.filter(request)) {
      _.value.header.status mustEqual BAD_REQUEST
    }
  }

  "ensure we return BadRequest when v1 Accept header" in {
    val sut = app.injector.instanceOf[ValidateAcceptHeaderImpl]

    val request = FakeRequest("POST", "/", FakeHeaders(Seq("Accept" -> "application/vnd.hmrc.1.0+gzip")), "")

    whenReady[Option[Result], Assertion](sut.filter(request)) {
      _.value.header.status mustEqual BAD_REQUEST
    }
  }

  "ensure we allow action to proceed when a valid Accept header" - {
    "when data is compressed" in {
      val sut = app.injector.instanceOf[ValidateAcceptHeaderImpl]

      val request = FakeRequest("POST", "/", FakeHeaders(Seq("Accept" -> "application/vnd.hmrc.2.0+gzip")), "")

      whenReady[Option[Result], Assertion](sut.filter(request)) {
        _ must not be defined
      }
    }

    "when data is uncompressed" in {
      val sut = app.injector.instanceOf[ValidateAcceptHeaderImpl]

      val request = FakeRequest("POST", "/", FakeHeaders(Seq("Accept" -> "application/vnd.hmrc.2.0+json")), "")

      whenReady[Option[Result], Assertion](sut.filter(request)) {
        _ must not be defined
      }
    }
  }
}
