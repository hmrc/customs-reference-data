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
import models.Phase.*
import models.request.VersionedRequest
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.mvc.*
import play.api.mvc.Results.BadRequest
import play.api.test.FakeRequest
import play.api.test.Helpers.GET
import sttp.model.HeaderNames.Accept

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class VersionedActionSpec extends SpecBase with GuiceOneAppPerSuite {

  private class Harness(override val parser: BodyParsers.Default) extends VersionedActionImpl(parser) {

    def callRefine[A](request: Request[A]): Future[Either[Result, VersionedRequest[A]]] =
      refine(request)
  }

  private val fakeRequest = FakeRequest(GET, "")

  "VersionedAction" - {

    "when 1.0 header" - {
      "must return request with version 1.0" in {
        val parser = app.injector.instanceOf[BodyParsers.Default]
        val action = new Harness(parser)

        val request = fakeRequest.withHeaders(Accept -> "application/vnd.hmrc.1.0+json")
        val result  = action.callRefine(request).futureValue

        result.value mustEqual VersionedRequest(request, Phase5)
      }
    }

    "when 2.0 header" - {
      "must return request with version 2.0" in {
        val parser = app.injector.instanceOf[BodyParsers.Default]
        val action = new Harness(parser)

        val request = fakeRequest.withHeaders(Accept -> "application/vnd.hmrc.2.0+json")
        val result  = action.callRefine(request).futureValue

        result.value mustEqual VersionedRequest(request, Phase6)
      }
    }

    "when undefined header" - {
      "must return request with version 1.0" in {
        val parser = app.injector.instanceOf[BodyParsers.Default]
        val action = new Harness(parser)

        val request = fakeRequest
        val result  = action.callRefine(request).futureValue

        result.value mustEqual VersionedRequest(request, Phase5)
      }
    }

    "when invalid version" - {
      "must return bad request" in {
        val parser = app.injector.instanceOf[BodyParsers.Default]
        val action = new Harness(parser)

        val request = fakeRequest.withHeaders(Accept -> "application/vnd.hmrc.foo+json")
        val result  = action.callRefine(request).futureValue

        result.left.value mustEqual BadRequest("Accept header contains an invalid version 'foo'")
      }
    }

    "when random header" - {
      "must return request with version 1.0" in {
        val parser = app.injector.instanceOf[BodyParsers.Default]
        val action = new Harness(parser)

        val request = fakeRequest.withHeaders(Accept -> "foo")
        val result  = action.callRefine(request).futureValue

        result.value mustEqual VersionedRequest(request, Phase5)
      }
    }
  }
}
