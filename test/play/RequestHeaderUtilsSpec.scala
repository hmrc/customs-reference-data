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

package play

import base.SpecBase
import generators.BaseGenerators
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks.forAll
import play.api.test.FakeRequest
import play.api.test.Helpers._

class RequestHeaderUtilsSpec extends SpecBase with GuiceOneAppPerSuite with BaseGenerators {

  private val util = app.injector.instanceOf[RequestHeaderUtils]

  "isRequestUnversioned" - {
    "must return true" - {
      "when URL starts with /api" in {
        val requestHeader = FakeRequest(GET, "/api/foo")
        val result        = util.isRequestUnversioned(requestHeader)
        result mustBe true
      }

      "when URL starts with /admin" in {
        val requestHeader = FakeRequest(GET, "/admin/foo")
        val result        = util.isRequestUnversioned(requestHeader)
        result mustBe true
      }

      "when URL starts with /ping" in {
        val requestHeader = FakeRequest(GET, "/ping/foo")
        val result        = util.isRequestUnversioned(requestHeader)
        result mustBe true
      }
    }

    "must return false" - {
      "when URL starts with something else" in {
        forAll(nonEmptyString) {
          context =>
            val requestHeader = FakeRequest(GET, s"/$context/foo")
            val result        = util.isRequestUnversioned(requestHeader)
            result mustBe false
        }
      }
    }
  }

  "getVersionedRequest" - {
    "when request has no version header" - {
      "must add version 2 routing" - {
        "when receiving gzip from EIS" in {
          val requestHeader = FakeRequest(POST, "/")

          val result = util.getVersionedRequest(requestHeader)
          result.uri mustBe "/v2.0"
          result.path mustBe "/v2.0"
        }

        "when JSON requested from frontend" in {
          val requestHeader = FakeRequest(POST, "/")

          val result = util.getVersionedRequest(requestHeader)
          result.uri mustBe "/v2.0"
          result.path mustBe "/v2.0"
        }
      }
    }

    "when request has version 2 header" - {
      "must add version 2 routing" - {
        "when receiving gzip from EIS" in {
          val requestHeader = FakeRequest(POST, "/foo/bar")
            .withHeaders(ACCEPT -> "application/vnd.hmrc.2.0+gzip")

          val result = util.getVersionedRequest(requestHeader)
          result.uri mustBe "/v2.0/foo/bar"
          result.path mustBe "/v2.0/foo/bar"
        }

        "when JSON requested from frontend" in {
          val requestHeader = FakeRequest(POST, "/foo/bar")
            .withHeaders(ACCEPT -> "application/vnd.hmrc.2.0+json")

          val result = util.getVersionedRequest(requestHeader)
          result.uri mustBe "/v2.0/foo/bar"
          result.path mustBe "/v2.0/foo/bar"
        }
      }
    }
  }
}
