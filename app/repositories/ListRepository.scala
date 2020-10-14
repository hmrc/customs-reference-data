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
import com.google.inject.Inject
import javax.inject.Singleton
import models.GenericListItem
import models.ListName
import models.VersionId
import models.VersionedListName
import play.api.libs.json.JsObject
import play.api.libs.json.JsString
import play.api.libs.json.Json
import play.api.libs.json.OWrites
import play.api.libs.json.Writes
import play.api.libs.functional.syntax._
import reactivemongo.akkastream.cursorProducer
import reactivemongo.api.ReadConcern
import reactivemongo.api.commands.MultiBulkWriteResult
import reactivemongo.play.json.ImplicitBSONHandlers.JsObjectDocumentWriter
import repositories.ListRepository.FailedWrite
import repositories.ListRepository.ListRepositoryWriteResult
import repositories.ListRepository.PartialWriteFailure
import repositories.ListRepository.SuccessfulWrite
import Query.QueryOps
import play.api.libs.functional.FunctionalBuilder
import play.api.libs.functional.~

import scala.concurrent.ExecutionContext
import scala.concurrent.Future

@Singleton
class ListRepository @Inject() (listCollection: ListCollection)(implicit ec: ExecutionContext, mt: Materializer) {

  def getListByName(listNameDetails: VersionedListName): Future[Seq[JsObject]] =
    listCollection().flatMap {
      _.find(listNameDetails.query, projection = Some(Json.obj("_id" -> 0)))
        .cursor[JsObject]()
        .documentSource()
        .map(jsObject => (jsObject \ "data").getOrElse(JsObject.empty).asInstanceOf[JsObject])
        .runWith(Sink.seq[JsObject])
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
              PartialWriteFailure(list.head.listName, res.writeErrors.map(_.index))
            else if ((list.length <= res.n + res.nModified))
              SuccessfulWrite
            else
              FailedWrite(list.head.listName)
        }
    }

}

object ListRepository {

  sealed trait ListRepositoryWriteResult

  case object SuccessfulWrite                                               extends ListRepositoryWriteResult
  case class PartialWriteFailure(listName: ListName, errorsIndex: Seq[Int]) extends ListRepositoryWriteResult
  case class FailedWrite(listName: ListName)                                extends ListRepositoryWriteResult
}
