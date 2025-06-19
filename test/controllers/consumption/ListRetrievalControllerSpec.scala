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

package controllers.consumption

import base.SpecBase
import connectors.CrdlCacheConnector
import generators.ModelArbitraryInstances
import models.{FilterParams, ListName, ReferenceDataList, VersionInformation}
import org.apache.pekko.NotUsed
import org.apache.pekko.stream.scaladsl.Source
import org.apache.pekko.util.ByteString
import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito.*
import org.scalacheck.Arbitrary.arbitrary
import org.scalatest.{BeforeAndAfterEach, TestData}
import org.scalatestplus.play.guice.GuiceOneAppPerTest
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.Application
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.{JsObject, Json}
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import services.consumption.ListRetrievalService

import scala.concurrent.Future

class ListRetrievalControllerSpec extends SpecBase with GuiceOneAppPerTest with ScalaCheckPropertyChecks with ModelArbitraryInstances with BeforeAndAfterEach {

  private val mockListRetrievalService = mock[ListRetrievalService]

  private val mockConnector = mock[CrdlCacheConnector]

  override def newAppForTest(testData: TestData): Application =
    new GuiceApplicationBuilder()
      .overrides(
        bind[ListRetrievalService].toInstance(mockListRetrievalService),
        bind[CrdlCacheConnector].toInstance(mockConnector)
      )
      .build()

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockListRetrievalService)
    reset(mockConnector)
  }

  "ListRetrievalController" - {

    "get" - {

      "when phase 5" - {

        "should return OK" in {

          val referenceDataList = arbitrary[ReferenceDataList].sample.value
          val version           = arbitrary[VersionInformation].sample.value
          lazy val url          = s"/customs-reference-data/lists/${referenceDataList.id.listName}"

          val fakeRequest = FakeRequest(GET, url)
            .withHeaders(ACCEPT -> "application/vnd.hmrc.1.0+json")

          val source: Source[JsObject, NotUsed] = Source(1 to 4).map(
            _ => Json.obj("index" -> "value")
          )

          when(mockListRetrievalService.getLatestVersion(any())).thenReturn(Future.successful(Some(version)))
          when(mockListRetrievalService.getStreamedList(any(), any(), any())).thenReturn(source)

          val result = route(app, fakeRequest).get

          status(result) mustEqual OK

          verify(mockListRetrievalService).getLatestVersion(referenceDataList.id)
          verify(mockListRetrievalService).getStreamedList(referenceDataList.id, version.versionId, None)
        }

        "should return NotFound when latest version returns None" in {

          val referenceDataList = arbitrary[ReferenceDataList].sample.value
          lazy val url          = s"/customs-reference-data/lists/${referenceDataList.id.listName}"

          val fakeRequest = FakeRequest(GET, url).withHeaders(ACCEPT -> "application/vnd.hmrc.1.0+json")

          val source: Source[JsObject, NotUsed] = Source(1 to 4).map(
            _ => Json.obj("index" -> "value")
          )

          when(mockListRetrievalService.getLatestVersion(any())).thenReturn(Future.successful(None))
          when(mockListRetrievalService.getStreamedList(any(), any(), any())).thenReturn(source)

          val result = route(app, fakeRequest).get

          status(result) mustEqual NOT_FOUND

          verify(mockListRetrievalService).getLatestVersion(referenceDataList.id)
          verify(mockListRetrievalService, never()).getStreamedList(eqTo(referenceDataList.id), any(), any())
        }
      }

      "when phase 6" - {

        "should return OK" - {

          "when there are no query parameters" in {

            val listName = ListName("AdditionalReference")

            lazy val url = s"/customs-reference-data/lists/$listName"

            val fakeRequest = FakeRequest(GET, url)
              .withHeaders(ACCEPT -> "application/vnd.hmrc.2.0+json")

            val source: Source[ByteString, NotUsed] = Source(1 to 4).map(
              i => ByteString.fromString(i.toString)
            )

            when(mockConnector.get(any(), any())(any())).thenReturn(Future.successful(source))

            val result = route(app, fakeRequest).get

            status(result) mustEqual OK

            verifyNoInteractions(mockListRetrievalService)
            verify(mockConnector).get(eqTo(listName.code.value), eqTo(FilterParams()))(any())
          }

          "when there are query parameters" in {

            val listName     = ListName("AdditionalReference")
            val filterParams = FilterParams(Seq("keys" -> Seq("00200")))

            lazy val url = s"/customs-reference-data/lists/$listName?keys=00200"

            val fakeRequest = FakeRequest(GET, url)
              .withHeaders(ACCEPT -> "application/vnd.hmrc.2.0+json")

            val source: Source[ByteString, NotUsed] = Source(1 to 4).map(
              i => ByteString.fromString(i.toString)
            )

            when(mockConnector.get(any(), any())(any())).thenReturn(Future.successful(source))

            val result = route(app, fakeRequest).get

            status(result) mustEqual OK

            verifyNoInteractions(mockListRetrievalService)
            verify(mockConnector).get(eqTo(listName.code.value), eqTo(filterParams))(any())
          }
        }

        "should return BAD_REQUEST when code list doesn't exist" in {

          val listName = ListName("foo")

          lazy val url = s"/customs-reference-data/lists/$listName"

          val fakeRequest = FakeRequest(GET, url)
            .withHeaders(ACCEPT -> "application/vnd.hmrc.2.0+json")

          val source: Source[ByteString, NotUsed] = Source(1 to 4).map(
            i => ByteString.fromString(i.toString)
          )

          when(mockConnector.get(any(), any())(any())).thenReturn(Future.successful(source))

          val result = route(app, fakeRequest).get

          status(result) mustEqual BAD_REQUEST

          verifyNoInteractions(mockListRetrievalService)
          verifyNoInteractions(mockConnector)
        }
      }
    }
  }
}
