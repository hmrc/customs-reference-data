/*
 * Copyright 2022 HM Revenue & Customs
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
import models._
import org.mongodb.scala.bson.BsonValue
import org.mongodb.scala.model.Indexes._
import org.mongodb.scala.model._
import play.api.Logging
import services.consumption.TimeService
import uk.gov.hmrc.mongo.MongoComponent
import uk.gov.hmrc.mongo.play.json.Codecs
import uk.gov.hmrc.mongo.play.json.PlayMongoRepository

import java.time.LocalDateTime
import javax.inject.Singleton
import scala.concurrent.ExecutionContext
import scala.concurrent.Future

@Singleton
class VersionRepository @Inject() (
  mongoComponent: MongoComponent,
  timeService: TimeService,
  config: AppConfig
)(implicit ec: ExecutionContext)
    extends PlayMongoRepository[VersionInformation](
      mongoComponent = mongoComponent,
      collectionName = "versions",
      domainFormat = VersionInformation.format,
      indexes = VersionRepository.indexes,
      replaceIndexes = config.replaceIndexes
    )
    with Logging {

  def save(versionId: VersionId, messageInformation: MessageInformation, feed: ApiDataSource, listNames: Seq[ListName]): Future[Boolean] = {
    val time: LocalDateTime = timeService.now()
    val versionInformation  = VersionInformation(messageInformation, versionId, time, feed, listNames)

    collection
      .insertOne(versionInformation)
      .toFuture()
      .map(_.wasAcknowledged())
  }

  def getLatest(listName: ListName): Future[Option[VersionInformation]] =
    collection
      .find(Filters.eq("listNames.listName", listName.listName))
      .sort(descending("snapshotDate", "createdOn"))
      .headOption()

  def getLatestListNames: Future[Seq[ListName]] = {
    val sort  = Aggregates.sort(descending("snapshotDate", "createdOn"))
    val group = Aggregates.group("$source", Accumulators.first("listNames", "$listNames"))

    collection
      .aggregate[BsonValue](Seq(sort, group))
      .map(Codecs.fromBson[ListNames](_))
      .toFuture()
      .map(_.flatMap(_.listNames))
  }

  def getLatestVersionIds: Future[Seq[VersionId]] = {
    val sort  = Aggregates.sort(descending("snapshotDate", "createdOn"))
    val group = Aggregates.group("$listNames.listName", Accumulators.first("versionId", "$versionId"))

    collection
      .aggregate[BsonValue](Seq(sort, group))
      .map(Codecs.fromBson[VersionId](_))
      .toFuture()
  }

  def deleteOutdatedDocuments(latestVersionIds: Seq[VersionId]): Future[Boolean] =
    collection
      .deleteMany(Filters.nin("versionId", latestVersionIds.map(_.versionId): _*))
      .toFuture()
      .map {
        x =>
          logger.info(s"[VersionRepository][deleteOutdatedDocuments] Deleted ${x.getDeletedCount} documents")
          true
      }
      .recover {
        case e =>
          logger.error(s"Error deleting documents: ${e.getMessage}")
          false
      }

}

object VersionRepository {

  val indexes: Seq[IndexModel] = {
    val versionIdIndex: IndexModel =
      IndexModel(
        keys = ascending("versionId"),
        indexOptions = IndexOptions().name("version-id-index")
      )

    val listNameAndDateCompoundIndex: IndexModel =
      IndexModel(
        keys = compoundIndex(ascending("listNames.listName"), descending("snapshotDate", "createdOn")),
        indexOptions = IndexOptions().name("list-name-and-date-compound-index")
      )

    val sourceAndDateCompoundIndex: IndexModel =
      IndexModel(
        keys = compoundIndex(ascending("source"), descending("snapshotDate", "createdOn")),
        indexOptions = IndexOptions().name("source-and-date-compound-index")
      )

    Seq(
      versionIdIndex,
      listNameAndDateCompoundIndex,
      sourceAndDateCompoundIndex
    )
  }
}
