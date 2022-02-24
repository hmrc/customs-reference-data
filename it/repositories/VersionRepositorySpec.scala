package repositories

import base.ItSpecBase
import generators.BaseGenerators
import generators.ModelArbitraryInstances
import models.ApiDataSource.ColDataFeed
import models.ApiDataSource.RefDataFeed
import models.ListName
import models.MessageInformation
import models.VersionId
import models.VersionInformation
import org.mockito.Mockito.when
import org.mongodb.scala.bson.BsonDocument
import org.mongodb.scala.bson.BsonString
import org.scalacheck.Arbitrary
import org.scalactic.Equality
import org.scalatest.BeforeAndAfterAll
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import services.consumption.TimeService
import uk.gov.hmrc.mongo.test.DefaultPlayMongoRepositorySupport

import java.time.LocalDate
import java.time.LocalDateTime
import scala.concurrent.ExecutionContext.Implicits.global

class VersionRepositorySpec
    extends ItSpecBase
    with BaseGenerators
    with ModelArbitraryInstances
    with BeforeAndAfterEach
    with BeforeAndAfterAll
    with GuiceOneAppPerSuite
    with DefaultPlayMongoRepositorySupport[VersionInformation] {

  private val mockTimeService: TimeService = mock[TimeService]

  override protected def repository = new VersionRepository(mongoComponent, mockTimeService)

  "must create the following indexes" in {
    val indexes = repository.collection.listIndexes().toFuture().futureValue

    indexes.length mustEqual 3

    indexes(1).get("name").get mustEqual BsonString("list-name-and-snapshot-date-compound-index")
    indexes(1).get("key").get mustEqual BsonDocument("listNames.listName" -> 1, "snapshotDate" -> -1)

    indexes(2).get("name").get mustEqual BsonString("source-and-snapshot-date-compound-index")
    indexes(2).get("key").get mustEqual BsonDocument("source" -> 1, "snapshotDate" -> -1)
  }

  "save" - {
    "saves and a version number when the version information is successfully saved" in {
      val messageInformation = Arbitrary.arbitrary[MessageInformation].sample.value
      val listName           = Arbitrary.arbitrary[ListName].sample.value

      val expectedVersionId = VersionId("1")

      when(mockTimeService.now()).thenReturn(LocalDateTime.now())

      val result = repository.save(expectedVersionId, messageInformation, RefDataFeed, Seq(listName)).futureValue

      val expectedVersionInformation = VersionInformation(messageInformation, expectedVersionId, LocalDateTime.now, RefDataFeed, Seq(listName))

      result mustEqual true

      val savedVersionInformation = findAll().futureValue.head

      savedVersionInformation mustEqual expectedVersionInformation
    }
  }

  "getLatest with listName filter" - {
    "returns the latest version for a listName by snapshotDate" in {
      val latestSnapshotDate = LocalDate.now()
      val latestCreatedOn    = LocalDateTime.now()

      val oldSnapshotDate = LocalDate.now().minusDays(1)
      val oldCreatedOn    = LocalDateTime.now().minusDays(1)

      when(mockTimeService.now()).thenReturn(oldCreatedOn, latestCreatedOn)

      val messageInformation = Arbitrary.arbitrary[MessageInformation].sample.value
      val listName           = Arbitrary.arbitrary[ListName].sample.value

      repository.save(VersionId("1"), messageInformation.copy(snapshotDate = oldSnapshotDate), RefDataFeed, Seq(listName)).futureValue
      repository.save(VersionId("2"), messageInformation.copy(snapshotDate = latestSnapshotDate), RefDataFeed, Seq(listName)).futureValue

      val expectedVersionInformation =
        VersionInformation(messageInformation.copy(snapshotDate = latestSnapshotDate), VersionId("2"), latestCreatedOn, RefDataFeed, Seq(listName))

      val result: VersionInformation = repository.getLatest(listName).futureValue.value

      result mustEqual expectedVersionInformation
    }

    "returns the latest version for a listName when the is a newer version that it does not belong to" in {
      val snapshotDate    = LocalDate.now()
      val latestCreatedOn = LocalDateTime.now()
      val oldCreatedOn    = LocalDateTime.now().minusDays(1)

      when(mockTimeService.now()).thenReturn(oldCreatedOn, latestCreatedOn)

      val messageInformation = Arbitrary.arbitrary[MessageInformation].sample.value
      val listName1          = ListName("1")
      val listName2          = ListName("2")

      repository.save(VersionId("1"), messageInformation.copy(snapshotDate = snapshotDate), RefDataFeed, Seq(listName1)).futureValue
      repository.save(VersionId("2"), messageInformation.copy(snapshotDate = snapshotDate), ColDataFeed, Seq(listName2)).futureValue

      val expectedVersionInformation =
        VersionInformation(messageInformation.copy(snapshotDate = snapshotDate), VersionId("1"), latestCreatedOn, RefDataFeed, Seq(listName1))

      val result: VersionInformation = repository.getLatest(listName1).futureValue.value

      result mustEqual expectedVersionInformation
    }
  }

  "getLatestListNames" - {
    "returns listnames for the latest REF and COL by snapshotDate" in {
      val newSnapshotDate       = LocalDate.now()
      val newMessageInformation = MessageInformation("messageId", newSnapshotDate)
      val oldMessageInformation = MessageInformation("messageId", newSnapshotDate.minusDays(1))

      when(mockTimeService.now())
        .thenReturn(LocalDateTime.now().minusDays(1))
        .thenReturn(LocalDateTime.now().minusDays(1))
        .thenReturn(LocalDateTime.now())

      val listNames1 = Seq(ListName("a"), ListName("b"))
      val listNames2 = Seq(ListName("1"), ListName("2"))
      val listNames3 = Seq(ListName("1.1"), ListName("2.1"))

      repository.save(VersionId("1"), oldMessageInformation, ColDataFeed, listNames1).futureValue
      repository.save(VersionId("2"), oldMessageInformation, RefDataFeed, listNames2).futureValue
      repository.save(VersionId("3"), newMessageInformation, RefDataFeed, listNames3).futureValue

      val result         = repository.getLatestListNames.futureValue
      val expectedResult = listNames1 ++ listNames3

      result must contain theSameElementsAs expectedResult
    }

    "ensure sort happens before group" in {
      val newSnapshotDate       = LocalDate.now()
      val newMessageInformation = MessageInformation("messageId", newSnapshotDate)
      val oldMessageInformation = MessageInformation("messageId", newSnapshotDate.minusDays(1))

      when(mockTimeService.now())
        .thenReturn(LocalDateTime.now())
        .thenReturn(LocalDateTime.now().minusDays(1))
        .thenReturn(LocalDateTime.now().minusDays(1))
        .thenReturn(LocalDateTime.now())

      val listNames1 = Seq(ListName("a"), ListName("b"))
      val listNames2 = Seq(ListName("c"), ListName("d"))
      val listNames3 = Seq(ListName("1"), ListName("2"))
      val listNames4 = Seq(ListName("1.1"), ListName("2.1"))

      repository.save(VersionId("1"), newMessageInformation, ColDataFeed, listNames1).futureValue
      repository.save(VersionId("2"), oldMessageInformation, ColDataFeed, listNames2).futureValue
      repository.save(VersionId("3"), oldMessageInformation, RefDataFeed, listNames3).futureValue
      repository.save(VersionId("4"), newMessageInformation, RefDataFeed, listNames4).futureValue

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
