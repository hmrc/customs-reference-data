package repositories

import java.time.LocalDate
import java.time.LocalDateTime

import base.ItSpecBase
import generators.BaseGenerators
import generators.ModelArbitraryInstances
import models.ListName
import models.MessageInformation
import models.VersionId
import models.VersionInformation
import org.mockito.Mockito
import org.scalacheck.Arbitrary
import org.scalactic.Equality
import org.scalatest.BeforeAndAfterAll
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import reactivemongo.play.json.ImplicitBSONHandlers.JsObjectDocumentWriter
import reactivemongo.play.json.collection.JSONCollection
import play.api.inject.bind
import services.TimeService
import org.mockito.Mockito.when

import scala.concurrent.ExecutionContext.Implicits.global

class VersionRepositorySpec
    extends ItSpecBase
    with BaseGenerators
    with ModelArbitraryInstances
    with BeforeAndAfterEach
    with BeforeAndAfterAll
    with GuiceOneAppPerSuite
    with FailOnUnindexedQueries {

  override def beforeAll(): Unit = {
    database.flatMap(_.drop()).futureValue
    super.beforeAll()
    started(app).futureValue
  }

  override def beforeEach(): Unit = {
    database
      .flatMap(
        _.collection[JSONCollection](VersionCollection.collectionName)
          .delete()
          .one(Json.obj())
      )
      .futureValue

    Mockito.reset(mockVersionIdProducer, mockTimeService)

    super.beforeEach()
  }

  override def afterAll(): Unit = {
    database.flatMap(_.drop()).futureValue
    super.afterAll()
  }

  val mockVersionIdProducer = mock[VersionIdProducer]
  val mockTimeService       = mock[TimeService]

  override def fakeApplication(): Application =
    new GuiceApplicationBuilder()
      .overrides(
        bind[VersionIdProducer].to(mockVersionIdProducer),
        bind[TimeService].to(mockTimeService)
      )
      .build()

  "save" - {
    "saves and a version number when the version information is successfully saved" in {

      val repo = app.injector.instanceOf[VersionRepository]

      val messageInformation = Arbitrary.arbitrary[MessageInformation].sample.value
      val listName           = Arbitrary.arbitrary[ListName].sample.value

      val expectedVersionId = VersionId("1")
      when(mockVersionIdProducer.apply()).thenReturn(expectedVersionId)
      when(mockTimeService.now()).thenReturn(LocalDateTime.now())

      val result = repo.save(messageInformation, Set(listName)).futureValue

      val expectedVersionInformation = VersionInformation(messageInformation, expectedVersionId, LocalDateTime.now, Set(listName))

      result mustEqual expectedVersionId

      val savedVersionInformation =
        database.flatMap(_.collection[JSONCollection](VersionCollection.collectionName).find(Json.obj(), None).one[VersionInformation]).futureValue.value

      savedVersionInformation mustEqual expectedVersionInformation
    }
  }

  "getLatest with listName filter" - {
    "returns the latest version for a listName by snapshotDate" in {
      val repo = app.injector.instanceOf[VersionRepository]

      val latestSnapshotDate = LocalDate.now()
      val latestCreatedOn    = LocalDateTime.now()

      val oldSnapshotDate = LocalDate.now().minusDays(1)
      val oldCreatedOn    = LocalDateTime.now().minusDays(1)

      when(mockVersionIdProducer.apply()).thenReturn(VersionId("1"), VersionId("2"))
      when(mockTimeService.now()).thenReturn(oldCreatedOn, latestCreatedOn)

      val messageInformation = Arbitrary.arbitrary[MessageInformation].sample.value
      val listName           = Arbitrary.arbitrary[ListName].sample.value

      repo.save(messageInformation.copy(snapshotDate = oldSnapshotDate), Set(listName)).futureValue
      repo.save(messageInformation.copy(snapshotDate = latestSnapshotDate), Set(listName)).futureValue

      val expectedVersionInformation =
        VersionInformation(messageInformation.copy(snapshotDate = latestSnapshotDate), VersionId("2"), latestCreatedOn, Set(listName))

      val result: VersionInformation = repo.getLatest(listName).futureValue.value

      result mustEqual expectedVersionInformation
    }

    "returns the latest version for a listName when the is a newer version that it does not belong to" in {
      val repo = app.injector.instanceOf[VersionRepository]

      val snapshotDate    = LocalDate.now()
      val latestCreatedOn = LocalDateTime.now()
      val oldCreatedOn    = LocalDateTime.now().minusDays(1)

      when(mockVersionIdProducer.apply()).thenReturn(VersionId("1"), VersionId("2"))
      when(mockTimeService.now()).thenReturn(oldCreatedOn, latestCreatedOn)

      val messageInformation = Arbitrary.arbitrary[MessageInformation].sample.value
      val listName1          = ListName("1")
      val listName2          = ListName("2")

      repo.save(messageInformation.copy(snapshotDate = snapshotDate), Set(listName1)).futureValue
      repo.save(messageInformation.copy(snapshotDate = snapshotDate), Set(listName2)).futureValue

      val expectedVersionInformation = VersionInformation(messageInformation.copy(snapshotDate = snapshotDate), VersionId("1"), latestCreatedOn, Set(listName1))

      val result: VersionInformation = repo.getLatest(listName1).futureValue.value

      result mustEqual expectedVersionInformation
    }
  }

  "getLatest" - {
    "returns all version with the latest snapshot date" in {
      val repo = app.injector.instanceOf[VersionRepository]

      val latestSnapshotDate = LocalDate.now()
      val latestCreatedOn    = LocalDateTime.now()

      val oldSnapshotDate = LocalDate.now().minusDays(1)
      val oldCreatedOn    = LocalDateTime.now().minusDays(1)

      when(mockVersionIdProducer.apply()).thenReturn(VersionId("1"), VersionId("2"), VersionId("3"))
      when(mockTimeService.now()).thenReturn(oldCreatedOn, latestCreatedOn)

      val messageInformation = Arbitrary.arbitrary[MessageInformation].sample.value
      val listNames1         = Set(ListName("1"), ListName("2"))
      val listNames2         = Set(ListName("a"), ListName("b"))
      val listNames3         = Set(ListName("c"), ListName("d"))

      repo.save(messageInformation.copy(snapshotDate = oldSnapshotDate), listNames1).futureValue
      repo.save(messageInformation.copy(snapshotDate = latestSnapshotDate), listNames2).futureValue
      repo.save(messageInformation.copy(snapshotDate = latestSnapshotDate), listNames3).futureValue

      val result = repo.getLatest().futureValue
      val expectedResult: Set[ListName] = listNames2 ++ listNames3

      result mustEqual expectedResult
    }
  }

  implicit val versionInformationEquality: Equality[VersionInformation] =
    (a, b) =>
      b match {
        case VersionInformation(mi, ver, _, _) => (a.messageInformation == mi) && (a.versionId == ver)
        case _                                 => false
      }

}
