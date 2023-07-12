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

package controllers.consumption.v1

import akka.NotUsed
import akka.stream.scaladsl.Source
import base.SpecBase
import generators.ModelArbitraryInstances
import models.ReferenceDataList
import models.VersionInformation
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.{eq => eqTo}
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.Mockito.when
import org.scalacheck.Arbitrary.arbitrary
import org.scalatest.TestData
import org.scalatestplus.play.guice.GuiceOneAppPerTest
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.Application
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.JsObject
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers.GET
import play.api.test.Helpers.route
import play.api.test.Helpers.status
import play.api.test.Helpers._
import services.consumption.v1.ListRetrievalService

import scala.concurrent.Future

class ListRetrievalControllerSpec extends SpecBase with GuiceOneAppPerTest with ScalaCheckPropertyChecks with ModelArbitraryInstances {

  private val mockListRetrievalService = mock[ListRetrievalService]

  override def newAppForTest(testData: TestData): Application =
    new GuiceApplicationBuilder()
      .overrides(bind[ListRetrievalService].toInstance(mockListRetrievalService))
      .build()

  "ListRetrievalController" - {

    "get" - {

      "should return OK" in {

        val referenceDataList = arbitrary[ReferenceDataList].sample.value
        val version           = arbitrary[VersionInformation].sample.value
        val url               = s"/customs-reference-data/lists/${referenceDataList.id.listName}"

        val fakeRequest = FakeRequest(GET, url).withHeaders(ACCEPT -> "application/vnd.hmrc.1.0+json")

        val source: Source[JsObject, NotUsed] = Source(1 to 4).map(_ => Json.obj("index" -> "value"))

        when(mockListRetrievalService.getLatestVersion(any())).thenReturn(Future.successful(Some(version)))
        when(mockListRetrievalService.getStreamedList(any(), any())).thenReturn(source)

        val result = route(app, fakeRequest).get

        status(result) mustBe OK

        verify(mockListRetrievalService).getLatestVersion(referenceDataList.id)
        verify(mockListRetrievalService).getStreamedList(referenceDataList.id, version.versionId)
      }

      "should return NotFound when latest version returns None" in {

        val referenceDataList = arbitrary[ReferenceDataList].sample.value
        val url               = s"/customs-reference-data/lists/${referenceDataList.id.listName}"

        val fakeRequest = FakeRequest(GET, url).withHeaders(ACCEPT -> "application/vnd.hmrc.1.0+json")

        val source: Source[JsObject, NotUsed] = Source(1 to 4).map(_ => Json.obj("index" -> "value"))

        when(mockListRetrievalService.getLatestVersion(any())).thenReturn(Future.successful(None))
        when(mockListRetrievalService.getStreamedList(any(), any())).thenReturn(source)

        val result = route(app, fakeRequest).get

        status(result) mustBe NOT_FOUND

        verify(mockListRetrievalService).getLatestVersion(referenceDataList.id)
        verify(mockListRetrievalService, never()).getStreamedList(eqTo(referenceDataList.id), any())
      }
    }
  }
}
