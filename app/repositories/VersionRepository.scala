/*
 * Copyright 2020 HM Revenue & Customs
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

import java.time.LocalDateTime

import cats.data._
import cats.implicits._
import com.google.inject.Inject
import javax.inject.Singleton
import models.ListName
import models.MessageInformation
import models.VersionId
import models.VersionInformation
import play.api.libs.json._
import reactivemongo.api.commands.WriteResult
import reactivemongo.play.json.ImplicitBSONHandlers.JsObjectDocumentWriter
import reactivemongo.play.json.collection.JSONCollection
import models.ApiDataSource
import models.ApiDataSource.ColDataFeed
import models.ApiDataSource.RefDataFeed
import repositories.Query.QueryOps
import services.consumption.TimeService

import scala.concurrent.ExecutionContext
import scala.concurrent.Future

@Singleton
class VersionRepository @Inject() (versionCollection: VersionCollection, versionIdProducer: VersionIdProducer, timeService: TimeService)(implicit
  ec: ExecutionContext
) {

  def save(messageInformation: MessageInformation, feed: ApiDataSource, listNames: Seq[ListName]): Future[VersionId] = {
    val versionId: VersionId = versionIdProducer()
    val time: LocalDateTime  = timeService.now()
    val versionInformation   = VersionInformation(messageInformation, versionId, time, feed, listNames)

    versionCollection().flatMap {
      _.insert(false)
        .one(Json.toJsObject(versionInformation))
        .flatMap {
          wr =>
            WriteResult
              .lastError(wr)
              .fold(Future.successful(versionId))(_ => Future.failed(new Exception("Failed to save version information")))
        }
    }
  }

  def getLatest(listName: ListName): Future[Option[VersionInformation]] =
    versionCollection().flatMap(
      _.find(Json.obj("listNames.listName" -> listName.listName), None)
        .sort(Json.obj("snapshotDate" -> -1))
        .one[VersionInformation]
    )

  def getLatestListNames(): Future[Seq[ListName]] = {
    case class ListNames(listNames: Seq[ListName])
    implicit val reads: Reads[ListNames] =
      (__ \ "listNames").read[Seq[ListName]].map(ListNames(_))

    def getListName(coll: JSONCollection)(source: ApiDataSource)(implicit q: Query[ApiDataSource], rds: Reads[ListName]): Future[Option[Seq[ListName]]] =
      coll
        .find(source.query, None)
        .sort(Json.obj("snapshotDate" -> -1))
        .one[ListNames]
        .map(_.map(_.listNames))

    (for {
      db    <- OptionT.liftF(versionCollection())
      list1 <- OptionT(getListName(db)(RefDataFeed))
      list2 <- OptionT(getListName(db)(ColDataFeed))
    } yield list1 ++ list2).value.map(_.getOrElse(Seq.empty))
  }

}
