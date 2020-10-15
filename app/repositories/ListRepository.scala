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

import akka.stream.Materializer
import akka.stream.scaladsl.Sink
import akka.stream.scaladsl.Source
import com.google.inject.Inject
import javax.inject.Singleton
import models.GenericListItem
import models.ListName
import models.VersionId
import models.VersionedListName
import play.api.libs.json.JsObject
import play.api.libs.json.Json
import play.api.libs.json.OFormat
import reactivemongo.akkastream.State
import reactivemongo.akkastream.cursorProducer
import reactivemongo.api.ReadConcern
import reactivemongo.api.commands.MultiBulkWriteResult
import repositories.ListRepository.FailedWrite
import repositories.ListRepository.ListRepositoryWriteResult
import repositories.ListRepository.PartialWriteFailure
import repositories.ListRepository.SuccessfulWrite
import repositories.Query.QueryOps

import scala.collection.immutable
import scala.concurrent.ExecutionContext
import scala.concurrent.Future

@Singleton
class ListRepository @Inject() (listCollection: ListCollection)(implicit ec: ExecutionContext, mt: Materializer) {

  def getListByName(listNameDetails: VersionedListName): Future[Seq[JsObject]] =
    listCollection.apply().flatMap {
      x =>
        import x.aggregationFramework.PipelineOperator

        val woa: PipelineOperator  = PipelineOperator(Json.obj("$match" -> listNameDetails.query))
        val woa1: PipelineOperator = PipelineOperator(Json.obj("$sort" -> Json.obj("_id" -> 1)))
        val woa2: PipelineOperator = PipelineOperator(Json.obj("$project" -> {
          Json.obj("data" -> 1) ++ Json.obj("_id" -> 0)
        }))

        val woa3 = x
          .aggregateWith[JsObject](allowDiskUse = true) {
            _ => (woa, List(woa1, woa2))
          }
          .documentSource()

        woa3.map(jsObject => (jsObject \ "data").getOrElse(JsObject.empty).asInstanceOf[JsObject]).runWith(Sink.seq[JsObject])
    }

  def getListNames(version: VersionId): Future[Seq[ListName]] =
    listCollection().flatMap {
      _.distinct[String, Seq]("listName", Some(version.query), ReadConcern.Local, None)
        .map(_.map(ListName(_)))
    }

  def insertList(list: Seq[GenericListItem]): Future[ListRepositoryWriteResult] =
    listCollection().flatMap {
      _.insert(ordered = true) // TODO: how do we recover if an item fails bulk insert?
        .many(list)
        .map {
          res =>
            if (res.writeErrors.nonEmpty && (res.n > 0 || res.nModified > 0))
              PartialWriteFailure(res.writeErrors.map(_.index).map(index => list(index)))
            else if ((list.length <= res.n + res.nModified))
              SuccessfulWrite
            else
              FailedWrite(res)
        }
    }

}

object ListRepository {

  sealed trait ListRepositoryWriteResult

  case object SuccessfulWrite                                          extends ListRepositoryWriteResult
  case class PartialWriteFailure(insertFailures: Seq[GenericListItem]) extends ListRepositoryWriteResult
  case class FailedWrite(multiBulkWriteResult: MultiBulkWriteResult)   extends ListRepositoryWriteResult
}
