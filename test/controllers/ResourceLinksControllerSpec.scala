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

import base.SpecBase
import org.scalatestplus.play.guice.GuiceOneAppPerTest
import play.api.test.FakeRequest
import play.api.test.Helpers._

class ResourceLinksControllerSpec extends SpecBase with GuiceOneAppPerTest {

  private val fakeRequest = FakeRequest(GET, "/customs-reference-data/resources").withHeaders("Host" -> "localhost")

  "ResourceLinksController" - {

    "GET" - {

      "should return OK and correct content type" in {

        val result = route(app, fakeRequest).get

        status(result) mustBe OK
        contentType(result).get mustBe "application/json"

      }

    }

  }

}
