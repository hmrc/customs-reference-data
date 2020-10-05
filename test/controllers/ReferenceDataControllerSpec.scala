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
import models.OtherError
import org.mockito.ArgumentMatchers._
import org.mockito.Mockito
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import play.api.http.Status
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import play.api.mvc.AnyContentAsRaw
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.ingestion.ReferenceDataService.DataProcessingResult.DataProcessingFailed
import services.ingestion.ReferenceDataService.DataProcessingResult.DataProcessingSuccessful
import services.ingestion.ReferenceDataService
import services.ingestion.SchemaValidationService

import scala.concurrent.Future

class ReferenceDataControllerSpec extends SpecBase with GuiceOneAppPerSuite with BeforeAndAfterEach {

  val mockReferenceDataService    = mock[ReferenceDataService]
  val mockSchemaValidationService = mock[SchemaValidationService]

  private val testJson = Json.obj("foo" -> "bar")

  "referenceDataLists" - {

    def fakeRequest: FakeRequest[AnyContentAsRaw] =
      FakeRequest(POST, routes.ReferenceDataController.referenceDataLists().url)
        .withRawBody(ByteString(testJson.toString.getBytes))

    "returns ACCEPTED when the data has been decompressed, validated and processed" in {
      when(mockReferenceDataService.validateAndDecompress(any(), any())).thenReturn(Right(testJson))
      when(mockReferenceDataService.insert(any())).thenReturn(Future.successful(DataProcessingSuccessful))

      val result = route(app, fakeRequest).value

      status(result) mustBe Status.ACCEPTED
    }

    "returns Bad Request when a decompression or validation error occurs" in {
      when(mockReferenceDataService.validateAndDecompress(any(), any())).thenReturn(Left(OtherError("error")))
      when(mockReferenceDataService.insert(any())).thenReturn(Future.successful(DataProcessingSuccessful))

      val result = route(app, fakeRequest).value

      status(result) mustBe Status.BAD_REQUEST
    }

    "returns with an Internal Server Error when the has been validated but data was not processed successfully" in {
      when(mockReferenceDataService.validateAndDecompress(any(), any())).thenReturn(Right(testJson))
      when(mockReferenceDataService.insert(any())).thenReturn(Future.successful(DataProcessingFailed))

      val result = route(app, fakeRequest).value

      status(result) mustBe Status.INTERNAL_SERVER_ERROR
      contentAsJson(result) mustBe Json.toJsObject(OtherError("Failed in processing the data list"))
    }

  }

  "customsOfficeLists" - {
    def fakeRequest: FakeRequest[AnyContentAsRaw] =
      FakeRequest(POST, routes.ReferenceDataController.customsOfficeLists().url)
        .withRawBody(ByteString(testJson.toString.getBytes))

    "returns ACCEPTED when the data has been decompressed, validated and processed" in {
      when(mockReferenceDataService.validateAndDecompress(any(), any())).thenReturn(Right(testJson))
      when(mockReferenceDataService.insert(any())).thenReturn(Future.successful(DataProcessingSuccessful))

      val result = route(app, fakeRequest).value

      status(result) mustBe Status.ACCEPTED
    }

    "returns Bad Request when a decompression or validation error occurs" in {
      when(mockReferenceDataService.validateAndDecompress(any(), any())).thenReturn(Left(OtherError("error")))
      when(mockReferenceDataService.insert(any())).thenReturn(Future.successful(DataProcessingSuccessful))

      val result = route(app, fakeRequest).value

      status(result) mustBe Status.BAD_REQUEST
    }

    "returns with an Internal Server Error when the has been validated but data was not processed successfully" in {
      when(mockReferenceDataService.validateAndDecompress(any(), any())).thenReturn(Right(testJson))
      when(mockReferenceDataService.insert(any())).thenReturn(Future.successful(DataProcessingFailed))

      val result = route(app, fakeRequest).value

      status(result) mustBe Status.INTERNAL_SERVER_ERROR
      contentAsJson(result) mustBe Json.toJsObject(OtherError("Failed in processing the data list"))
    }
  }

  override def beforeEach(): Unit = {
    super.beforeEach()
    Mockito.reset(mockReferenceDataService, mockSchemaValidationService)
  }

  // Do not use directly use `app` instead
  override def fakeApplication(): Application =
    new GuiceApplicationBuilder()
      .configure("play.http.router" -> "testOnlyDoNotUseInAppConf.Routes")
      .overrides(
        bind[ReferenceDataService].toInstance(mockReferenceDataService),
        bind[SchemaValidationService].toInstance(mockSchemaValidationService)
      )
      .build()
}
