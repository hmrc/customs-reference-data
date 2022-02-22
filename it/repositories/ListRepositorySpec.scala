package repositories

import akka.actor.ActorSystem
import akka.stream.scaladsl.Source
import akka.stream.testkit.scaladsl.TestSink
import base.ItSpecBase
import generators.{BaseGenerators, ModelArbitraryInstances}
import models.{GenericListItem, ListName, VersionId, VersionedListName}
import org.scalacheck.{Arbitrary, Gen}
import org.scalacheck.Arbitrary.arbitrary
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach}
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks.forAll
import play.api.libs.json.{JsObject, Json}
import reactivemongo.api.indexes.IndexType
import reactivemongo.api.{Cursor, DefaultDB}
import reactivemongo.play.json.collection.Helpers.idWrites
import reactivemongo.play.json.collection.JSONCollection

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Await, Future}
import scala.concurrent.duration.Duration

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

    super.beforeEach()
  }

  override def afterAll(): Unit = {
    dropDatabase()
    super.afterAll()
  }

  private def seedData(database: Future[DefaultDB], data: Seq[JsObject]): Unit =
    database.flatMap {
      _.collection[JSONCollection](ListCollection.collectionName)
        .insert(ordered = true)
        .many(data)
    }.futureValue

  private def listOfItemsForVersion(versionId: VersionId): Gen[List[GenericListItem]] = {
    implicit val arbitraryVersionId: Arbitrary[VersionId] = Arbitrary(versionId)
    listWithMaxLength(5)(arbitraryGenericListItem)
  }

  "must create the following indexes" in {
    val repository = app.injector.instanceOf[ListRepository]
    Await.result(repository.insertList(Nil), Duration.Inf) // triggers index creation

    val indexes = database.flatMap {
      result =>
        result.collection[JSONCollection](ListCollection.collectionName).indexesManager.list()
    }.futureValue.map {
      index =>
        (index.name.get, index.key)
    }

    indexes must contain theSameElementsAs List(
      ("list-name-and-version-id-compound-index", Seq("listName" -> IndexType.Ascending, "versionId" -> IndexType.Ascending)),
      ("_id_", Seq(("_id", IndexType.Ascending)))
    )
  }

  "getListByName" - {

    implicit lazy val actorSystem: ActorSystem = ActorSystem()

    "returns the list items that match the specified VersionId" in {
      val versionId  = VersionId("1")
      val dataListV1 = listOfItemsForVersion(versionId).sample.value
      val dataListV2 = listOfItemsForVersion(VersionId("2")).sample.value
      val listName   = arbitrary[ListName].sample.value

      val targetList = dataListV1.map(_.copy(listName = listName)).map(Json.toJsObject(_))
      val otherList  = dataListV2.map(_.copy(listName = listName)).map(Json.toJsObject(_))

      seedData(database, targetList ++ otherList)

      val repository = app.injector.instanceOf[ListRepository]

      val result: Future[Source[JsObject, Future[_]]] = repository.getListByName(VersionedListName(listName, versionId))

      val data = targetList.map(_ - "listName" - "snapshotDate" - "versionId" - "messageID")

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

      val result: Future[Source[JsObject, Future[_]]] = repository.getListByName(VersionedListName(listName, versionId))

      result
        .futureValue
        .runWith(TestSink.probe[JsObject])
        .request(1)
        .expectComplete()
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

          result must contain allElementsOf expectedResult

          dropDatabase()
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
