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

package services.consumption

import base.SpecBase
import generators.BaseGenerators
import generators.ModelArbitraryInstances
import models._
import org.apache.pekko.NotUsed
import org.apache.pekko.actor.ActorSystem
import org.apache.pekko.stream.scaladsl.Source
import org.apache.pekko.stream.testkit.scaladsl.TestSink
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks
import play.api.inject.bind
import play.api.libs.json.JsObject
import play.api.libs.json.JsString
import play.api.libs.json.Json
import play.api.test.Helpers._
import repositories.ListRepository
import repositories.VersionRepository

import scala.concurrent.Future

class ListRetrievalServiceSpec extends SpecBase with ModelArbitraryInstances with BaseGenerators with ScalaCheckDrivenPropertyChecks {

  "getResourceLinks" - {

    "must" - {

      "return a None list if no listnames are found" in {

        val mockVersionRepository = mock[VersionRepository]

        val app = baseApplicationBuilder.andThen(
          _.overrides(
            bind[VersionRepository].toInstance(mockVersionRepository)
          )
        )

        val listNames = Future.successful(Seq.empty[ListName])

        running(app) {
          application =>
            when(mockVersionRepository.getLatestListNames).thenReturn(listNames)

            val service = application.injector.instanceOf[ListRetrievalService]

            service.getResourceLinks.futureValue must not be defined
        }
      }

      "return list of list names" in {

        val mockVersionRepository = mock[VersionRepository]

        val app = baseApplicationBuilder.andThen(
          _.overrides(
            bind[VersionRepository].toInstance(mockVersionRepository)
          )
        )

        running(app) {
          application =>
            forAll(listWithMaxLength[ListName](10)) {
              listNames =>
                when(mockVersionRepository.getLatestListNames).thenReturn(Future.successful(listNames))

                val service = application.injector.instanceOf[ListRetrievalService]

                val resourceLinks: Seq[Map[String, JsObject]] = listNames.map {
                  listName =>
                    Map(listName.listName -> JsObject(Seq("href" -> JsString("/customs-reference-data/lists/" + listName.listName))))
                }

                val links = Map(
                  "self" -> JsObject(Seq("href" -> JsString("/customs-reference-data/lists")))
                ) ++ resourceLinks.flatten

                service.getResourceLinks.futureValue.value mustEqual ResourceLinks(_links = links)
            }

        }
      }
    }
  }

  "getLatestVersion" - {

    "must return VersionInformation when given the latest version information" in {

      val mockVersionRepository = mock[VersionRepository]

      val app = baseApplicationBuilder.andThen(
        _.overrides(
          bind[VersionRepository].toInstance(mockVersionRepository)
        )
      )

      running(app) {
        application =>
          forAll(arbitrary[VersionInformation]) {
            versionInformation =>
              when(mockVersionRepository.getLatest(any())).thenReturn(Future.successful(Some(versionInformation)))

              val service = application.injector.instanceOf[ListRetrievalService]
              val result  = service.getLatestVersion(versionInformation.listNames.head)

              result.futureValue.value mustEqual versionInformation
          }
      }
    }

    "must return None when no version information is found" in {

      val mockVersionRepository = mock[VersionRepository]

      val app = baseApplicationBuilder.andThen(
        _.overrides(
          bind[VersionRepository].toInstance(mockVersionRepository)
        )
      )

      running(app) {
        application =>
          when(mockVersionRepository.getLatest(any())).thenReturn(Future.successful(None))

          val service = application.injector.instanceOf[ListRetrievalService]
          val result  = service.getLatestVersion(ListName("Invalid"))

          result.futureValue must not be defined
      }
    }
  }

  "getStreamedList" - {

    implicit lazy val actorSystem: ActorSystem = ActorSystem()

    "must return filtered reference data as stream" in {

      val filterParams: FilterParams = new FilterParams(Seq("data.filter" -> Seq("me")))
      val referenceDataList          = arbitrary[ReferenceDataList].sample.value
      val version                    = arbitrary[VersionInformation].sample.value

      val source: Source[JsObject, NotUsed] = Source(1 to 4).map(
        _ => Json.obj("index" -> "value", "data" -> Json.obj("filter" -> "me"))
      )

      val mockVersionRepository = mock[VersionRepository]
      val mockListRepository    = mock[ListRepository]

      val app = baseApplicationBuilder.andThen(
        _.overrides(
          bind[ListRepository].toInstance(mockListRepository),
          bind[VersionRepository].toInstance(mockVersionRepository)
        )
      )

      val sourceElement        = Json.obj("filter" -> "me")
      val expectedSourceValues = scala.collection.immutable.Seq.fill(4)(sourceElement)

      running(app) {
        application =>
          when(mockListRepository.getListByName(any(), any(), any())).thenReturn(source)
          when(mockVersionRepository.getLatest(any())).thenReturn(Future.successful(Some(version)))

          val service = application.injector.instanceOf[ListRetrievalService]

          service
            .getStreamedList(referenceDataList.id, version.versionId, Some(filterParams))
            .runWith(TestSink.probe[JsObject])
            .request(4)
            .expectNextN(expectedSourceValues)
      }
    }
  }
}
