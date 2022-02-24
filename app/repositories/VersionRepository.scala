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

import cats.data._
import cats.implicits._
import com.google.inject.Inject
import models.ApiDataSource.ColDataFeed
import models.ApiDataSource.RefDataFeed
import models._
import org.mongodb.scala.model.Filters
import org.mongodb.scala.model.IndexModel
import org.mongodb.scala.model.IndexOptions
import org.mongodb.scala.model.Indexes._
import services.consumption.TimeService
import uk.gov.hmrc.mongo.MongoComponent
import uk.gov.hmrc.mongo.play.json.PlayMongoRepository

import java.time.LocalDateTime
import javax.inject.Singleton
import scala.concurrent.ExecutionContext
import scala.concurrent.Future

@Singleton
class VersionRepository @Inject() (
  mongoComponent: MongoComponent,
  timeService: TimeService
)(implicit ec: ExecutionContext)
    extends PlayMongoRepository[VersionInformation](
      mongoComponent = mongoComponent,
      collectionName = "versions",
      domainFormat = VersionInformation.format,
      indexes = VersionRepository.indexes,
      replaceIndexes = true // TODO - remove or set to false after deployment of CTCTRADERS-2934 changes
    ) {

  def save(versionId: VersionId, messageInformation: MessageInformation, feed: ApiDataSource, listNames: Seq[ListName]): Future[Boolean] = {
    val time: LocalDateTime = timeService.now()
    val versionInformation  = VersionInformation(messageInformation, versionId, time, feed, listNames)

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
      .sort(descending("snapshotDate"))
      .headOption()

  def getLatestListNames: Future[Seq[ListName]] = {

    def getListName(source: ApiDataSource): Future[Option[Seq[ListName]]] =
      collection
        .find(Filters.eq("source", source.toString))
        .sort(descending("snapshotDate"))
        .headOption()
        .map(_.map(_.listNames))

    (for {
      list1 <- OptionT(getListName(RefDataFeed))
      list2 <- OptionT(getListName(ColDataFeed))
    } yield list1 ++ list2).value.map(_.getOrElse(Seq.empty))
  }

}

object VersionRepository {

  val indexes: Seq[IndexModel] = {
    val listNameAndSnapshotDateCompoundIndex: IndexModel =
      IndexModel(
        keys = compoundIndex(ascending("listNames.listName"), descending("snapshotDate")),
        indexOptions = IndexOptions().name("list-name-and-snapshot-date-compound-index")
      )

    val sourceAndSnapshotDateCompoundIndex: IndexModel =
      IndexModel(
        keys = compoundIndex(ascending("source"), descending("snapshotDate")),
        indexOptions = IndexOptions().name("source-and-snapshot-date-compound-index")
      )

    Seq(
      listNameAndSnapshotDateCompoundIndex,
      sourceAndSnapshotDateCompoundIndex
    )
  }
}
