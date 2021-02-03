package repositories

import akka.actor.ActorSystem
import akka.stream.{ActorMaterializer, Materializer}
import akka.stream.scaladsl.Source
import akka.stream.testkit.scaladsl.TestSink
import base.ItSpecBase
import generators.BaseGenerators
import generators.ModelArbitraryInstances
import models.GenericListItem
import models.ListName
import models.VersionId
import models.VersionedListName
import org.scalacheck.Arbitrary
import org.scalacheck.Arbitrary.arbitrary
import org.scalatest.BeforeAndAfterAll
import org.scalatest.BeforeAndAfterEach
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks.forAll
import play.api.libs.json.JsObject
import play.api.libs.json.Json
import reactivemongo.api.Cursor
import reactivemongo.api.DefaultDB
import reactivemongo.play.json.ImplicitBSONHandlers.JsObjectDocumentWriter
import reactivemongo.play.json.collection.JSONCollection
import repositories.SuccessfulWrite

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

  def listOfItemsForVersion(versionId: VersionId) = {
    implicit val arbitraryVersionId: Arbitrary[VersionId] = Arbitrary(versionId)
    listWithMaxLength(5)(arbitraryGenericListItem)
  }

  "getListByNameSource" - {

    implicit lazy val actorSystem: ActorSystem = ActorSystem()
    implicit lazy val mat: Materializer        = ActorMaterializer()

    "returns the list items that match the specified VersionId" in {
      val versionId  = VersionId("1")
      val dataListV1 = listOfItemsForVersion(versionId).sample.value
      val dataListV2 = listOfItemsForVersion(VersionId("2")).sample.value
      val listName   = arbitrary[ListName].sample.value

      val targetList = dataListV1.map(_.copy(listName = listName)).map(Json.toJsObject(_))
      val otherList  = dataListV2.map(_.copy(listName = listName)).map(Json.toJsObject(_))

      seedData(database, targetList ++ otherList)

      val repository = app.injector.instanceOf[ListRepository]

      val result: Future[Source[JsObject, Future[_]]] = repository.getListByNameSource(VersionedListName(listName, versionId))

      val data = targetList.map(x => (x - "listName" - "snapshotDate" - "versionId" - "messageID"))

      result
        .futureValue
        .runWith(TestSink.probe[JsObject])
        .request(targetList.length)
        .expectNextN(data)
    }

    "returns a source that completes immediately when there are no matching items" in {
      val versionId  = VersionId("1")
      val listName   = arbitrary[ListName].sample.value
      val repository = app.injector.instanceOf[ListRepository]

      val result: Future[Source[JsObject, Future[_]]] = repository.getListByNameSource(VersionedListName(listName, versionId))

      result
        .futureValue
        .runWith(TestSink.probe[JsObject])
        .request(1)
        .expectComplete()
    }
  }

  "getListByName" - {

    "returns the list items that match the specified VersionId" in {
      val versionId  = VersionId("1")
      val dataListV1 = listOfItemsForVersion(versionId).sample.value
      val dataListV2 = listOfItemsForVersion(VersionId("2")).sample.value
      val listName   = arbitrary[ListName].sample.value

      val targetList = dataListV1.map(_.copy(listName = listName)).map(Json.toJsObject(_))
      val otherList  = dataListV2.map(_.copy(listName = listName)).map(Json.toJsObject(_))

      seedData(database, targetList ++ otherList)

      val repository = app.injector.instanceOf[ListRepository]

      val result = repository.getListByName(VersionedListName(listName, versionId))

      val expectedResult = targetList.map(parentData => (parentData \ "data").getOrElse(JsObject.empty))

      result.futureValue mustBe expectedResult
    }

    "returns the list items that match the list name" in {
      val versionId     = VersionId("1")
      val dataListV1    = listOfItemsForVersion(versionId).sample.value
      val dataListV2    = listOfItemsForVersion(versionId).sample.value
      val listName      = ListName("l1")
      val otherlistName = ListName("l2")

      val targetList = dataListV1.map(_.copy(listName = listName)).map(Json.toJsObject(_))
      val otherList  = dataListV2.map(_.copy(listName = otherlistName)).map(Json.toJsObject(_))

      seedData(database, targetList ++ otherList)

      val repository = app.injector.instanceOf[ListRepository]

      val result = repository.getListByName(VersionedListName(listName, versionId))

      val expectedResult = targetList.map(parentData => (parentData \ "data").getOrElse(JsObject.empty))

      result.futureValue mustBe expectedResult
    }

    "returns an empty list when there are no items that that match the list name" in {
      val versionId = VersionId("1")
      val listItem  = arbitrary[GenericListItem].sample.value
      val listName  = ListName("l1")

      val targetList = Json.toJsObject(listItem.copy(listName = listName, versionId = versionId))

      seedData(database, Seq(targetList))

      val repository = app.injector.instanceOf[ListRepository]

      val result = repository.getListByName(VersionedListName(ListName("other"), versionId))

      result.futureValue mustBe Nil
    }

    "returns an empty list when there are no items that that the version Id for a list name " in {
      val versionId = VersionId("1")
      val listItem  = arbitrary[GenericListItem].sample.value
      val listName  = ListName("l1")

      val targetList = Json.toJsObject(listItem.copy(listName = listName, versionId = versionId))

      seedData(database, Seq(targetList))

      val repository = app.injector.instanceOf[ListRepository]

      val result = repository.getListByName(VersionedListName(listName, VersionId("2")))

      result.futureValue mustBe Nil
    }
  }

  "getListNames" - {

    "must return list of ListNames" in {

      val toJsObjectSeq: List[GenericListItem] => Seq[JsObject] = _.map(Json.toJsObject[GenericListItem])

      val versionId = VersionId("2")

      forAll(listOfItemsForVersion(versionId), listOfItemsForVersion(VersionId("1234"))) {
        (genericListItems, oldList) =>
          seedData(database, toJsObjectSeq(oldList))
          seedData(database, toJsObjectSeq(genericListItems))

          val repository = app.injector.instanceOf[ListRepository]
          val result     = repository.getListNames(versionId).futureValue

          val expectedResult: Seq[ListName] = genericListItems.map(_.listName)

          result mustBe expectedResult

          database.flatMap(_.drop()).futureValue
      }
    }

    "must return empty list when no list items are found" in {

      val repository = app.injector.instanceOf[ListRepository]
      val result     = repository.getListNames(VersionId("123")).futureValue

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
