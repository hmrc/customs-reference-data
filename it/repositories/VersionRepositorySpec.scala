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
import generators.BaseGenerators
import generators.ModelArbitraryInstances
import models.ApiDataSource.ColDataFeed
import models.ApiDataSource.RefDataFeed
import models.ListName
import models.MessageInformation
import models.VersionId
import models.VersionInformation
import org.scalacheck.Arbitrary
import org.scalactic.Equality
import org.scalatest.BeforeAndAfterAll
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import uk.gov.hmrc.mongo.test.DefaultPlayMongoRepositorySupport

import java.time.temporal.ChronoUnit
import java.time.Instant
import java.time.LocalDate
import scala.concurrent.ExecutionContext.Implicits.global

class VersionRepositorySpec
    extends ItSpecBase
    with BaseGenerators
    with ModelArbitraryInstances
    with BeforeAndAfterEach
    with BeforeAndAfterAll
    with GuiceOneAppPerSuite
    with DefaultPlayMongoRepositorySupport[VersionInformation] {

  private lazy val appConfig: AppConfig = app.injector.instanceOf[AppConfig]

  override protected val repository = new VersionRepository(mongoComponent, appConfig)

  "save" - {
    "saves and a version number when the version information is successfully saved" in {
      val messageInformation = Arbitrary.arbitrary[MessageInformation].sample.value
      val listName           = Arbitrary.arbitrary[ListName].sample.value

      val expectedVersionId = VersionId("1")

      val result = repository.save(expectedVersionId, messageInformation, RefDataFeed, Seq(listName), Instant.now()).futureValue

      val expectedVersionInformation = VersionInformation(messageInformation, expectedVersionId, Instant.now, RefDataFeed, Seq(listName))

      result mustEqual true

      val savedVersionInformation = findAll().futureValue.head

      savedVersionInformation mustEqual expectedVersionInformation
    }
  }

  "getLatest with listName filter" - {
    "returns the latest version for a listName by snapshotDate and createdOn date" in {
      val nowDate = LocalDate.now()
      val nowTime = Instant.now()

      val messageInformation = Arbitrary.arbitrary[MessageInformation].sample.value
      val listName           = Arbitrary.arbitrary[ListName].sample.value

      val v1 = VersionInformation(messageInformation.copy(snapshotDate = nowDate), VersionId("1"), nowTime, RefDataFeed, Seq(listName))
      val v2 = VersionInformation(messageInformation.copy(snapshotDate = nowDate), VersionId("2"), nowTime.plus(1, ChronoUnit.DAYS), RefDataFeed, Seq(listName))
      val v3 = VersionInformation(
        messageInformation.copy(snapshotDate = nowDate.minusDays(1)),
        VersionId("3"),
        nowTime.plus(2, ChronoUnit.DAYS),
        RefDataFeed,
        Seq(listName)
      )

      Seq(v1, v2, v3).map(insert(_).futureValue)

      val result: VersionInformation = repository.getLatest(listName).futureValue.value

      result mustEqual v2
    }

    "returns the latest version for a listName when there is a newer version that it does not belong to" in {
      val nowDate = LocalDate.now()
      val nowTime = Instant.now()

      val messageInformation = Arbitrary.arbitrary[MessageInformation].sample.value
      val listName1          = ListName("1")
      val listName2          = ListName("2")

      val v1 =
        VersionInformation(
          messageInformation.copy(snapshotDate = nowDate.minusDays(1)),
          VersionId("1"),
          nowTime.minus(1, ChronoUnit.DAYS),
          RefDataFeed,
          Seq(listName1)
        )
      val v2 = VersionInformation(messageInformation.copy(snapshotDate = nowDate), VersionId("2"), nowTime, ColDataFeed, Seq(listName2))

      Seq(v1, v2).map(insert(_).futureValue)

      val result: VersionInformation = repository.getLatest(listName1).futureValue.value

      result mustEqual v1
    }
  }

  "getLatestListNames" - {
    "returns listnames for the latest REF by snapshotDate" in {
      val newSnapshotDate       = LocalDate.now()
      val newMessageInformation = MessageInformation("messageId", newSnapshotDate)

      val listNames = Seq(ListName("1"), ListName("2"))

      val v = VersionInformation(newMessageInformation, VersionId("1"), Instant.now(), RefDataFeed, listNames)
      insert(v).futureValue

      val result         = repository.getLatestListNames.futureValue
      val expectedResult = listNames

      result must contain theSameElementsAs expectedResult
    }

    "returns listnames for the latest COL by snapshotDate" in {
      val newSnapshotDate       = LocalDate.now()
      val newMessageInformation = MessageInformation("messageId", newSnapshotDate)

      val listNames = Seq(ListName("1"), ListName("2"))

      val v = VersionInformation(newMessageInformation, VersionId("1"), Instant.now(), ColDataFeed, listNames)
      insert(v).futureValue

      val result         = repository.getLatestListNames.futureValue
      val expectedResult = listNames

      result must contain theSameElementsAs expectedResult
    }

    "returns listnames for the latest REF and COL by snapshotDate" in {
      val newSnapshotDate       = LocalDate.now()
      val newMessageInformation = MessageInformation("messageId", newSnapshotDate)
      val oldMessageInformation = MessageInformation("messageId", newSnapshotDate.minusDays(1))

      val listNames1 = Seq(ListName("a"), ListName("b"))
      val listNames2 = Seq(ListName("1"), ListName("2"))
      val listNames3 = Seq(ListName("1.1"), ListName("2.1"))

      val v1 = VersionInformation(oldMessageInformation, VersionId("1"), Instant.now().minus(1, ChronoUnit.DAYS), ColDataFeed, listNames1)
      val v2 = VersionInformation(oldMessageInformation, VersionId("2"), Instant.now().minus(1, ChronoUnit.DAYS), RefDataFeed, listNames2)
      val v3 = VersionInformation(newMessageInformation, VersionId("3"), Instant.now(), RefDataFeed, listNames3)

      Seq(v1, v2, v3).map(insert(_).futureValue)

      val result         = repository.getLatestListNames.futureValue
      val expectedResult = listNames1 ++ listNames3

      result must contain theSameElementsAs expectedResult
    }

    "ensure sort happens before group" in {
      val newSnapshotDate       = LocalDate.now()
      val newMessageInformation = MessageInformation("messageId", newSnapshotDate)
      val oldMessageInformation = MessageInformation("messageId", newSnapshotDate.minusDays(1))

      val listNames1 = Seq(ListName("a"), ListName("b"))
      val listNames2 = Seq(ListName("c"), ListName("d"))
      val listNames3 = Seq(ListName("1"), ListName("2"))
      val listNames4 = Seq(ListName("1.1"), ListName("2.1"))

      val v1 = VersionInformation(newMessageInformation, VersionId("1"), Instant.now(), ColDataFeed, listNames1)
      val v2 = VersionInformation(oldMessageInformation, VersionId("2"), Instant.now().minus(1, ChronoUnit.DAYS), ColDataFeed, listNames2)
      val v3 = VersionInformation(oldMessageInformation, VersionId("3"), Instant.now().minus(1, ChronoUnit.DAYS), RefDataFeed, listNames3)
      val v4 = VersionInformation(newMessageInformation, VersionId("4"), Instant.now(), RefDataFeed, listNames4)

      Seq(v1, v2, v3, v4).map(insert(_).futureValue)

      val result         = repository.getLatestListNames.futureValue
      val expectedResult = listNames1 ++ listNames4

      result must contain theSameElementsAs expectedResult
    }
  }

  implicit val versionInformationEquality: Equality[VersionInformation] =
    (a, b) =>
      b match {
        case VersionInformation(mi, ver, _, sc, ln) =>
          (a.messageInformation == mi) && (a.versionId == ver) && (a.source == sc) && (a.listNames == ln)
        case _ => false
      }

}
