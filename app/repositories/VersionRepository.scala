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
import models.*
import org.mongodb.scala.*
import org.mongodb.scala.bson.BsonValue
import org.mongodb.scala.model.*
import org.mongodb.scala.model.Indexes.*
import uk.gov.hmrc.mongo.MongoComponent
import uk.gov.hmrc.mongo.play.json.{Codecs, PlayMongoRepository}

import java.time.Instant
import java.time.temporal.ChronoUnit.SECONDS
import javax.inject.Singleton
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class VersionRepository @Inject() (
  mongoComponent: MongoComponent,
  config: AppConfig
)(implicit ec: ExecutionContext)
    extends PlayMongoRepository[VersionInformation](
      mongoComponent = mongoComponent,
      collectionName = VersionRepository.collectionName,
      domainFormat = VersionInformation.format,
      indexes = VersionRepository.indexes,
      replaceIndexes = config.replaceIndexes
    ) {

  override lazy val requiresTtlIndex: Boolean = false

  def save(
    versionId: VersionId,
    messageInformation: MessageInformation,
    feed: ApiDataSource,
    listNames: Seq[ListName],
    createdOn: Instant
  ): Future[Either[ErrorDetails, Unit]] = {
    val versionInformation = VersionInformation(messageInformation, versionId, createdOn, feed, listNames)

    collection
      .insertOne(versionInformation)
      .toFuture()
      .map(_.wasAcknowledged())
      .map {
        case true  => Right(())
        case false => Left(MongoError(s"Write was not acknowledge when saving version $versionId"))
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

  def getExpiredVersions(now: Instant, feed: ApiDataSource): Future[Seq[VersionId]] =
    val filter = Filters.and(
      Filters.eq("source", feed.toString),
      Filters.lt("createdOn", now.minus(config.ttl, SECONDS))
    )
    collection
      .find(filter)
      .toFuture()
      .map(_.map(_.versionId))

  def remove(versionIds: Seq[VersionId]): Future[Either[ErrorDetails, Unit]] = {
    val filter = Filters.in("versionId", versionIds.map(_.versionId)*)
    collection
      .deleteMany(filter)
      .toFuture()
      .map(_.wasAcknowledged())
      .map {
        case true  => Right(())
        case false => Left(MongoError(s"Failed to remove versions with version ID ${versionIds.mkString(", ")}"))
      }
  }
}

object VersionRepository {

  val collectionName: String = "v2-versions"

  val indexes: Seq[IndexModel] = {
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

    val versionIdIndex: IndexModel =
      IndexModel(
        keys = Indexes.ascending("versionId"),
        indexOptions = IndexOptions().name("versionId-index")
      )

    Seq(listNameAndDateCompoundIndex, sourceAndDateCompoundIndex, versionIdIndex)
  }
}
