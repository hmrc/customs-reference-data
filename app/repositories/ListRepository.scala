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
import com.mongodb.client.model.InsertManyOptions
import config.AppConfig
import models._
import org.apache.pekko.NotUsed
import org.apache.pekko.stream.scaladsl.Source
import org.mongodb.scala.bson.BsonValue
import org.mongodb.scala.bson.conversions.Bson
import org.mongodb.scala.model.Indexes.ascending
import org.mongodb.scala.model.Indexes.compoundIndex
import org.mongodb.scala.model._
import play.api.libs.json.JsObject
import uk.gov.hmrc.mongo.MongoComponent
import uk.gov.hmrc.mongo.play.json.Codecs
import uk.gov.hmrc.mongo.play.json.PlayMongoRepository
import org.mongodb.scala._

import java.util.concurrent.TimeUnit
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
      collectionName = "v2-reference-data-lists-new",
      domainFormat = GenericListItem.format,
      indexes = ListRepository.indexes(config),
      replaceIndexes = config.replaceIndexes
    ) {

  override lazy val requiresTtlIndex: Boolean = config.isTtlEnabled

  def getListByName(listName: ListName, versionId: VersionId, filter: Option[FilterParams]): Source[JsObject, NotUsed] = {

    val standardFilters = Aggregates.filter(
      Filters.and(
        Filters.eq("listName", listName.listName),
        Filters.eq("versionId", versionId.versionId)
      )
    )

    val extraFilters: Seq[Bson] = filter
      .map {
        _.parameters
          .flatMap {
            case (_, Nil)           => None
            case (key, head :: Nil) => Some(Filters.eq(key, head))
            case (key, values)      => Some(Filters.in(key, values*))
          }
          .map(Aggregates.filter)
      }
      .getOrElse(Seq.empty)

    val projection = Aggregates.project(
      Projections.fields(
        Projections.include("data"),
        Projections.exclude("_id")
      )
    )

    Source.fromPublisher(
      collection
        .aggregate[BsonValue](Seq(standardFilters, projection) ++ extraFilters)
        .allowDiskUse(true)
        .map(Codecs.fromBson[JsObject](_))
    )
  }

  def insertList(list: GenericList): Future[ListRepositoryWriteResult] =
    collection
      .insertMany(list.entries, new InsertManyOptions().ordered(true))
      .toFuture()
      .map(_.wasAcknowledged())
      .map {
        case true  => SuccessfulWrite(list)
        case false => FailedWrite(list)
      }

  // TODO - define some method to delete any document with a version ID not in the list of version IDs
  def remove(versionIds: Seq[VersionId]) = {
    val filter = Filters.nin("versionId", versionIds)
    ???
  }
}

object ListRepository {

  // TODO - remove TTL index
  def indexes(config: AppConfig): Seq[IndexModel] = {
    val listNameAndVersionIdCompoundIndex: IndexModel = IndexModel(
      keys = compoundIndex(ascending("listName"), ascending("versionId")),
      indexOptions = IndexOptions().name("list-name-and-version-id-compound-index")
    )

    lazy val createdOnIndex: IndexModel = IndexModel(
      keys = Indexes.ascending("createdOn"),
      indexOptions = IndexOptions().name("ttl-index").expireAfter(config.ttl.asInstanceOf[Number].longValue, TimeUnit.SECONDS)
    )

    listNameAndVersionIdCompoundIndex +: (if (config.isTtlEnabled) Seq(createdOnIndex) else Nil)
  }
}
