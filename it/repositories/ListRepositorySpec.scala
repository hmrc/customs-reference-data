package repositories

import java.time.LocalDate

import base.ItSpecBase
import generators.{BaseGenerators, ModelArbitraryInstances}
import models.{GenericListItem, ListName, MetaData}
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.{JsObject, Json}
import play.api.test.Helpers.running
import reactivemongo.api.Cursor
import reactivemongo.bson.BSONObjectID
import reactivemongo.play.json.ImplicitBSONHandlers.JsObjectDocumentWriter
import reactivemongo.play.json.collection.JSONCollection
import repositories.ListRepository.SuccessfulWrite

import scala.concurrent.ExecutionContext.Implicits.global

class ListRepositorySpec
    extends ItSpecBase
    with FailOnUnindexedQueries
    with BaseGenerators
    with ModelArbitraryInstances {

  import ListRepositorySpec._

  "getList" - {

    "must handle" - {

      "single record found" in {

        database.flatMap(_.drop()).futureValue

        val app: Application = new GuiceApplicationBuilder().build()

        running(app) {
          started(app).futureValue

          val repository = app.injector.instanceOf[ListRepository]

          database.flatMap {
            _.collection[JSONCollection](ListCollection.collectionName)
              .insert(ordered = true)
              .many(Seq(sampleDataSet1))
          }.futureValue

          val result = repository.getList(ListName("AdditionalInformationIdCommon"), MetaData("", LocalDate.now)).futureValue

          result mustBe List(sampleDataSet1)
        }
      }

      "multiple records found" in {

        database.flatMap(_.drop()).futureValue

        val app: Application = new GuiceApplicationBuilder().build()

        running(app) {
          started(app).futureValue

          val repository = app.injector.instanceOf[ListRepository]

          database.flatMap {
            _.collection[JSONCollection](ListCollection.collectionName)
              .insert(ordered = true)
              .many(Seq(sampleDataSet1, sampleDataSet2))
          }.futureValue

          val result = repository.getList(ListName("AdditionalInformationIdCommon"), MetaData("", LocalDate.now)).futureValue

          result mustBe List(sampleDataSet1, sampleDataSet2)
        }
      }

      "no records found" in {

        database.flatMap(_.drop()).futureValue

        val app: Application = new GuiceApplicationBuilder().build()

        running(app) {
          started(app).futureValue

          val repository = app.injector.instanceOf[ListRepository]

          val result = repository.getList(ListName("AdditionalInformationIdCommon"), MetaData("", LocalDate.now)).futureValue

          result mustBe Nil
        }
      }

    }
  }

  "insertList" - {
    "must save a list" in {

      database.flatMap(_.drop()).futureValue

      val app: Application = new GuiceApplicationBuilder().build()

      running(app) {
        started(app).futureValue

        val list = listWithMaxLength[GenericListItem](10).sample.value

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
  }

}
