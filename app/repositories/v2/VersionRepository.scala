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

package repositories.v2

import com.google.inject.Inject
import config.AppConfig
import models._
import org.mongodb.scala.bson.BsonValue
import org.mongodb.scala.model.Indexes._
import org.mongodb.scala.model._
import repositories.SuccessfulVersionDelete
import repositories.VersionRepositoryDeleteResult
import repositories.FailedVersionDelete
import uk.gov.hmrc.mongo.MongoComponent
import uk.gov.hmrc.mongo.play.json.Codecs
import uk.gov.hmrc.mongo.play.json.PlayMongoRepository

import java.time.Instant
import java.util.concurrent.TimeUnit
import javax.inject.Singleton
import scala.concurrent.ExecutionContext
import scala.concurrent.Future

@Singleton
class VersionRepository @Inject() (
  mongoComponent: MongoComponent,
  config: AppConfig
)(implicit ec: ExecutionContext)
    extends PlayMongoRepository[VersionInformation](
      mongoComponent = mongoComponent,
      collectionName = "v2-versions",
      domainFormat = VersionInformation.format,
      indexes = VersionRepository.indexes(config),
      replaceIndexes = config.replaceIndexes
    ) {

  override lazy val requiresTtlIndex: Boolean = config.isP5TtlEnabled

  def save(
    versionId: VersionId,
    messageInformation: MessageInformation,
    feed: ApiDataSource,
    listNames: Seq[ListName],
    createdOn: Instant
  ): Future[Boolean] = {
    val versionInformation = VersionInformation(messageInformation, versionId, createdOn, feed, listNames)

    collection
      .insertOne(versionInformation)
      .toFuture()
      .map {
        _.wasAcknowledged()
      }
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

  def deleteOldImports(createdOn: Instant, versionId: VersionId): Future[VersionRepositoryDeleteResult] =
    collection
      .deleteMany(Filters.lt("createdOn", createdOn))
      .toFuture()
      .map(_.wasAcknowledged())
      .map {
        case true  => SuccessfulVersionDelete
        case false => FailedVersionDelete(versionId.versionId)
      }

}

object VersionRepository {

  def indexes(config: AppConfig): Seq[IndexModel] = {
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

    lazy val createdOnIndex: IndexModel = IndexModel(
      keys = Indexes.ascending("createdOn"),
      indexOptions = IndexOptions().name("ttl-index").expireAfter(config.ttl, TimeUnit.SECONDS)
    )

    Seq(listNameAndDateCompoundIndex, sourceAndDateCompoundIndex) ++ (if (config.isTtlEnabled) Seq(createdOnIndex) else Nil)
  }
}
