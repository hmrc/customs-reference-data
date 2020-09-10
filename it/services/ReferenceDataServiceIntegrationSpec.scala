package services

import java.time.LocalDate

import base.ItSpecBase
import generators.BaseGenerators
import models._
import org.scalacheck.Arbitrary
import org.scalacheck.Gen
import org.scalatest.BeforeAndAfterAll
import org.scalatest.BeforeAndAfterEach
import org.scalatest.matchers.MatchResult
import org.scalatest.matchers.Matcher
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks
import play.api.Application
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.JsObject
import play.api.libs.json.Json
import reactivemongo.play.json.ImplicitBSONHandlers.JsObjectDocumentWriter
import reactivemongo.play.json.collection.JSONCollection
import repositories.ListCollection
import repositories.ListRepository
import repositories.MongoSuite
import repositories.VersionCollection

import scala.concurrent.ExecutionContext.Implicits.global

class ReferenceDataServiceIntegrationSpec
    extends ItSpecBase
    with MongoSuite
    with BaseGenerators
    with BeforeAndAfterEach
    with BeforeAndAfterAll
    with GuiceOneAppPerSuite
    with ScalaCheckDrivenPropertyChecks {

  import generators.ModelArbitraryInstances._
  import generators.ModelGenerators._

  val expectedVersionId                        = VersionId("1234")
  val fakeVersionIdProducer: VersionIdProducer = () => expectedVersionId

  override def fakeApplication(): Application =
    new GuiceApplicationBuilder()
      .overrides(bind[VersionIdProducer].toInstance(fakeVersionIdProducer))
      .build()

  "saves all the data items for each list" in {
    val messageInformation = Arbitrary.arbitrary[MessageInformation].sample.value
    val json               = genReferenceDataJson(5, 5, messageInformation = Some(Gen.const(messageInformation))).sample.value
    val data               = ReferenceDataPayload(json)
    val expectedListNames  = (json \ "lists").as[JsObject].keys.map(ListName(_))

    app.injector.instanceOf[ReferenceDataService].insert(data).futureValue

    val listRepository = app.injector.instanceOf[ListRepository]

    expectedListNames.nonEmpty mustBe true

    expectedListNames.foreach {
      listName =>
        val retrievedList = listRepository.getList(listName, MetaData("", LocalDate.now)).futureValue

        retrievedList.length mustEqual 5

        retrievedList.foreach {
          _ must haveListInfo(
            expectedListName = listName,
            expectedSnapshotDate = messageInformation.snapshotDate,
            expectedVersionId = expectedVersionId
          )
        }
    }
  }

  def haveListInfo(expectedListName: ListName, expectedSnapshotDate: LocalDate, expectedVersionId: VersionId): Matcher[JsObject] =
    entryItem => {
      import models.MongoDateTimeFormats._

      val entryListName  = entryItem.as[ListName]
      val entryLocalDate = (entryItem \ "snapshotDate").as[LocalDate]
      val entryVersionId = entryItem.as[VersionId]

      MatchResult(
        (entryListName == expectedListName) && (entryLocalDate == expectedSnapshotDate) && (entryVersionId == expectedVersionId),
        s"""Expected (listName: `$entryListName`, snapshotDate: $entryLocalDate, versionId: $entryVersionId) to equal (listName: `$expectedListName`, snapshotDate: $expectedSnapshotDate, versionId: $expectedVersionId)""",
        s"""Expected (listName: `$entryListName`, snapshotDate: $entryLocalDate, versionId: $entryVersionId) to not equal (listName: `$expectedListName`, snapshotDate: $expectedSnapshotDate, versionId: $expectedVersionId)"""
      )
    }

  override def beforeAll(): Unit = {
    database.flatMap(_.drop()).futureValue
    started(app)
    super.beforeAll()
  }

  override def beforeEach(): Unit = {
    database
      .flatMap(
        _.collection[JSONCollection](ListCollection.collectionName)
          .delete()
          .one(Json.obj())
      )
      .futureValue

    database
      .flatMap(
        _.collection[JSONCollection](VersionCollection.collectionName)
          .delete()
          .one(Json.obj())
      )
      .futureValue
    super.beforeEach()
  }

  override def afterAll(): Unit = {
    database.flatMap(_.drop()).futureValue
    super.afterAll()
  }
}
