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
import models.{CodeList, FilterParams, VersionInformation}
import org.apache.pekko.NotUsed
import scala.collection.immutable.{Seq => ImmSeq}
import org.apache.pekko.stream.Materializer
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

        "should return OK" - {

          "when there are no query parameters" in {

            implicit val mat: Materializer = app.materializer

            val codeList = CodeList("AdditionalInformation")
            val version  = arbitrary[VersionInformation].sample.value
            lazy val url = s"/customs-reference-data/lists/${codeList.listName}"

            val fakeRequest = FakeRequest(GET, url)
              .withHeaders(ACCEPT -> "application/vnd.hmrc.1.0+json")

            val json = ImmSeq(
              Json.obj(
                "documentType" -> "C651",
                "description"  -> "Electronic administrative document (e-AD), as referred to in Article 3(1) of Reg. (EC) No 684/2009"
              ),
              Json.obj(
                "documentType" -> "C658",
                "description" -> "Fallback Document for movements of excise goods under suspension of excise duty, as referred to in Article 9(1) of Commission Delegated Regulation (EU) 2022/1636"
              )
            )

            val source: Source[JsObject, NotUsed] = Source.apply(json)

            when(mockListRetrievalService.getLatestVersion(any())).thenReturn(Future.successful(Some(version)))
            when(mockListRetrievalService.getStreamedList(any(), any(), any())).thenReturn(source)

            val result = route(app, fakeRequest).get

            val expectedJson = Json.parse(s"""
                |{
                |  "_links": {
                |    "self": {
                |      "href": "/customs-reference-data/lists/AdditionalInformation"
                |    }
                |  },
                |  "meta": {
                |    "version": "${version.versionId}",
                |    "snapshotDate": "${version.messageInformation.snapshotDate}"
                |  },
                |  "id": "AdditionalInformation",
                |  "data": [
                |    {
                |      "documentType": "C651",
                |      "description": "Electronic administrative document (e-AD), as referred to in Article 3(1) of Reg. (EC) No 684/2009"
                |    },
                |    {
                |      "documentType": "C658",
                |      "description": "Fallback Document for movements of excise goods under suspension of excise duty, as referred to in Article 9(1) of Commission Delegated Regulation (EU) 2022/1636"
                |    }
                |  ]
                |}
                |""".stripMargin)

            status(result) mustEqual OK
            contentAsJson(result) mustEqual expectedJson

            verify(mockListRetrievalService).getLatestVersion(codeList.listName)
            verify(mockListRetrievalService).getStreamedList(codeList.listName, version.versionId, None)
          }

          "when there are query parameters" in {

            implicit val mat: Materializer = app.materializer

            val codeList = CodeList("AdditionalInformation")
            val version  = arbitrary[VersionInformation].sample.value
            lazy val url = s"/customs-reference-data/lists/${codeList.listName}?data.documentType=C651"

            val filterParams = FilterParams(Seq("data.documentType" -> Seq("C651")))

            val fakeRequest = FakeRequest(GET, url)
              .withHeaders(ACCEPT -> "application/vnd.hmrc.1.0+json")

            val json = ImmSeq(
              Json.obj(
                "documentType" -> "C651",
                "description"  -> "Electronic administrative document (e-AD), as referred to in Article 3(1) of Reg. (EC) No 684/2009"
              )
            )

            val source: Source[JsObject, NotUsed] = Source.apply(json)

            when(mockListRetrievalService.getLatestVersion(any())).thenReturn(Future.successful(Some(version)))
            when(mockListRetrievalService.getStreamedList(any(), any(), any())).thenReturn(source)

            val result = route(app, fakeRequest).get

            val expectedJson = Json.parse(s"""
                |{
                |  "_links": {
                |    "self": {
                |      "href": "/customs-reference-data/lists/AdditionalInformation?data.documentType=C651"
                |    }
                |  },
                |  "meta": {
                |    "version": "${version.versionId}",
                |    "snapshotDate": "${version.messageInformation.snapshotDate}"
                |  },
                |  "id": "AdditionalInformation",
                |  "data": [
                |    {
                |      "documentType": "C651",
                |      "description": "Electronic administrative document (e-AD), as referred to in Article 3(1) of Reg. (EC) No 684/2009"
                |    }
                |  ]
                |}
                |""".stripMargin)

            status(result) mustEqual OK
            contentAsJson(result) mustEqual expectedJson

            verify(mockListRetrievalService).getLatestVersion(codeList.listName)
            verify(mockListRetrievalService).getStreamedList(codeList.listName, version.versionId, Some(filterParams))
          }
        }

        "should return NotFound when latest version returns None" in {

          val codeList = CodeList("AdditionalInformation")
          lazy val url = s"/customs-reference-data/lists/${codeList.listName}"

          val fakeRequest = FakeRequest(GET, url).withHeaders(ACCEPT -> "application/vnd.hmrc.1.0+json")

          when(mockListRetrievalService.getLatestVersion(any())).thenReturn(Future.successful(None))

          val result = route(app, fakeRequest).get

          status(result) mustEqual NOT_FOUND

          verify(mockListRetrievalService).getLatestVersion(codeList.listName)
          verify(mockListRetrievalService, never()).getStreamedList(eqTo(codeList.listName), any(), any())
        }
      }

      "when phase 6" - {

        "should return OK" - {

          "when there are no query parameters" in {

            implicit val mat: Materializer = app.materializer

            val listName = "AdditionalReference"
            val codeList = CodeList(listName)

            lazy val url = s"/customs-reference-data/lists/$listName"

            val fakeRequest = FakeRequest(GET, url)
              .withHeaders(ACCEPT -> "application/vnd.hmrc.2.0+json")

            val json =
              """
                |[
                |  {
                |    "key": "C651",
                |    "value": "Electronic administrative document (e-AD), as referred to in Article 3(1) of Reg. (EC) No 684/2009"
                |  },
                |  {
                |    "key": "C658",
                |    "value": "Fallback Document for movements of excise goods under suspension of excise duty, as referred to in Article 9(1) of Commission Delegated Regulation (EU) 2022/1636"
                |  }
                |]
                |""".stripMargin

            val source: Source[ByteString, ?] = Source.single(ByteString(json))

            when(mockConnector.get(any(), any())(any())).thenReturn(Future.successful(source))

            val result = route(app, fakeRequest).get

            val expectedJson = Json.parse(json)

            status(result) mustEqual OK
            contentAsJson(result) mustEqual expectedJson

            verifyNoInteractions(mockListRetrievalService)
            verify(mockConnector).get(eqTo(codeList), eqTo(FilterParams()))(any())
          }

          "when there are query parameters" in {

            implicit val mat: Materializer = app.materializer

            val listName = "AdditionalReference"
            val codeList = CodeList(listName)

            val filterParams = FilterParams(Seq("keys" -> Seq("C651")))

            lazy val url = s"/customs-reference-data/lists/$listName?keys=C651"

            val fakeRequest = FakeRequest(GET, url)
              .withHeaders(ACCEPT -> "application/vnd.hmrc.2.0+json")

            val json =
              """
                |[
                |  {
                |    "key": "C651",
                |    "value": "Electronic administrative document (e-AD), as referred to in Article 3(1) of Reg. (EC) No 684/2009"
                |  }
                |]
                |""".stripMargin

            val source: Source[ByteString, ?] = Source.single(ByteString(json))

            when(mockConnector.get(any(), any())(any())).thenReturn(Future.successful(source))

            val result = route(app, fakeRequest).get

            val expectedJson = Json.parse(json)

            status(result) mustEqual OK
            contentAsJson(result) mustEqual expectedJson

            verifyNoInteractions(mockListRetrievalService)
            verify(mockConnector).get(eqTo(codeList), eqTo(filterParams))(any())
          }
        }

        "should return BAD_REQUEST when code list doesn't exist" in {

          lazy val url = s"/customs-reference-data/lists/foo"

          val fakeRequest = FakeRequest(GET, url)
            .withHeaders(ACCEPT -> "application/vnd.hmrc.2.0+json")

          val result = route(app, fakeRequest).get

          status(result) mustEqual BAD_REQUEST

          verifyNoInteractions(mockListRetrievalService)
          verifyNoInteractions(mockConnector)
        }
      }
    }
  }
}
