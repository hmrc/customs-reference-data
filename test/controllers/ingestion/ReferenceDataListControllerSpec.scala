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

package controllers.ingestion

import base.SpecBase
import models.ApiDataSource.RefDataFeed
import models.OtherError
import models.MongoError
import org.mockito.ArgumentMatchers.{eq => eqTo, _}
import org.mockito.Mockito
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import play.api.http.Status
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import play.api.mvc.AnyContentAsJson
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.ingestion.ReferenceDataService

import scala.concurrent.Future

class ReferenceDataListControllerSpec extends SpecBase with GuiceOneAppPerSuite with BeforeAndAfterEach {

  val mockReferenceDataService: ReferenceDataService = mock[ReferenceDataService]

  private val testJson = Json.obj("foo" -> "bar")

  private val headers = Seq(
    "Accept"        -> "application/vnd.hmrc.2.0+gzip",
    "Authorization" -> "Bearer ABC"
  )

  "referenceDataLists" - {

    def fakeRequest: FakeRequest[AnyContentAsJson] =
      FakeRequest(POST, "/customs-reference-data/reference-data-lists")
        .withJsonBody(testJson)
        .withHeaders(headers*)

    "returns ACCEPTED when the data has been validated and processed" in {
      when(mockReferenceDataService.validate(any(), any())).thenReturn(Right(testJson))
      when(mockReferenceDataService.insert(eqTo(RefDataFeed), any())).thenReturn(Future.successful(Right(())))

      val result = route(app, fakeRequest).value

      status(result) mustEqual Status.ACCEPTED
    }

    "returns Bad Request when a validation error occurs" in {
      when(mockReferenceDataService.validate(any(), any())).thenReturn(Left(OtherError("error")))

      val result = route(app, fakeRequest).value

      status(result) mustEqual Status.BAD_REQUEST
      contentAsJson(result) mustEqual Json.toJsObject(OtherError("error"))
    }

    "returns with an Internal Server Error when the has been validated but data was not processed successfully" in {
      when(mockReferenceDataService.validate(any(), any())).thenReturn(Right(testJson))
      when(mockReferenceDataService.insert(eqTo(RefDataFeed), any())).thenReturn(Future.successful(Left(MongoError("error"))))

      val result = route(app, fakeRequest).value

      status(result) mustEqual Status.INTERNAL_SERVER_ERROR
      contentAsJson(result) mustEqual Json.toJsObject(MongoError("error"))
    }

  }

  override def beforeEach(): Unit = {
    super.beforeEach()
    Mockito.reset(mockReferenceDataService)
  }

  // Do not use directly use `app` instead
  override def fakeApplication(): Application =
    new GuiceApplicationBuilder()
      .configure("play.http.router" -> "testOnlyDoNotUseInAppConf.Routes")
      .overrides(
        bind[ReferenceDataService].toInstance(mockReferenceDataService)
      )
      .build()
}
