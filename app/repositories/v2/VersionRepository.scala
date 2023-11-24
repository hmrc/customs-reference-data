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

import cats.data.EitherT
import com.google.inject.Inject
import config.AppConfig
import models._
import org.mongodb.scala.bson.BsonArray
import org.mongodb.scala.bson.BsonValue
import org.mongodb.scala.model.Indexes._
import org.mongodb.scala.model._
import play.api.Logging
import repositories.SuccessState
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
    )
    with Logging {

  override lazy val requiresTtlIndex: Boolean = VersionRepository.requiresTtlIndex(config)

  // TODO - Add more granular errors for the specific fails encountered
  private def otherError(error: String): Left[OtherError, Nothing] = {
    logger.warn(error)
    Left(OtherError(error))
  }

  def save(
    versionId: VersionId,
    messageInformation: MessageInformation,
    feed: ApiDataSource,
    listNames: Seq[ListName],
    createdOn: Instant
  ): EitherT[Future, ErrorDetails, SuccessState.type] = {
    val versionInformation = VersionInformation(messageInformation, versionId, createdOn, feed, listNames)

    EitherT(
      collection
        .insertOne(versionInformation)
        .toFuture()
        .map(_.wasAcknowledged())
        .map {
          case true  => Right(SuccessState)
          case false => otherError(versionId.versionId)
        }
        .recover {
          x =>
            otherError(s"Failed to save lists: $listNames - ${x.getMessage}")
        }
    )
  }

  def deleteListVersion(listNames: Seq[ListName], createdOn: Instant): EitherT[Future, ErrorDetails, SuccessState.type] =
    removeListNamesFromVersion(listNames, createdOn).flatMap(_ => deleteVersionsWithNoListNames())

  private def removeListNamesFromVersion(listNames: Seq[ListName], createdOn: Instant): EitherT[Future, ErrorDetails, SuccessState.type] =
    EitherT(
      collection
        .updateMany(
          Filters.lt("createdOn", createdOn),
          Updates.pull("listNames", Filters.in("listName", listNames.map(_.listName): _*))
        )
        .toFuture()
        .map(_.wasAcknowledged())
        .map {
          case true  => Right(SuccessState)
          case false => otherError(s"Failed to remove list names from array: $listNames")
        }
        .recover {
          x =>
            otherError(s"Failed to remove list names from array: $listNames - ${x.getMessage}")
        }
    )

  private def deleteVersionsWithNoListNames(): EitherT[Future, ErrorDetails, SuccessState.type] =
    EitherT(
      collection
        .deleteMany(Filters.eq("listNames", BsonArray()))
        .toFuture()
        .map(_.wasAcknowledged())
        .map {
          case true  => Right(SuccessState)
          case false => otherError("Failed to remove versions with no list names")
        }
        .recover {
          x =>
            otherError(s"Failed to remove versions with no list names - ${x.getMessage}")
        }
    )

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

}

object VersionRepository {

  def requiresTtlIndex(implicit config: AppConfig): Boolean = config.isP5TtlEnabled

  def indexes(implicit config: AppConfig): Seq[IndexModel] = {
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

    val listNamesIndex: IndexModel =
      IndexModel(
        keys = ascending("listNames"),
        indexOptions = IndexOptions().name("list-names-index")
      )

    lazy val createdOnIndex: IndexModel = IndexModel(
      keys = Indexes.ascending("createdOn"),
      indexOptions = {
        val baseOptions = IndexOptions().name("created-on-index")
        if (requiresTtlIndex) baseOptions.expireAfter(config.ttl, TimeUnit.SECONDS) else baseOptions
      }
    )

    Seq(
      listNameAndDateCompoundIndex,
      sourceAndDateCompoundIndex,
      listNamesIndex,
      createdOnIndex
    )
  }
}
