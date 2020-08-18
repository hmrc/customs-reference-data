/*
 * Copyright 2020 HM Revenue & Customs
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

package controllers

import akka.util.ByteString
import base.SpecBase
import play.api.http.Status
import play.api.libs.json.JsValue
import play.api.libs.json.Json
import play.api.libs.streams.Accumulator
import play.api.mvc.Action
import play.api.mvc.AnyContentAsJson
import play.api.mvc.Result
import play.api.test.FakeRequest
import play.api.test.Helpers._

import scala.concurrent.Future

class ReferenceDataControllerSpec extends SpecBase {

  val withTestOnlyRouter: AppFunction =
    baseApplicationBuilder.andThen(_.configure("play.http.router" -> "testOnlyDoNotUseInAppConf.Routes"))

  private def fakeRequest: FakeRequest[AnyContentAsJson] =
    FakeRequest(POST, routes.ReferenceDataController.post().url)
      .withJsonBody(Json.obj())

  "post" - {
    "returns Ok for a JSON body" in {
      running(withTestOnlyRouter) {
        application =>
          val result = route(application, fakeRequest).value

          status(result) mustBe Status.OK
      }
    }
  }
}
