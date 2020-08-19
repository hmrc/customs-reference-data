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

package services

import java.time.LocalDate

import base.SpecBase
import models.ListName
import models.MetaData
import models.ReferenceDataList
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import play.api.inject.bind
import play.api.libs.json.JsArray
import play.api.test.Helpers._
import repositories.ListRepository

import scala.concurrent.Future

class ListRetrievalServiceSpec extends SpecBase {

  "getList" - {

    "must return a ReferenceDataList when available" in {

      val mockListRepository = mock[ListRepository]

      // Potential Generators
      val testListName      = ListName("testListName")
      val metaData          = MetaData("test", LocalDate.now)
      val referenceDataList = ReferenceDataList(testListName, metaData, JsArray.empty)

      val app = baseApplicationBuilder.andThen(
        _.overrides(bind[ListRepository].toInstance(mockListRepository))
      )

      when(mockListRepository.getList(any())).thenReturn(Future.successful(Some(referenceDataList)))

      running(app) {
        application =>
          val service = application.injector.instanceOf[ListRetrievalService]

          service.getList(testListName).futureValue.value mustBe ReferenceDataList(testListName, metaData, JsArray.empty)
      }
    }

    "must return None when not available" in {

      val mockListRepository = mock[ListRepository]

      // Potential Generators
      val testListName = ListName("testListName")

      val app = baseApplicationBuilder.andThen(
        _.overrides(bind[ListRepository].toInstance(mockListRepository))
      )

      when(mockListRepository.getList(any())).thenReturn(Future.successful(None))

      running(app) {
        application =>
          val service = application.injector.instanceOf[ListRetrievalService]

          service.getList(testListName).futureValue mustBe None
      }
    }

  }

}
