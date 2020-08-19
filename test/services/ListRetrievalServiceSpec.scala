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
import generators.ModelGenerators
import models.{MetaData, ReferenceDataList}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks
import play.api.inject.bind
import play.api.libs.json.JsArray
import play.api.test.Helpers._
import repositories.ListRepository

import scala.concurrent.Future

class ListRetrievalServiceSpec extends SpecBase with ModelGenerators with ScalaCheckDrivenPropertyChecks {

  "getList" - {

    "must return a ReferenceDataList when available" in {

      val mockListRepository = mock[ListRepository]

      val app = baseApplicationBuilder.andThen(
        _.overrides(bind[ListRepository].toInstance(mockListRepository))
      )

      running(app) {
        application =>
          forAll(arbitrary[ReferenceDataList]) {
            referenceDataList =>
              when(mockListRepository.getList(any(), any())).thenReturn(Future.successful(JsArray.empty))

              val listWithDate = referenceDataList.copy(metaData = MetaData("version", LocalDate.of(2020, 11, 5)))

              val service = application.injector.instanceOf[ListRetrievalService]

              service.getList(referenceDataList.id).futureValue.value mustBe listWithDate
          }
      }
    }
  }

}
