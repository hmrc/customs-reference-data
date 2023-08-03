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

import akka.NotUsed
import akka.stream.scaladsl.Source
import com.google.inject.Inject
import com.mongodb.client.model.InsertManyOptions
import config.AppConfig
import models._
import org.mongodb.scala.bson.BsonValue
import org.mongodb.scala.bson.conversions.Bson
import org.mongodb.scala.model.Indexes.{ascending, compoundIndex}
import org.mongodb.scala.model._
import play.api.Logging
import play.api.libs.json.JsObject
import repositories._
import uk.gov.hmrc.mongo.MongoComponent
import uk.gov.hmrc.mongo.play.json.{Codecs, PlayMongoRepository}

import java.util.concurrent.TimeUnit
import javax.inject.Singleton
import scala.concurrent.{ExecutionContext, Future}

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
    )
    with Logging {

  override lazy val requiresTtlIndex: Boolean = config.isP5TtlEnabled

  def getListByName(listName: ListName, versionId: VersionId, filter: Option[FilterParams] = None): Source[JsObject, NotUsed] = {

    val standardFilters = Aggregates.filter(
      Filters.and(
        Filters.eq("listName", listName.listName),
        Filters.eq("versionId", versionId.versionId)
      )
    )

    val extraFilters: Seq[Bson] = filter
      .map(
        x =>
          x.parameters.map(
            f =>
              Aggregates.filter(
                Filters.eq(fieldName = f._1, value = f._2)
              )
          )
      )
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

  def getListByNameWithFilter(listName: ListName, versionId: VersionId, filter: FilterParams): Source[JsObject, NotUsed] =
    getListByName(listName, versionId, Some(filter))

  def deleteOldImports(payload: ReferenceDataPayload, versionId: VersionId): Future[ListRepositoryDeleteResult] = {

    val filter = Aggregates.filter(
      Filters.and(
        Filters.lt("importId", versionId.versionId)
      )
    )

    collection
      .deleteMany(filter)
      .toFuture()
      .map(_.wasAcknowledged())
      .map {
        case true  => SuccessfulDelete
        case false => FailedDelete(payload.listNames)
      }
  }

  def insertList(list: Seq[GenericListItem]): Future[ListRepositoryWriteResult] =
    collection
      .insertMany(list, new InsertManyOptions().ordered(true))
      .toFuture()
      .map(_.wasAcknowledged())
      .map {
        case true  => SuccessfulWrite
        case false => FailedWrite(list.head.listName)
      }
}

object ListRepository {

  def indexes(config: AppConfig): Seq[IndexModel] = {
    val listNameAndVersionIdCompoundIndex: IndexModel = IndexModel(
      keys = compoundIndex(ascending("listName"), ascending("versionId")),
      indexOptions = IndexOptions().name("list-name-and-version-id-compound-index")
    )

    lazy val createdOnIndex: IndexModel = IndexModel(
      keys = Indexes.ascending("createdOn"),
      indexOptions = IndexOptions().name("ttl-index").expireAfter(config.ttl, TimeUnit.SECONDS)
    )

    listNameAndVersionIdCompoundIndex +: (if (config.isTtlEnabled) Seq(createdOnIndex) else Nil)
  }
}
