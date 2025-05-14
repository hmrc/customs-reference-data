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
import org.mongodb.scala.bson.BsonDocument
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import uk.gov.hmrc.mongo.test.MongoSupport

import scala.concurrent.ExecutionContext.Implicits.global

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
          "versionId-index",
          BsonDocument("versionId" -> 1),
          None
        )
      )
    }
  }
}
