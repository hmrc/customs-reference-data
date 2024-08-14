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

package controllers.ingestion.v2.testOnly

import base.SpecBase
import models.ApiDataSource.ColDataFeed
import models.OtherError
import models.WriteError
import org.mockito.ArgumentMatchers.{eq => eqTo, _}
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import play.api.http.Status
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import play.api.mvc.AnyContentAsXml
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.ingestion.v2.ReferenceDataService
import utils.XmlToJsonConverter.CustomsOfficeListXmlToJsonConverter

import scala.concurrent.Future

class TestOnlyCustomsOfficeListControllerSpec extends SpecBase with GuiceOneAppPerSuite with BeforeAndAfterEach {

  private val mockXmlToJsonConverter: CustomsOfficeListXmlToJsonConverter = mock[CustomsOfficeListXmlToJsonConverter]

  private val mockReferenceDataService: ReferenceDataService = mock[ReferenceDataService]

  private val testXml =
    <foo>
      bar
    </foo>

  private val testJson = Json.obj("foo" -> "bar")

  private val headers = Seq(
    "Accept" -> "application/vnd.hmrc.2.0+gzip"
  )

  "post" - {
    def fakeRequest: FakeRequest[AnyContentAsXml] =
      FakeRequest(POST, "/customs-reference-data/test-only/customs-office-lists")
        .withXmlBody(testXml)
        .withHeaders(headers: _*)

    "returns ACCEPTED when the data has been validated and processed" in {

      when(mockXmlToJsonConverter.convert(eqTo(testXml))).thenReturn(testJson)
      when(mockReferenceDataService.validate(any(), eqTo(testJson))).thenReturn(Right(testJson))
      when(mockReferenceDataService.insert(eqTo(ColDataFeed), any())).thenReturn(Future.successful(None))

      val result = route(app, fakeRequest).value

      status(result) mustBe Status.ACCEPTED
    }

    "returns Bad Request when a validation error occurs" in {

      when(mockXmlToJsonConverter.convert(eqTo(testXml))).thenReturn(testJson)
      when(mockReferenceDataService.validate(any(), any())).thenReturn(Left(OtherError("error")))

      val result = route(app, fakeRequest).value

      status(result) mustBe Status.BAD_REQUEST
      contentAsJson(result) mustBe Json.toJsObject(OtherError("error"))
    }

    "returns with an Internal Server Error when the has been validated but data was not processed successfully" in {

      when(mockXmlToJsonConverter.convert(eqTo(testXml))).thenReturn(testJson)
      when(mockReferenceDataService.validate(any(), any())).thenReturn(Right(testJson))
      when(mockReferenceDataService.insert(eqTo(ColDataFeed), any())).thenReturn(Future.successful(Some(WriteError("error"))))

      val result = route(app, fakeRequest).value

      status(result) mustBe Status.INTERNAL_SERVER_ERROR
      contentAsJson(result) mustBe Json.toJsObject(WriteError("error"))
    }
  }

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockXmlToJsonConverter)
    reset(mockReferenceDataService)
  }

  // Do not use directly use `app` instead
  override def fakeApplication(): Application =
    new GuiceApplicationBuilder()
      .configure("play.http.router" -> "testOnlyDoNotUseInAppConf.Routes")
      .overrides(
        bind[CustomsOfficeListXmlToJsonConverter].toInstance(mockXmlToJsonConverter),
        bind[ReferenceDataService].toInstance(mockReferenceDataService)
      )
      .build()
}
