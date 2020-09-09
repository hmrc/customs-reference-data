package repositories

import java.time.LocalDate

import base.ItSpecBase
import generators.BaseGenerators
import generators.ModelArbitraryInstances
import models.GenericListItem
import models.ListName
import models.MessageInformation
import models.MetaData
import org.scalatest.BeforeAndAfterAll
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.libs.json.JsObject
import play.api.libs.json.Json
import reactivemongo.api.Cursor
import reactivemongo.api.DefaultDB
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
    with FailOnUnindexedQueries {

  import ListRepositorySpec._

  override def beforeAll(): Unit = {
    database.flatMap(_.drop()).futureValue
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
    database.flatMap(_.drop()).futureValue
    super.afterAll()
  }

  private def seedData(database: Future[DefaultDB], data: Seq[JsObject]): Unit =
    database.flatMap {
      _.collection[JSONCollection](ListCollection.collectionName)
        .insert(ordered = true)
        .many(data)
    }.futureValue

  "getList" - {

    "must handle" - {

      "single record found" in {

        seedData(database, Seq(sampleDataSet1))

        val repository = app.injector.instanceOf[ListRepository]
        val result     = repository.getList(ListName("AdditionalInformationIdCommon"), MetaData("", LocalDate.now))
        result.futureValue mustBe List(sampleDataSet1)
      }

      "multiple records found" in {

        seedData(database, Seq(sampleDataSet1, sampleDataSet2))

        val repository = app.injector.instanceOf[ListRepository]
        val result     = repository.getList(ListName("AdditionalInformationIdCommon"), MetaData("", LocalDate.now))
        result.futureValue mustBe List(sampleDataSet1, sampleDataSet2)
      }

      "no records found" in {

        val repository = app.injector.instanceOf[ListRepository]
        val result     = repository.getList(ListName("AdditionalInformationIdCommon"), MetaData("", LocalDate.now))

        result.futureValue mustBe Nil
      }

    }
  }

  "insertList" - {
    "must save a list" in {
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

  val sampleDataSet1: JsObject = Json.toJsObject(
    GenericListItem(
      listName = ListName("AdditionalInformationIdCommon"),
      messageInformation = MessageInformation(
        messageId = "1",
        snapshotDate = LocalDate.now()
      ),
      data = Json.obj(
        "snapshotId" -> "snapshot",
        "state"      -> "valid",
        "activeFrom" -> "2020-01-18",
        "code"       -> "00100",
        "remark"     -> "foo",
        "description" ->
          Json.obj(
            "en" -> "Simplified authorisation"
          )
      )
    )
  ) ++ Json.obj("_id" -> id1.toString())

  val sampleDataSet2: JsObject = Json.toJsObject(
    GenericListItem(
      listName = ListName("AdditionalInformationIdCommon"),
      messageInformation = MessageInformation(
        messageId = "1",
        snapshotDate = LocalDate.now()
      ),
      data = Json.obj(
        "snapshotId" -> "snapshot",
        "state"      -> "valid",
        "activeFrom" -> "2020-01-18",
        "code"       -> "00100",
        "remark"     -> "foo",
        "description" ->
          Json.obj(
            "en" -> "Simplified authorisation"
          )
      )
    )
  ) ++ Json.obj("_id" -> id2.toString())
}
