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

package controllers.ingestion.v2

import akka.util.ByteString
import base.SpecBase
import models.ApiDataSource.ColDataFeed
import models.OtherError
import models.WriteError
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
import play.api.mvc.AnyContentAsRaw
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.ingestion.v2.ReferenceDataService

import java.io.File
import java.nio.file.Files
import scala.concurrent.Future

class CustomsOfficeListControllerSpec extends SpecBase with GuiceOneAppPerSuite with BeforeAndAfterEach {

  val mockReferenceDataService: ReferenceDataService = mock[ReferenceDataService]

  private val testJson = Json.obj("foo" -> "bar")

  private val headers = Seq(
    "Accept"        -> "application/vnd.hmrc.2.0+gzip",
    "Authorization" -> "Bearer ABC"
  )

  private def fakeRequest: FakeRequest[AnyContentAsJson] =
    fakeRequest(headers)

  private def fakeRequest(headers: Seq[(String, String)]): FakeRequest[AnyContentAsJson] =
    FakeRequest(POST, "/customs-reference-data/customs-office-lists")
      .withJsonBody(testJson)
      .withHeaders(headers: _*)

  "customsOfficeLists" - {

    "returns Accepted when the data has been validated and processed" in {
      when(mockReferenceDataService.validate(any(), any())).thenReturn(Right(testJson))
      when(mockReferenceDataService.insert(eqTo(ColDataFeed), any())).thenReturn(Future.successful(None))

      val result = route(app, fakeRequest).value

      status(result) mustBe Status.ACCEPTED
    }

    "returns Accepted when the data is gzipped" in {
      val file       = new File(getClass.getResource("/test.data.json.gz").toURI)
      val byteString = ByteString(Files.readAllBytes(file.toPath))

      val headers = Seq(
        "Accept"           -> "application/vnd.hmrc.2.0+gzip",
        "Authorization"    -> "Bearer ABC",
        "Content-Encoding" -> "gzip",
        "Content-Type"     -> "application/json"
      )

      def fakeRequest: FakeRequest[AnyContentAsRaw] =
        FakeRequest(POST, "/customs-reference-data/customs-office-lists")
          .withRawBody(byteString)
          .withHeaders(headers: _*)

      when(mockReferenceDataService.validate(any(), any())).thenReturn(Right(testJson))
      when(mockReferenceDataService.insert(eqTo(ColDataFeed), any())).thenReturn(Future.successful(None))

      val result = route(app, fakeRequest).value

      status(result) mustBe Status.ACCEPTED // getting BAD_REQUEST
    }

    "returns Bad Request when Accept header is missing" in {
      val headers = Seq("Authorization" -> "Bearer ABC")

      val result = route(app, fakeRequest(headers)).value

      status(result) mustBe Status.BAD_REQUEST
    }

    "returns Unauthorized when Authorization header is missing" in {
      val headers = Seq("Accept" -> "application/vnd.hmrc.2.0+gzip")

      val result = route(app, fakeRequest(headers)).value

      status(result) mustBe Status.UNAUTHORIZED
    }

    "returns Bad Request when a validation error occurs" in {
      when(mockReferenceDataService.validate(any(), any())).thenReturn(Left(OtherError("error")))

      val result = route(app, fakeRequest).value

      status(result) mustBe Status.BAD_REQUEST
      contentAsJson(result) mustBe Json.toJsObject(OtherError("error"))
    }

    "returns with an Internal Server Error when the has been validated but data was not processed successfully" in {
      when(mockReferenceDataService.validate(any(), any())).thenReturn(Right(testJson))
      when(mockReferenceDataService.insert(eqTo(ColDataFeed), any())).thenReturn(Future.successful(Some(WriteError("error"))))

      val result = route(app, fakeRequest).value

      status(result) mustBe Status.INTERNAL_SERVER_ERROR
      contentAsJson(result) mustBe Json.toJsObject(WriteError("error"))
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
