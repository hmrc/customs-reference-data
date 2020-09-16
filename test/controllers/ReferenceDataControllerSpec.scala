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

import java.io.ByteArrayOutputStream
import java.util.zip.GZIPOutputStream

import akka.util.ByteString
import base.SpecBase
import models.ResponseErrorMessage
import models.ResponseErrorType.OtherError
import org.mockito.ArgumentMatchers._
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
import services.ReferenceDataService
import services.ReferenceDataService.DataProcessingResult._

import scala.concurrent.Future

class ReferenceDataControllerSpec extends SpecBase with GuiceOneAppPerSuite with BeforeAndAfterEach {

  override def beforeEach(): Unit =
    super.beforeEach()

  val mockReferenceDataService = mock[ReferenceDataService]

  // Do not use directly use `app` instead
  override def fakeApplication(): Application =
    new GuiceApplicationBuilder()
      .configure("play.http.router" -> "testOnlyDoNotUseInAppConf.Routes")
      .overrides(bind[ReferenceDataService].toInstance(mockReferenceDataService))
      .build()

  private def fakeRequest(byteArray: Array[Byte]): FakeRequest[AnyContentAsRaw] =
    FakeRequest(POST, routes.ReferenceDataController.post().url)
      .withRawBody(ByteString.apply(byteArray))

  def compress(input: Array[Byte]): Array[Byte] = {
    val bos  = new ByteArrayOutputStream(input.length)
    val gzip = new GZIPOutputStream(bos)
    gzip.write(input)
    gzip.close()
    val compressed = bos.toByteArray
    bos.close()
    compressed
  }

  "post" - {
    "returns Ok when the data has been processed with a valid GZipped Json body" in {
      when(mockReferenceDataService.insert(any())).thenReturn(Future.successful(DataProcessingSuccessful))

      val json =
        """
          |{
          |   "messageInformation": {
          |     "messageID": "74bd0784-8dc9-4eba-a435-9914ace26995",
          |     "snapshotDate": "2020-07-06"
          | }
          |}
          |""".stripMargin

      val compressedJson = compress(json.getBytes)

      val result = route(app, fakeRequest(compressedJson)).value

      status(result) mustBe Status.ACCEPTED
    }

    "returns with an Internal Server Error when the data was not processed successfully" in {
      when(mockReferenceDataService.insert(any())).thenReturn(Future.successful(DataProcessingFailed))

      val json =
        """
          |{
          |   "messageInformation": {
          |     "messageID": "74bd0784-8dc9-4eba-a435-9914ace26995",
          |     "snapshotDate": "2020-07-06"
          | }
          |}
          |""".stripMargin

      val compressedJson = compress(json.getBytes)

      val result = route(app, fakeRequest(compressedJson)).value

      status(result) mustBe Status.INTERNAL_SERVER_ERROR
      contentAsJson(result) mustBe Json.toJsObject(ResponseErrorMessage(OtherError, None))
    }

    "returns Internal Server Error if request body is not in compressed GZip format" in {

      val json =
        """
          |{
          |   "messageInformation": {
          |     "messageID": "74bd0784-8dc9-4eba-a435-9914ace26995",
          |     "snapshotDate": "2020-07-06"
          | }
          |}
          |""".stripMargin

      val result = route(app, fakeRequest(json.getBytes)).value

      status(result) mustBe Status.INTERNAL_SERVER_ERROR
    }

    "returns Internal Server Error if request body is compressed however Json is invalid" in {

      val invalidJson = "Invalid Json"

      val result = route(app, fakeRequest(invalidJson.getBytes)).value

      status(result) mustBe Status.INTERNAL_SERVER_ERROR
    }

//    "returns Internal Server Error if request body is larger than memory threshold" in {
//
//      val result = route(app, fakeRequest(???)).value
//
//      status(result) mustBe Status.INTERNAL_SERVER_ERROR
//    }
  }

}
