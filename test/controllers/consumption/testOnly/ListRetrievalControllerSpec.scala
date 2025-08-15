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
import connectors.CrdlCacheConnector
import generators.ModelArbitraryInstances
import models.CodeList.RefDataCodeList
import models.{FilterParams, ListName}
import org.apache.pekko.stream.Materializer
import org.apache.pekko.stream.scaladsl.Source
import org.apache.pekko.util.ByteString
import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito.*
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

import scala.concurrent.Future
import scala.util.{Failure, Success}

class ListRetrievalControllerSpec extends SpecBase with GuiceOneAppPerTest with ScalaCheckPropertyChecks with ModelArbitraryInstances with BeforeAndAfterEach {

  private val mockListRetrievalService = mock[ListRetrievalService]
  private val mockCrdlCacheConnector   = mock[CrdlCacheConnector]

  override def newAppForTest(testData: TestData): Application =
    new GuiceApplicationBuilder()
      .overrides(
        bind[ListRetrievalService].toInstance(mockListRetrievalService),
        bind[CrdlCacheConnector].toInstance(mockCrdlCacheConnector)
      )
      .configure("play.http.router" -> "testOnlyDoNotUseInAppConf.Routes")
      .build()

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockListRetrievalService)
    reset(mockCrdlCacheConnector)
  }

  "TestOnlyListRetrievalController" - {

    "get" - {

      "when phase 5" - {

        "should return OK when get returns a Success" - {

          "when there are no filter params" in {

            val listName = ListName("AdditionalReference")
            val codeList = RefDataCodeList(listName, "CL380")

            lazy val url = s"/customs-reference-data/test-only/lists/$listName"

            val fakeRequest = FakeRequest(GET, url)
              .withHeaders(ACCEPT -> "application/vnd.hmrc.1.0+json")

            val json = Json
              .parse("""
                |[
                |  {
                |    "documentType": "C651",
                |    "description": "Electronic administrative document (e-AD), as referred to in Article 3(1) of Reg. (EC) No 684/2009"
                |  },
                |  {
                |    "documentType": "C658",
                |    "description": "Fallback Document for movements of excise goods under suspension of excise duty, as referred to in Article 9(1) of Commission Delegated Regulation (EU) 2022/1636"
                |  }
                |]
                |""".stripMargin)
              .as[JsArray]

            when(mockListRetrievalService.get(any(), any())).thenReturn(Success(json))

            val result = route(app, fakeRequest).get

            val expectedJson = Json.parse("""
                |{
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

            verify(mockListRetrievalService).get(eqTo(codeList), eqTo(None))
            verifyNoInteractions(mockCrdlCacheConnector)
          }

          "when there are filter params" in {

            val listName = ListName("AdditionalReference")
            val codeList = RefDataCodeList(listName, "CL380")

            val filterParams = FilterParams(Seq("data.documentType" -> Seq("C651")))

            lazy val url = s"/customs-reference-data/test-only/lists/$listName?data.documentType=C651"

            val fakeRequest = FakeRequest(GET, url)
              .withHeaders(ACCEPT -> "application/vnd.hmrc.1.0+json")

            val json = Json
              .parse("""
                |[
                |  {
                |    "documentType": "C651",
                |    "description": "Electronic administrative document (e-AD), as referred to in Article 3(1) of Reg. (EC) No 684/2009"
                |  }
                |]
                |""".stripMargin)
              .as[JsArray]

            when(mockListRetrievalService.get(any(), any())).thenReturn(Success(json))

            val result = route(app, fakeRequest).get

            val expectedJson = Json
              .parse("""
                |{
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

            verify(mockListRetrievalService).get(eqTo(codeList), eqTo(Some(filterParams)))
            verifyNoInteractions(mockCrdlCacheConnector)
          }
        }

        "should return NotFound when get returns a Failure" in {

          val listName = ListName("AdditionalReference")
          val codeList = RefDataCodeList(listName, "CL380")

          lazy val url = s"/customs-reference-data/test-only/lists/$listName"

          val fakeRequest = FakeRequest(GET, url)
            .withHeaders(ACCEPT -> "application/vnd.hmrc.1.0+json")

          when(mockListRetrievalService.get(any(), any())).thenReturn(Failure(new Throwable("")))

          val result = route(app, fakeRequest).get

          status(result) mustEqual NOT_FOUND

          verify(mockListRetrievalService).get(eqTo(codeList), eqTo(None))
          verifyNoInteractions(mockCrdlCacheConnector)
        }
      }

      "when phase 6" - {

        "should return OK when get returns a Success" - {

          "when there are no filter params" in {

            implicit val mat: Materializer = app.materializer

            val listName = ListName("AdditionalReference")
            val codeList = RefDataCodeList(listName, "CL380")

            lazy val url = s"/customs-reference-data/test-only/lists/$listName"

            val fakeRequest = FakeRequest(GET, url)
              .withHeaders(ACCEPT -> "application/vnd.hmrc.2.0+json")

            val json = Json.parse("""
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
                |""".stripMargin)

            val source: Source[ByteString, ?] = Source.single(ByteString(Json.stringify(json)))

            when(mockCrdlCacheConnector.get(any(), any())(any())).thenReturn(Future.successful(source))

            val result = route(app, fakeRequest).get

            status(result) mustEqual OK
            contentAsJson(result) mustEqual json

            verifyNoInteractions(mockListRetrievalService)
            verify(mockCrdlCacheConnector).get(eqTo(codeList), eqTo(FilterParams()))(any())
          }

          "when there are filter params" in {

            implicit val mat: Materializer = app.materializer

            val listName = ListName("AdditionalReference")
            val codeList = RefDataCodeList(listName, "CL380")

            val filterParams = FilterParams(Seq("keys" -> Seq("C651")))

            lazy val url = s"/customs-reference-data/test-only/lists/$listName?keys=C651"

            val fakeRequest = FakeRequest(GET, url)
              .withHeaders(ACCEPT -> "application/vnd.hmrc.2.0+json")

            val json = Json.parse("""
                |[
                |  {
                |    "key": "C651",
                |    "value": "Electronic administrative document (e-AD), as referred to in Article 3(1) of Reg. (EC) No 684/2009"
                |  }
                |]
                |""".stripMargin)

            val source: Source[ByteString, ?] = Source.single(ByteString(Json.stringify(json)))

            when(mockCrdlCacheConnector.get(any(), any())(any())).thenReturn(Future.successful(source))

            val result = route(app, fakeRequest).get

            status(result) mustEqual OK
            contentAsJson(result) mustEqual json

            verifyNoInteractions(mockListRetrievalService)
            verify(mockCrdlCacheConnector).get(eqTo(codeList), eqTo(filterParams))(any())
          }
        }
      }
    }
  }
}
