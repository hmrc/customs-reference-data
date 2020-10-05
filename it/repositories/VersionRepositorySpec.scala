package repositories

import java.time.LocalDate
import java.time.LocalDateTime

import base.ItSpecBase
import generators.BaseGenerators
import generators.ModelArbitraryInstances
import models.{ListName, MessageInformation, VersionId, VersionInformation}
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
import services.ingestion.VersionIdProducer

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
      val listName = Arbitrary.arbitrary[ListName].sample.value

      val expectedVersionId = VersionId("1")
      when(mockVersionIdProducer.apply()).thenReturn(expectedVersionId)
      when(mockTimeService.now()).thenReturn(LocalDateTime.now())

      val result = repo.save(messageInformation, Seq(listName)).futureValue

      val expectedVersionInformation = VersionInformation(messageInformation, expectedVersionId, LocalDateTime.now, Seq(listName))

      result mustEqual expectedVersionId

      val savedVersionInformation =
        database.flatMap(_.collection[JSONCollection](VersionCollection.collectionName).find(Json.obj(), None).one[VersionInformation]).futureValue.value

      savedVersionInformation mustEqual expectedVersionInformation
    }
  }

  "getLatest" - {
    "gets the most recent version information by snapshotDate" in {
      val repo = app.injector.instanceOf[VersionRepository]

      val recentDate = LocalDate.now()
      val createdOn  = LocalDateTime.now()

      when(mockVersionIdProducer.apply()).thenReturn(VersionId("1"), VersionId("2"))
      when(mockTimeService.now()).thenReturn(createdOn)

      val messageInformation = Arbitrary.arbitrary[MessageInformation].sample.value
      val listName = Arbitrary.arbitrary[ListName].sample.value

      repo.save(messageInformation.copy(snapshotDate = recentDate.minusDays(1)), Seq(listName)).futureValue
      repo.save(messageInformation.copy(snapshotDate = recentDate), Seq(listName)).futureValue

      val expectedVersionInformation = VersionInformation(messageInformation.copy(snapshotDate = recentDate), VersionId("2"), createdOn, Seq(listName))

      val result: VersionInformation = repo.getLatest.futureValue.value

      result mustEqual expectedVersionInformation
    }
  }

  implicit val versionInformationEquality: Equality[VersionInformation] =
    (a, b) =>
      b match {
        case VersionInformation(mi, ver, _, _) => (a.messageInformation == mi) && (a.versionId == ver)
        case _                              => false
      }

}
