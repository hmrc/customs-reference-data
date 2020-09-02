package services

import java.time.LocalDate

import base.ItSpecBase
import generators.BaseGenerators
import models.MetaData
import org.scalatest.BeforeAndAfterAll
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import reactivemongo.play.json.collection.JSONCollection
import repositories.ListRepository
import repositories.MongoSuite
import repositories.ListCollection

import scala.concurrent.ExecutionContext.Implicits.global

class ReferenceDataServiceIntegrationSpec
    extends ItSpecBase
    with MongoSuite
    with BaseGenerators
    with BeforeAndAfterEach
    with BeforeAndAfterAll
    with GuiceOneAppPerSuite {

  import generators.ModelGenerators._

  override def beforeEach(): Unit = {
    database.flatMap(_.collection[JSONCollection](ListCollection.collectionName).drop(failIfNotFound = false))
    super.beforeEach()
  }

  override def afterAll(): Unit = {
    database.flatMap(_.collection[JSONCollection](ListCollection.collectionName).drop(failIfNotFound = false))
    super.afterAll()
  }

  "ReferenceDataService saves all the data items for each list" in {
    val data = genReferenceDataPayload(5, 5).sample.value

    app.injector.instanceOf[ReferenceDataService].insert(data).futureValue

    val listRepository = app.injector.instanceOf[ListRepository]

    data.listsNames.foreach {
      listName =>
        val retrievedList = listRepository.getList(listName, MetaData("", LocalDate.now)).futureValue

        retrievedList.length mustEqual 5
    }
  }
}
