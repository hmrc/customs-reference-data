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

import akka.NotUsed
import akka.stream.scaladsl.Source
import com.google.inject.Inject
import com.mongodb.client.model.InsertManyOptions
import models.GenericListItem
import models.ListName
import models.VersionId
import org.mongodb.scala.bson.BsonValue
import org.mongodb.scala.model.Indexes.ascending
import org.mongodb.scala.model.Indexes.compoundIndex
import org.mongodb.scala.model._
import play.api.libs.json.JsObject
import uk.gov.hmrc.mongo.MongoComponent
import uk.gov.hmrc.mongo.play.json.Codecs
import uk.gov.hmrc.mongo.play.json.PlayMongoRepository

import javax.inject.Singleton
import scala.concurrent.ExecutionContext
import scala.concurrent.Future

@Singleton
class ListRepository @Inject() (
  mongoComponent: MongoComponent
)(implicit ec: ExecutionContext)
    extends PlayMongoRepository[GenericListItem](
      mongoComponent = mongoComponent,
      collectionName = "reference-data-lists",
      domainFormat = GenericListItem.format,
      indexes = ListRepository.indexes,
      replaceIndexes = true // TODO - remove or set to false after deployment of CTCTRADERS-2934 changes
    ) {

  def getListByName(listName: ListName, versionId: VersionId): Source[JsObject, NotUsed] = {
    val filter = Aggregates.filter(
      Filters.and(
        Filters.eq("listName", listName.listName),
        Filters.eq("versionId", versionId.versionId)
      )
    )

    val projection = Aggregates.project(
      Projections.fields(
        Projections.include("data"),
        Projections.exclude("_id")
      )
    )

    Source.fromPublisher(
      collection
        .aggregate[BsonValue](Seq(filter, projection))
        .allowDiskUse(true)
        .map(Codecs.fromBson[JsObject](_))
    )
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

  val indexes: Seq[IndexModel] = {
    val versionIdIndex: IndexModel =
      IndexModel(
        keys = ascending("versionId"),
        indexOptions = IndexOptions().name("version-id-index")
      )

    val listNameAndVersionIdCompoundIndex: IndexModel =
      IndexModel(
        keys = compoundIndex(ascending("listName"), ascending("versionId")),
        indexOptions = IndexOptions().name("list-name-and-version-id-compound-index")
      )

    Seq(
      versionIdIndex,
      listNameAndVersionIdCompoundIndex
    )
  }
}
