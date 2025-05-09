/*
 * Copyright 2023 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package repositories

import base.SpecBase
import config.AppConfig
import models.ApiDataSource.ColDataFeed
import models.{ListName, MessageInformation, VersionId, VersionInformation, WriteError}
import org.mockito.ArgumentMatchers.{eq as eqTo, *}
import org.mockito.Mockito.when
import org.mongodb.scala.bson.BsonDocument
import org.mongodb.scala.result.InsertOneResult
import org.mongodb.scala.{MongoCollection, MongoDatabase, SingleObservable}
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import uk.gov.hmrc.mongo.MongoComponent
import uk.gov.hmrc.mongo.test.MongoSupport

import java.time.{Instant, LocalDate}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class VersionRepositorySpec extends SpecBase with GuiceOneAppPerSuite with BeforeAndAfterEach with MongoSupport {

  private val appConfig = app.injector.instanceOf[AppConfig]

  override def beforeEach(): Unit = {
    super.beforeEach()
    dropDatabase()
  }

  "indexes" - {
    "must return indexes" in {
      val repository = new VersionRepository(mongoComponent, appConfig)

      val indexes = repository.indexes.map(_.tupled()).toSet

      indexes mustEqual Set(
        (
          "list-name-and-date-compound-index",
          BsonDocument("listNames.listName" -> 1, "snapshotDate" -> -1, "createdOn" -> -1),
          None
        ),
        (
          "source-and-date-compound-index",
          BsonDocument("source" -> 1, "snapshotDate" -> -1, "createdOn" -> -1),
          None
        ),
        (
          "createdOn-index",
          BsonDocument("createdOn" -> 1),
          None
        ),
        (
          "versionId-index",
          BsonDocument("versionId" -> 1),
          None
        )
      )
    }
  }

  "save" - {
    "must return Left" - {
      "when write fails" in {
        val versionId          = "1"
        val messageInformation = MessageInformation("messageId", LocalDate.now())
        val listNames          = Seq(ListName("foo"))

        val mockMongoComponent: MongoComponent                       = mock[MongoComponent]
        val mockMongoDatabase: MongoDatabase                         = mock[MongoDatabase]
        val mockMongoCollection: MongoCollection[VersionInformation] = mock[MongoCollection[VersionInformation]]
        val mockSingleObservable: SingleObservable[InsertOneResult]  = mock[SingleObservable[InsertOneResult]]

        val repository = new VersionRepository(mockMongoComponent, appConfig)

        when(mockMongoComponent.database).thenReturn(mockMongoDatabase)
        when(mockMongoDatabase.getCollection[VersionInformation](eqTo(VersionRepository.collectionName))(any(), any())).thenReturn(mockMongoCollection)
        when(mockMongoCollection.insertOne(any())).thenReturn(mockSingleObservable)
        when(mockSingleObservable.toFuture()).thenReturn(Future.failed(new Throwable("something went wrong")))

        val result = repository.save(VersionId(versionId), messageInformation, ColDataFeed, listNames, Instant.now()).futureValue

        result.left.value mustEqual WriteError(s"Failed to save version $versionId")
      }
    }
  }
}
