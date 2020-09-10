package repositories

import java.time.LocalDate

import base.ItSpecBase
import generators.{BaseGenerators, ModelArbitraryInstances}
import models.{GenericListItem, ListName, MetaData}
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach}
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks.forAll
import play.api.libs.json.{JsObject, Json}
import reactivemongo.api.{Cursor, DefaultDB}
import reactivemongo.bson.BSONObjectID
import reactivemongo.play.json.ImplicitBSONHandlers.JsObjectDocumentWriter
import reactivemongo.play.json.collection.JSONCollection
import repositories.ListRepository.SuccessfulWrite

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ListRepositorySpec
  extends ItSpecBase
    with BaseGenerators
    with ModelArbitraryInstances
    with BeforeAndAfterEach
    with BeforeAndAfterAll
    with GuiceOneAppPerSuite
    with MongoSuite
    with ScalaFutures {

  import ListRepositorySpec._

  override def beforeAll(): Unit = {
    database.flatMap(_.collection[JSONCollection](ListCollection.collectionName).drop(failIfNotFound = false)).futureValue
    super.beforeAll()
    started(app).futureValue
  }

  override def beforeEach(): Unit = {
    database
      .flatMap(
        _.collection[JSONCollection](ListCollection.collectionName)
          .delete()
          .one(Json.obj())
      )
      .futureValue

    super.beforeEach()
  }

  override def afterAll(): Unit = {
    database.flatMap(_.collection[JSONCollection](ListCollection.collectionName).drop(failIfNotFound = false)).futureValue
    super.afterAll()
  }

  private def seedData(database: Future[DefaultDB], data: Seq[JsObject]): Unit =
    database.flatMap {
      _.collection[JSONCollection](ListCollection.collectionName)
        .insert(ordered = true)
        .many(data)
    }.futureValue

  "getListByName" - {

    "must return list of JsObjects when" - {

      "single record found" in {

        seedData(database, Seq(sampleDataSet1))

        val repository = app.injector.instanceOf[ListRepository]
        val result = repository.getListByName(ListName("AdditionalInformationIdCommon"), MetaData("", LocalDate.now))
        result.futureValue mustBe List(sampleDataSet1)
      }

      "multiple records found" in {

        seedData(database, Seq(sampleDataSet1, sampleDataSet2))

        val repository = app.injector.instanceOf[ListRepository]
        val result = repository.getListByName(ListName("AdditionalInformationIdCommon"), MetaData("", LocalDate.now))
        result.futureValue mustBe List(sampleDataSet1, sampleDataSet2)
      }

      "no records found" in {

        val repository = app.injector.instanceOf[ListRepository]
        val result = repository.getListByName(ListName("AdditionalInformationIdCommon"), MetaData("", LocalDate.now))

        result.futureValue mustBe Nil
      }

    }
  }

  "getAllLists" - {

    "must return list of GenericListItem" in {

      forAll(listWithMaxLength(5)(arbitraryGenericListItem)) {
        genericListItems =>

          val jsObjectSeq = genericListItems.map(Json.toJsObject[GenericListItem])

          seedData(database, jsObjectSeq)

          val repository = app.injector.instanceOf[ListRepository]
          val result = repository.getAllLists.futureValue

          result mustBe genericListItems

          database.flatMap(_.drop()).futureValue
      }
    }

    "must return empty list when no list items are found" in {

      val repository = app.injector.instanceOf[ListRepository]
      val result = repository.getAllLists.futureValue

      result mustBe Nil
    }
  }

  "insertList" - {
    "must save a list" in {
      val list = listWithMaxLength[GenericListItem](10)(arbitraryGenericListItem).sample.value

      val repository = app.injector.instanceOf[ListRepository]

      repository.insertList(list).futureValue mustBe SuccessfulWrite

      val result =
        database
          .flatMap(
            _.collection[JSONCollection](ListCollection.collectionName)
              .find(Json.obj(), None)
              .cursor[GenericListItem]()
              .collect[Seq](11, Cursor.FailOnError())
          )
          .futureValue

      result must contain theSameElementsAs list
    }
  }

}

object ListRepositorySpec {

  val id1: BSONObjectID = BSONObjectID.generate()
  val id2: BSONObjectID = BSONObjectID.generate()
  val id3: BSONObjectID = BSONObjectID.generate()

  val sampleDataSet1: JsObject =
    Json.obj(
      "_id" -> id1.toString(),
      "listName" -> "AdditionalInformationIdCommon",
      "snapshotId" -> "snapshot",
      "state" -> "valid",
      "activeFrom" -> "2020-01-18",
      "code" -> "00100",
      "remark" -> "foo",
      "description" ->
        Json.obj(
          "en" -> "Simplified authorisation"
        )
    )

  val sampleDataSet2: JsObject =
    Json.obj(
      "_id" -> id2.toString(),
      "listName" -> "AdditionalInformationIdCommon",
      "snapshotId" -> "snapshot",
      "state" -> "valid",
      "activeFrom" -> "2020-01-18",
      "code" -> "00100",
      "remark" -> "foo",
      "description" ->
        Json.obj(
          "en" -> "Simplified authorisation"
        )
    )

  val sampleDataSetWithDifferentName: JsObject =
    Json.obj(
      "_id" -> id3.toString(),
      "listName" -> "AdditionalListName",
      "snapshotId" -> "snapshot",
      "state" -> "valid",
      "activeFrom" -> "2020-01-18",
      "code" -> "00100",
      "remark" -> "foo",
      "description" ->
        Json.obj(
          "en" -> "Simplified authorisation"
        )
    )
}
