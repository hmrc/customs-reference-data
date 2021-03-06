/*
 * Copyright 2021 HM Revenue & Customs
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

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.Materializer
import akka.stream.scaladsl.Source
import akka.stream.testkit.scaladsl.TestSink
import base.SpecBase
import generators.BaseGenerators
import generators.ModelArbitraryInstances
import models._
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
            when(mockVersionRepository.getLatestListNames()).thenReturn(listNames)

            val service = application.injector.instanceOf[ListRetrievalService]

            service.getResourceLinks().futureValue mustBe None
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
                when(mockVersionRepository.getLatestListNames()).thenReturn(Future.successful(listNames))

                val service = application.injector.instanceOf[ListRetrievalService]

                val resourceLinks: Seq[Map[String, JsObject]] = listNames.map {
                  listName =>
                    Map(listName.listName -> JsObject(Seq("href" -> JsString("/customs-reference-data/lists/" + listName.listName))))
                }

                val links = Map(
                  "self" -> JsObject(Seq("href" -> JsString("/customs-reference-data/lists")))
                ) ++ resourceLinks.flatten

                service.getResourceLinks().futureValue mustBe Some(ResourceLinks(_links = links))
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
                when(mockVersionRepository.getLatest(any())).thenReturn(Future.successful(Some(versionInformation)))
                when(mockListRepository.getListByName(any())).thenReturn(Future.successful(List(JsObject.empty)))

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
        when(mockVersionRepository.getLatest(any())).thenReturn(Future.successful(Some(versionInformation)))

        running(app) {
          application =>
            when(mockListRepository.getListByName(any())).thenReturn(Future.successful(Nil))

            val service = application.injector.instanceOf[ListRetrievalService]
            val result  = service.getList(ListName("Invalid name"))

            result.futureValue mustBe None
        }
      }
    }
  }

  "getMetaData" - {

    "must return MetaData when given the latest version information" in {

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
              val result  = service.getMetaData(versionInformation.listNames.head)

              result.futureValue.value mustBe MetaData(versionInformation)
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
          val result  = service.getMetaData(ListName("Invalid"))

          result.futureValue mustBe None
      }
    }
  }

  "streamList" - {

    implicit lazy val actorSystem: ActorSystem = ActorSystem()
    implicit lazy val mat: Materializer        = ActorMaterializer()

    "must return reference data as stream" in {

      val mockVersionRepository = mock[VersionRepository]
      val mockListRepository    = mock[ListRepository]

      val app = baseApplicationBuilder.andThen(
        _.overrides(
          bind[ListRepository].toInstance(mockListRepository),
          bind[VersionRepository].toInstance(mockVersionRepository)
        )
      )

      val sourceElement        = Json.obj("a" -> "b")
      val expectedSourceValues = scala.collection.immutable.Seq.fill(4)(sourceElement)

      running(app) {
        application =>
          forAll(arbitrary[ReferenceDataList], arbitrary[VersionInformation]) {
            (referenceDataList, versionInformation) =>
              val source: Source[JsObject, Future[_]] =
                Source.futureSource(Future.successful(Source(1 to 4).map(_ => Json.obj("index" -> "value", "data" -> sourceElement))))

              when(mockListRepository.getListByNameSource(any())).thenReturn(Future.successful(source))
              when(mockVersionRepository.getLatest(any())).thenReturn(Future.successful(Some(versionInformation)))

              val service = application.injector.instanceOf[ListRetrievalService]

              service
                .streamList(referenceDataList.id)
                .futureValue
                .value
                .runWith(TestSink.probe[JsObject])
                .request(4)
                .expectNextN(expectedSourceValues)
          }
      }
    }

    "must return None if no version information is found" in {

      val mockVersionRepository = mock[VersionRepository]

      val app = baseApplicationBuilder.andThen(
        _.overrides(
          bind[VersionRepository].toInstance(mockVersionRepository)
        )
      )

      running(app) {
        application =>
          forAll(arbitrary[ReferenceDataList]) {
            referenceDataList =>
              when(mockVersionRepository.getLatest(any())).thenReturn(Future.successful(None))

              val service = application.injector.instanceOf[ListRetrievalService]

              service.streamList(referenceDataList.id).futureValue mustBe None
          }
      }
    }
  }

}
