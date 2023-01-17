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

import com.google.inject.Inject
import config.AppConfig
import models.GenericListItem
import org.mongodb.scala.model.Indexes.ascending
import org.mongodb.scala.model.Indexes.compoundIndex
import org.mongodb.scala.model._
import play.api.Logging
import uk.gov.hmrc.mongo.MongoComponent
import uk.gov.hmrc.mongo.play.json.PlayMongoRepository

import javax.inject.Singleton
import scala.concurrent.ExecutionContext
import scala.concurrent.Future

@Singleton
class ListRepository @Inject() (
  mongoComponent: MongoComponent,
  config: AppConfig
)(implicit ec: ExecutionContext)
    extends PlayMongoRepository[GenericListItem](
      mongoComponent = mongoComponent,
      collectionName = "reference-data-lists",
      domainFormat = GenericListItem.format,
      indexes = ListRepository.indexes,
      replaceIndexes = config.replaceIndexes
    )
    with Logging {

  def dropCollection(): Future[Unit] = {
    logger.info("Dropping old collection")
    collection.drop().toFuture().map(_ => ())
  }
}

object ListRepository {

  val indexes: Seq[IndexModel] = {
    val listNameAndVersionIdCompoundIndex: IndexModel =
      IndexModel(
        keys = compoundIndex(ascending("listName"), ascending("versionId")),
        indexOptions = IndexOptions().name("list-name-and-version-id-compound-index")
      )

    Seq(
      listNameAndVersionIdCompoundIndex
    )
  }
}
