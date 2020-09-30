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
import play.api.libs.json.JsObject
import play.api.libs.json.JsValue
import play.api.libs.json.Json
import reactivemongo.api.Cursor
import reactivemongo.api.Cursor.WithOps
import reactivemongo.api.commands.MultiBulkWriteResult
import reactivemongo.play.json.ImplicitBSONHandlers.JsObjectDocumentWriter
import repositories.ListRepository.FailedWrite
import repositories.ListRepository.ListRepositoryWriteResult
import repositories.ListRepository.PartialWriteFailure
import repositories.ListRepository.SuccessfulWrite
import reactivemongo.akkastream.State
import reactivemongo.akkastream.cursorProducer

import scala.concurrent.ExecutionContext
import scala.concurrent.Future

@Singleton
class ListRepository @Inject() (listCollection: ListCollection)(implicit ec: ExecutionContext, mt: Materializer) {

  def getListByName(listName: ListName, versionId: VersionId): Future[List[JsObject]] = {
    val selector = Json.toJsObject(listName) ++ Json.toJsObject(versionId)

    listCollection().flatMap {
      collection =>
        val cursor: Source[JsObject, Future[State]] =
          collection
            .find(selector, projection = Some(Json.obj("_id" -> 0)))
            .cursor[JsObject]()
            .documentSource()

        val woa: Source[JsObject, Future[State]] = cursor.map(x => (x \ "data").getOrElse(JsObject.empty).asInstanceOf[JsObject])

        woa.runWith(Sink.seq[JsObject]).map(_.toList)
    }

//
//    listCollection().flatMap {
//      _.find(selector, projection = Some(Json.obj("_id" -> 0)))
//        .cursor[JsObject]()
//        .collect[List](-1, Cursor.FailOnError[List[JsObject]]())
//    }
  }

  // This should only retrieve listNames (not whole collection)
  def getAllLists(version: VersionId): Future[List[GenericListItem]] =
    listCollection().flatMap {
      _.find(Json.toJsObject(version), None)
        .cursor[GenericListItem]()
        .collect[List](-1, Cursor.FailOnError[List[GenericListItem]]())
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
