package repositories

import java.time.LocalDate

import base.ItSpecBase
import models.{ListName, MetaData}
import org.scalatest.concurrent.IntegrationPatience
import play.api.libs.json.{JsArray, JsObject, Json}
import play.api.test.Helpers.running
import reactivemongo.play.json.ImplicitBSONHandlers.JsObjectDocumentWriter
import reactivemongo.play.json.collection.JSONCollection

import scala.concurrent.ExecutionContext.Implicits.global


class ListRepositorySpec extends ItSpecBase with MongoSuite with IntegrationPatience {

  "getList" - {

    "must return a JsArray when a match is found" in {

      database.flatMap {
        db =>
          db.drop().flatMap {
            _ =>
              db.collection[JSONCollection](ListRepository.collectionName)
                .insert
                .one(sampleDataSet)
          }
      }.futureValue

      running(baseApplicationBuilder) {
        app =>

          val repository = app.injector.instanceOf[ListRepository]
          val result = repository.getList(ListName("AdditionalInformationIdCommon"), MetaData("", LocalDate.now))

          result.futureValue mustBe JsArray(Seq(sampleDataSet))
      }
    }


    "must return a JsArray when a match is found" in {

      database.flatMap {
        db =>
          db.drop().flatMap {
            _ =>
              db.collection[JSONCollection](ListRepository.collectionName)
                .insert
                .one(sampleDataSet)
          }
      }.futureValue

      running(baseApplicationBuilder) {
        app =>

          val repository = app.injector.instanceOf[ListRepository]
          val result = repository.getList(ListName("Invalid Name"), MetaData("", LocalDate.now))

          result.futureValue mustBe JsArray.empty
      }
    }
  }

  val sampleDataSet: JsObject =
    Json.obj(
      "listName"  -> "AdditionalInformationIdCommon",
      "snapshotId"    -> "snapshot",
      "state"         -> "valid",
      "activeFrom"    -> "2020-01-18",
      "code"          -> "00100",
      "remark"        -> "foo",
      "description"   ->
        Json.obj(
          "en" -> "Simplified authorisation"
        )
    )
}
