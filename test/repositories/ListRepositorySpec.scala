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
import org.mockito.Mockito.reset
import org.mockito.Mockito.when
import org.mongodb.scala.bson.BsonDocument
import org.mongodb.scala.bson.BsonNumber
import org.mongodb.scala.bson.BsonString
import org.scalatest.BeforeAndAfterEach

class ListRepositorySpec extends SpecBase with BeforeAndAfterEach {
  private val mockConfig = mock[AppConfig]
  private val ttl        = 5

  override def beforeEach(): Unit = {
    super.beforeEach()

    reset(mockConfig)
    when(mockConfig.ttl).thenReturn(ttl)
  }

  "indexes" - {
    "when Ttl index is enabled" - {
      "must return 2 indexes" in {
        when(mockConfig.isTtlEnabled).thenReturn(true)

        val indexes = ListRepository.indexes(mockConfig).map(_.tupled()).toSet

        indexes mustEqual Set(
          (
            BsonString("list-name-and-version-id-compound-index"),
            BsonDocument("listName" -> 1, "versionId" -> 1),
            None
          ),
          (
            BsonString("ttl-index"),
            BsonDocument("createdOn" -> 1),
            Some(BsonNumber(ttl))
          )
        )
      }
    }
  }
}
