package services

import java.time.LocalDate

import base.ItSpecBase
import generators.BaseGenerators
import models.ApiDataSource.ColDataFeed
import models.ApiDataSource.RefDataFeed
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
import reactivemongo.play.json.collection.Helpers.idWrites
import reactivemongo.play.json.collection.JSONCollection
import repositories.{ListCollection, MongoSuite, VersionCollection, VersionIdProducer}
import services.consumption.ListRetrievalService
import services.ingestion.ReferenceDataService

import scala.concurrent.ExecutionContext.Implicits.global

class InsertAndRetrieveIntegrationSpec
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

  "saves all the data items for each list for reference data list" in {
    val messageInformation = Arbitrary.arbitrary[MessageInformation].sample.value
    val json               = genReferenceDataListsJson(5, 5, messageInformation = Some(Gen.const(messageInformation))).sample.value
    val data               = ReferenceDataListsPayload(json)
    val expectedListNames  = (json \ "lists").as[JsObject].keys.map(ListName(_))

    app.injector.instanceOf[ReferenceDataService].insert(RefDataFeed, data).futureValue

    val listRetrievalService = app.injector.instanceOf[ListRetrievalService]

    expectedListNames.nonEmpty mustBe true

    expectedListNames.foreach {
      listName =>
        val result = listRetrievalService.getList(listName).futureValue.value

        result.id mustEqual listName
        result.metaData.version mustEqual expectedVersionId.versionId
        result.metaData.snapshotDate mustEqual messageInformation.snapshotDate
        result.data mustEqual (json \ "lists" \ listName.listName \ "listEntries").toOption.value.as[List[JsObject]]

    }
  }

  "saves all the data items for each list for customs office list" in {
    val messageInformation = Arbitrary.arbitrary[MessageInformation].sample.value
    val json               = genReferenceDataListsJson(5, 5, messageInformation = Some(Gen.const(messageInformation))).sample.value
    val data               = ReferenceDataListsPayload(json)
    val expectedListNames  = (json \ "lists").as[JsObject].keys.map(ListName(_))

    app.injector.instanceOf[ReferenceDataService].insert(ColDataFeed, data).futureValue

    val listRetrievalService = app.injector.instanceOf[ListRetrievalService]

    expectedListNames.nonEmpty mustBe true

    expectedListNames.foreach {
      listName =>
        val result = listRetrievalService.getList(listName).futureValue.value

        result.id mustEqual listName
        result.metaData.version mustEqual expectedVersionId.versionId
        result.metaData.snapshotDate mustEqual messageInformation.snapshotDate
        result.data mustEqual (json \ "lists" \ listName.listName \ "listEntries").toOption.value.as[List[JsObject]]

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
    dropDatabase()
    createCollections()
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
    dropDatabase()
    super.afterAll()
  }
}
