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

package repositories

import base.ItSpecBase
import config.AppConfig
import generators.{BaseGenerators, ModelArbitraryInstances}
import models.{GenericList, GenericListItem, ListName, MessageInformation, VersionId}
import org.apache.pekko.NotUsed
import org.apache.pekko.actor.ActorSystem
import org.apache.pekko.stream.scaladsl.Source
import org.apache.pekko.stream.testkit.scaladsl.TestSink
import org.mongodb.scala.*
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.{Arbitrary, Gen}
import org.scalactic.Equality
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach}
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.libs.json.{JsObject, Json}
import uk.gov.hmrc.mongo.test.DefaultPlayMongoRepositorySupport

import java.time.{Instant, LocalDate}
import scala.concurrent.ExecutionContext.Implicits.global

class ListRepositorySpec
    extends ItSpecBase
    with BaseGenerators
    with ModelArbitraryInstances
    with BeforeAndAfterEach
    with BeforeAndAfterAll
    with GuiceOneAppPerSuite
    with ScalaFutures
    with DefaultPlayMongoRepositorySupport[GenericListItem] {

  private lazy val appConfig: AppConfig = app.injector.instanceOf[AppConfig]

  override protected val repository: ListRepository = new ListRepository(mongoComponent, appConfig)

  private def seedData(documents: Seq[GenericListItem]): Unit =
    repository.collection
      .insertMany(documents)
      .toFuture()
      .futureValue

  private def listOfItemsForVersion(versionId: VersionId): Gen[List[GenericListItem]] = {
    implicit val arbitraryVersionId: Arbitrary[VersionId] = Arbitrary(versionId)
    listWithMaxLength[GenericListItem](5)
  }

  "getListByName" - {

    implicit lazy val actorSystem: ActorSystem = ActorSystem()

    "returns the list items that match the specified VersionId" in {
      val versionId  = VersionId("1")
      val dataListV1 = listOfItemsForVersion(versionId).sample.value
      val dataListV2 = listOfItemsForVersion(VersionId("2")).sample.value
      val listName   = arbitrary[ListName].sample.value

      val targetList = dataListV1.map(_.copy(listName = listName))
      val otherList  = dataListV2.map(_.copy(listName = listName))

      seedData(targetList ++ otherList)

      val result: Source[JsObject, NotUsed] = repository.getListByName(listName, versionId, None)

      val data = targetList
        .map(Json.toJsObject(_))
        .map(_ - "listName" - "snapshotDate" - "versionId" - "messageID" - "createdOn")

      result
        .runWith(TestSink.probe[JsObject])
        .request(targetList.length)
        .expectNextN(data)
    }

    "returns a source that completes immediately when there are no matching items" in {
      val versionId = VersionId("1")
      val listName  = arbitrary[ListName].sample.value

      val result: Source[JsObject, NotUsed] = repository.getListByName(listName, versionId, None)

      result
        .runWith(TestSink.probe[JsObject])
        .request(1)
        .expectComplete()
    }
  }

  "insertList" - {

    "must save a list" in {
      val listName = nonEmptyString.sample.value
      val entries  = listWithMaxLength[GenericListItem](10).sample.value

      val list = GenericList(ListName(listName), entries)

      val result = repository.insertList(list).futureValue

      result `mustBe` SuccessfulWrite(ListName(listName), entries.length)

      findAll().futureValue must contain theSameElementsAs entries
    }
  }

  "remove" - {
    "must remove documents" - {
      "when document has an expired version ID" in {
        val v1 = VersionId("1")
        val v2 = VersionId("2")
        val v3 = VersionId("3")

        val expiredVersionIds = Seq(v1, v2)

        val gli1 = GenericListItem(ListName("a"), MessageInformation("messageId1", LocalDate.now()), v1, Json.obj(), Instant.now())
        val gli2 = GenericListItem(ListName("b"), MessageInformation("messageId2", LocalDate.now()), v2, Json.obj(), Instant.now())
        val gli3 = GenericListItem(ListName("c"), MessageInformation("messageId3", LocalDate.now()), v3, Json.obj(), Instant.now())

        seedData(Seq(gli1, gli2, gli3))

        repository.remove(expiredVersionIds).futureValue

        val result = findAll().futureValue.map(_.versionId)

        result mustEqual Seq(v3)
      }
    }
  }
}
