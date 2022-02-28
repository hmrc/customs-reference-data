/*
 * Copyright 2022 HM Revenue & Customs
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

package controllers.deletion

import base.SpecBase
import org.mockito.Mockito.reset
import org.mockito.Mockito.verify
import org.mockito.Mockito.when
import org.scalatest.BeforeAndAfterEach
import org.scalatest.TestData
import org.scalatestplus.play.guice.GuiceOneAppPerTest
import play.api.Application
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.consumption.ListRetrievalService

import scala.concurrent.Future

class DeletionControllerSpec extends SpecBase with GuiceOneAppPerTest with BeforeAndAfterEach {

  private val mockListRetrievalService = mock[ListRetrievalService]

  override def newAppForTest(testData: TestData): Application =
    new GuiceApplicationBuilder()
      .overrides(bind[ListRetrievalService].toInstance(mockListRetrievalService))
      .build()

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockListRetrievalService)
  }

  "DeletionController" - {

    "delete" - {

      "should return Ok if deletion succeeds" in {

        val fakeRequest = FakeRequest(DELETE, routes.DeletionController.delete().url)

        when(mockListRetrievalService.deleteOutdatedDocuments()).thenReturn(Future.successful(true))

        val result = route(app, fakeRequest).get

        status(result) mustBe OK

        verify(mockListRetrievalService).deleteOutdatedDocuments()
      }

      "should return Internal Server Error if deletion fails" in {

        val fakeRequest = FakeRequest(DELETE, routes.DeletionController.delete().url)

        when(mockListRetrievalService.deleteOutdatedDocuments()).thenReturn(Future.successful(false))

        val result = route(app, fakeRequest).get

        status(result) mustBe INTERNAL_SERVER_ERROR

        verify(mockListRetrievalService).deleteOutdatedDocuments()
      }
    }
  }
}
