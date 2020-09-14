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
import generators.ModelArbitraryInstances
import models.ResourceLinks
import org.mockito.Mockito.when
import org.scalacheck.Arbitrary.arbitrary
import org.scalatest.TestData
import org.scalatestplus.play.guice.GuiceOneAppPerTest
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.Application
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.ListRetrievalService

import scala.concurrent.Future

class ResourceLinksControllerSpec extends SpecBase with GuiceOneAppPerTest with ScalaCheckPropertyChecks with ModelArbitraryInstances {

  private val fakeRequest = FakeRequest(GET, routes.ResourceLinksController.get().url)

  private val mockListRetrievalService = mock[ListRetrievalService]

  override def newAppForTest(testData: TestData): Application =
    new GuiceApplicationBuilder()
      .overrides(bind[ListRetrievalService].toInstance(mockListRetrievalService))
      .build()

  "ResourceLinksController" - {

    "resourceLinks" - {

      "should return OK and reference data links" in {

        forAll(arbitrary[ResourceLinks]) {
          resourceLinks =>
            when(mockListRetrievalService.getResourceLinks())
              .thenReturn(Future.successful(Some(resourceLinks)))

            val result = route(app, fakeRequest).get

            status(result) mustBe OK
            contentType(result).get mustBe "application/json"
        }
      }

      "should return 500 when reference data links are unavailable" in {

        when(mockListRetrievalService.getResourceLinks())
          .thenReturn(Future.successful(None))

        val result = route(app, fakeRequest).get

        status(result) mustBe INTERNAL_SERVER_ERROR
      }
    }
  }

}
