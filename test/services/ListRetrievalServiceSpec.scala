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

import base.SpecBase
import generators.BaseGenerators
import generators.ModelArbitraryInstances
import models.ListName
import models.MetaData
import models.ReferenceDataList
import models.ResourceLinks
import models.VersionInformation
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.{eq => eqTo}
import org.mockito.Mockito._
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks
import play.api.inject.bind
import play.api.libs.json.JsObject
import play.api.libs.json.JsString
import play.api.test.Helpers._
import repositories.ListRepository
import repositories.VersionRepository

import scala.concurrent.Future

class ListRetrievalServiceSpec extends SpecBase with ModelArbitraryInstances with BaseGenerators with ScalaCheckDrivenPropertyChecks {

  "getResourceLinks" - {

    "must" - {

      "return None if no links exist" in {

        val mockVersionRepository = mock[VersionRepository]
        val mockListRepository    = mock[ListRepository]

        val app = baseApplicationBuilder.andThen(
          _.overrides(
            bind[ListRepository].toInstance(mockListRepository),
            bind[VersionRepository].toInstance(mockVersionRepository)
          )
        )

        val versionInformation = arbitrary[VersionInformation].sample.value

        running(app) {
          application =>
            when(mockVersionRepository.getLatest).thenReturn(Future.successful(Some(versionInformation)))
            when(mockListRepository.getListNames(any())).thenReturn(Future.successful(Nil))

            val service = application.injector.instanceOf[ListRetrievalService]

            service.getResourceLinks().futureValue mustBe None
        }
      }

      "return None if no version information is found" in {

        val mockVersionRepository = mock[VersionRepository]

        val app = baseApplicationBuilder.andThen(
          _.overrides(
            bind[VersionRepository].toInstance(mockVersionRepository)
          )
        )

        running(app) {
          application =>
            when(mockVersionRepository.getLatest).thenReturn(Future.successful(None))

            val service = application.injector.instanceOf[ListRetrievalService]

            service.getResourceLinks().futureValue mustBe None
        }
      }

      "return ListRepository if links exist" in {

        val mockVersionRepository = mock[VersionRepository]
        val mockListRepository    = mock[ListRepository]

        val app = baseApplicationBuilder.andThen(
          _.overrides(
            bind[ListRepository].toInstance(mockListRepository),
            bind[VersionRepository].toInstance(mockVersionRepository)
          )
        )

        running(app) {
          application =>
            forAll(listWithMaxLength(5)(arbitraryListName), arbitrary[VersionInformation]) {

              (listNames, versionInformation) =>
                when(mockVersionRepository.getLatest).thenReturn(Future.successful(Some(versionInformation)))
                when(mockListRepository.getListNames(any())).thenReturn(Future.successful(listNames))

                val service = application.injector.instanceOf[ListRetrievalService]

                val metaData = MetaData(versionInformation.versionId.versionId, versionInformation.messageInformation.snapshotDate)

                val resourceLinks: Seq[Map[String, JsObject]] = listNames.zipWithIndex.map {
                  case (listName, index) =>
                    Map(s"list${index + 1}" -> JsObject(Seq("href" -> JsString("/customs-reference-data/" + listName.listName))))
                }

                val links = Map(
                  "self" -> JsObject(Seq("href" -> JsString("/customs-reference-data/lists")))
                ) ++ resourceLinks.flatten

                service.getResourceLinks().futureValue mustBe Some(
                  ResourceLinks(_links = links, metaData = metaData)
                )
            }

        }
      }
    }
  }

  "getListByName" - {

    "must" - {

      "return a ReferenceDataList when available" in {

        val mockVersionRepository = mock[VersionRepository]
        val mockListRepository    = mock[ListRepository]

        val app = baseApplicationBuilder.andThen(
          _.overrides(
            bind[ListRepository].toInstance(mockListRepository),
            bind[VersionRepository].toInstance(mockVersionRepository)
          )
        )

        running(app) {
          application =>
            forAll(arbitrary[ReferenceDataList], arbitrary[VersionInformation]) {
              (referenceDataList, versionInformation) =>
                when(mockVersionRepository.getLatest).thenReturn(Future.successful(Some(versionInformation)))
                when(mockListRepository.getListByName(any(), eqTo(versionInformation.versionId))).thenReturn(Future.successful(List(JsObject.empty)))

                val listWithDate = referenceDataList
                  .copy(
                    metaData = MetaData(versionInformation),
                    data = List(JsObject.empty)
                  )

                val service = application.injector.instanceOf[ListRetrievalService]

                service.getList(referenceDataList.id).futureValue.value mustBe listWithDate
            }
        }
      }

      "return None when ReferenceDataList is unavailable" in {

        val mockVersionRepository = mock[VersionRepository]
        val mockListRepository    = mock[ListRepository]

        val app = baseApplicationBuilder.andThen(
          _.overrides(
            bind[ListRepository].toInstance(mockListRepository),
            bind[VersionRepository].toInstance(mockVersionRepository)
          )
        )

        val versionInformation = arbitrary[VersionInformation].sample.value
        when(mockVersionRepository.getLatest).thenReturn(Future.successful(Some(versionInformation)))

        running(app) {
          application =>
            when(mockListRepository.getListByName(any(), any())).thenReturn(Future.successful(Nil))

            val service = application.injector.instanceOf[ListRetrievalService]
            val result  = service.getList(ListName("Invalid name"))

            result.futureValue mustBe None
        }
      }
    }
  }

}
