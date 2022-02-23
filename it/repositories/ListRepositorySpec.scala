package repositories

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.scaladsl.Source
import akka.stream.testkit.scaladsl.TestSink
import base.ItSpecBase
import generators.BaseGenerators
import generators.ModelArbitraryInstances
import models.GenericListItem
import models.ListName
import models.VersionId
import models.VersionedListName
import org.mongodb.scala.bson.BsonDocument
import org.mongodb.scala.bson.BsonString
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Arbitrary
import org.scalacheck.Gen
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.BeforeAndAfterAll
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks.forAll
import play.api.libs.json.JsObject
import play.api.libs.json.Json
import uk.gov.hmrc.mongo.test.DefaultPlayMongoRepositorySupport

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ListRepositorySpec
    extends ItSpecBase
    with BaseGenerators
    with ModelArbitraryInstances
    with BeforeAndAfterEach
    with BeforeAndAfterAll
    with GuiceOneAppPerSuite
    with ScalaFutures
    with DefaultPlayMongoRepositorySupport[GenericListItem] {

  override protected def repository = new ListRepository(mongoComponent)

  private def seedData(documents: Seq[GenericListItem]): Unit =
    repository.collection
      .insertMany(documents)
      .toFuture
      .futureValue

  private def listOfItemsForVersion(versionId: VersionId): Gen[List[GenericListItem]] = {
    implicit val arbitraryVersionId: Arbitrary[VersionId] = Arbitrary(versionId)
    listWithMaxLength(5)(arbitraryGenericListItem)
  }

  "must create the following indexes" in {
    val indexes = repository.collection.listIndexes().toFuture().futureValue

    indexes.length mustEqual 3

    indexes(1).get("name").get mustEqual BsonString("version-id-index")
    indexes(1).get("key").get mustEqual BsonDocument("versionId" -> 1)

    indexes(2).get("name").get mustEqual BsonString("list-name-and-version-id-compound-index")
    indexes(2).get("key").get mustEqual BsonDocument("listName" -> 1, "versionId" -> 1)
  }

  "getListByName" - {

    implicit lazy val actorSystem: ActorSystem = ActorSystem()

    "returns the list items that match the specified VersionId" in {
      val versionId  = VersionId("1")
      val dataListV1 = listOfItemsForVersion(versionId).sample.value
      val dataListV2 = listOfItemsForVersion(VersionId("2")).sample.value
      val listName   = arbitrary[ListName].sample.value

      val targetList = dataListV1.map(_.copy(listName = listName))
      val otherList  = dataListV2.map(_.copy(listName = listName))

      seedData(targetList ++ otherList)

      val result: Future[Source[JsObject, NotUsed]] = repository.getListByName(VersionedListName(listName, versionId))

      val data = targetList
        .map(Json.toJsObject(_))
        .map(_ - "listName" - "snapshotDate" - "versionId" - "messageID")

      result.futureValue
        .runWith(TestSink.probe[JsObject])
        .request(targetList.length)
        .expectNextN(data)
    }

    "returns a source that completes immediately when there are no matching items" in {
      val versionId = VersionId("1")
      val listName  = arbitrary[ListName].sample.value

      val result: Future[Source[JsObject, NotUsed]] = repository.getListByName(VersionedListName(listName, versionId))

      result.futureValue
        .runWith(TestSink.probe[JsObject])
        .request(1)
        .expectComplete()
    }
  }

  "getListNames" - {

    "must return list of ListNames" in {

      val versionId = VersionId("2")

      forAll(listOfItemsForVersion(versionId), listOfItemsForVersion(VersionId("1234"))) {
        (genericListItems, oldList) =>
          seedData(oldList)
          seedData(genericListItems)

          val result = repository.getListNames(versionId).futureValue

          val expectedResult: Seq[ListName] = genericListItems.map(_.listName)

          result must contain allElementsOf expectedResult

          dropDatabase()
      }
    }

    "must return empty list when no list items are found" in {

      val result = repository.getListNames(VersionId("123")).futureValue

      result mustBe Nil
    }
  }

  "insertList" - {

    "must save a list" in {
      val list = listWithMaxLength[GenericListItem](10)(arbitraryGenericListItem).sample.value

      repository.insertList(list).futureValue mustBe SuccessfulWrite

      val result = findAll().futureValue

      result must contain theSameElementsAs list
    }
  }

}
