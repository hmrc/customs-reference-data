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

package controllers.consumption.testOnly

import base.SpecBase
import generators.ModelArbitraryInstances
import models.ListName
import models.Phase.*
import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito.*
import org.scalacheck.Arbitrary.arbitrary
import org.scalatest.{BeforeAndAfterEach, TestData}
import org.scalatestplus.play.guice.GuiceOneAppPerTest
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.Application
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.{JsArray, Json}
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import services.consumption.testOnly.ListRetrievalService

import scala.util.{Failure, Success}

class ListRetrievalControllerSpec extends SpecBase with GuiceOneAppPerTest with ScalaCheckPropertyChecks with ModelArbitraryInstances with BeforeAndAfterEach {

  private val mockListRetrievalService = mock[ListRetrievalService]

  override def newAppForTest(testData: TestData): Application =
    new GuiceApplicationBuilder()
      .overrides(
        bind[ListRetrievalService].toInstance(mockListRetrievalService)
      )
      .configure("play.http.router" -> "testOnlyDoNotUseInAppConf.Routes")
      .build()

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockListRetrievalService)
  }

  "TestOnlyListRetrievalController" - {

    "get" - {

      "when phase 5" - {

        "should return OK when get returns a Success" in {

          val listName = arbitrary[ListName].sample.value

          lazy val url = s"/customs-reference-data/test-only/lists/$listName"

          val fakeRequest = FakeRequest(GET, url)
            .withHeaders(ACCEPT -> "application/vnd.hmrc.1.0+json")

          val json = JsArray(
            Seq(
              Json.obj("foo" -> "bar")
            )
          )

          when(mockListRetrievalService.get(any(), any(), any())).thenReturn(Success(json))

          val result = route(app, fakeRequest).get

          status(result) mustEqual OK
          contentAsJson(result) mustEqual Json.obj("data" -> json)

          verify(mockListRetrievalService).get(eqTo(listName.listName), eqTo(Phase5), eqTo(None))
        }

        "should return NotFound when get returns a Failure" in {

          val listName = arbitrary[ListName].sample.value

          lazy val url = s"/customs-reference-data/test-only/lists/$listName"

          val fakeRequest = FakeRequest(GET, url)
            .withHeaders(ACCEPT -> "application/vnd.hmrc.1.0+json")

          when(mockListRetrievalService.get(any(), any(), any())).thenReturn(Failure(new Throwable("")))

          val result = route(app, fakeRequest).get

          status(result) mustEqual NOT_FOUND

          verify(mockListRetrievalService).get(eqTo(listName.listName), eqTo(Phase5), eqTo(None))
        }
      }

      "when phase 6" - {

        "should return OK when get returns a Success" in {

          val listName = arbitrary[ListName].sample.value

          lazy val url = s"/customs-reference-data/test-only/lists/$listName"

          val fakeRequest = FakeRequest(GET, url)
            .withHeaders(ACCEPT -> "application/vnd.hmrc.2.0+json")

          val json = JsArray(
            Seq(
              Json.obj("foo" -> "bar")
            )
          )

          when(mockListRetrievalService.get(any(), any(), any())).thenReturn(Success(json))

          val result = route(app, fakeRequest).get

          status(result) mustEqual OK
          contentAsJson(result) mustEqual json

          verify(mockListRetrievalService).get(eqTo(listName.listName), eqTo(Phase6), eqTo(None))
        }

        "should return NotFound when get returns a Failure" in {

          val listName = arbitrary[ListName].sample.value

          lazy val url = s"/customs-reference-data/test-only/lists/$listName"

          val fakeRequest = FakeRequest(GET, url)
            .withHeaders(ACCEPT -> "application/vnd.hmrc.2.0+json")

          when(mockListRetrievalService.get(any(), any(), any())).thenReturn(Failure(new Throwable("")))

          val result = route(app, fakeRequest).get

          status(result) mustEqual NOT_FOUND

          verify(mockListRetrievalService).get(eqTo(listName.listName), eqTo(Phase6), eqTo(None))
        }
      }
    }
  }
}
