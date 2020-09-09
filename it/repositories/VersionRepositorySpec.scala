package repositories

import java.time.LocalDateTime

import base.ItSpecBase
import generators.BaseGenerators
import generators.ModelArbitraryInstances
import models.MessageInformation
import models.VersionId
import models.VersionInformation
import org.scalacheck.Arbitrary
import org.scalactic.Equality
import org.scalatest.BeforeAndAfterAll
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import reactivemongo.play.json.ImplicitBSONHandlers.JsObjectDocumentWriter
import reactivemongo.play.json.collection.JSONCollection
import play.api.inject.bind
import services.VersionIdProducer

import scala.concurrent.ExecutionContext.Implicits.global

class VersionRepositorySpec
    extends ItSpecBase
    with BaseGenerators
    with ModelArbitraryInstances
    with BeforeAndAfterEach
    with BeforeAndAfterAll
    with GuiceOneAppPerSuite
    with FailOnUnindexedQueries {

  override def beforeAll(): Unit = {
    database.flatMap(_.drop()).futureValue
    super.beforeAll()
    started(app).futureValue
  }

  override def beforeEach(): Unit = {
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

  val expectedVersionId                        = VersionId("1")
  val fakeVersionIdProducer: VersionIdProducer = () => expectedVersionId

  override def fakeApplication(): Application =
    new GuiceApplicationBuilder()
      .overrides(
        bind[VersionIdProducer].to(fakeVersionIdProducer)
      )
      .build()

  "save" - {
    "saves and a version number when the version information is successfully saved" in {
      val repo = app.injector.instanceOf[VersionRepository]

      val messageInformation = Arbitrary.arbitrary[MessageInformation].sample.value
      val result             = repo.save(messageInformation).futureValue

      val expectedVersionInformation = VersionInformation(messageInformation, expectedVersionId, LocalDateTime.now)

      result mustEqual expectedVersionId

      val savedVersionInformation =
        database.flatMap(_.collection[JSONCollection](VersionCollection.collectionName).find(Json.obj(), None).one[VersionInformation]).futureValue.value

      savedVersionInformation mustEqual expectedVersionInformation
    }
  }

  implicit val versionInformationEquality: Equality[VersionInformation] =
    (a, b) =>
      b match {
        case VersionInformation(mi, ver, _) => (a.messageInformation == mi) && (a.versionId == ver)
        case _                              => false
      }

}
