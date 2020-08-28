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

import com.google.inject.Inject
import models.GenericListItem
import models.ListName
import models.MetaData
import play.api.libs.json.JsObject
import play.api.libs.json.Json
import play.modules.reactivemongo.ReactiveMongoApi
import reactivemongo.api.Cursor
import reactivemongo.api.commands.MultiBulkWriteResult
import reactivemongo.play.json.ImplicitBSONHandlers.JsObjectDocumentWriter
import reactivemongo.play.json.collection.JSONCollection
import repositories.ListRepository.FailedWrite
import repositories.ListRepository.ListRepositoryWriteResult
import repositories.ListRepository.PartialWriteFailure
import repositories.ListRepository.SuccessfulWrite
import repositories.ListRepository.collectionName

import scala.concurrent.ExecutionContext
import scala.concurrent.Future

class ListRepository @Inject() (mongo: ReactiveMongoApi)(implicit ec: ExecutionContext) {

  private def collection: Future[JSONCollection] =
    mongo.database.map(_.collection[JSONCollection](collectionName))

  def getList(listName: ListName, metaDeta: MetaData): Future[List[JsObject]] = {
    val selector = Json.toJsObject(listName)

    collection.flatMap {
      _.find(selector, None).cursor[JsObject]().collect[List](-1, Cursor.FailOnError[List[JsObject]]())
    }
  }

  def insertList(list: Seq[GenericListItem]): Future[ListRepositoryWriteResult] =
    collection.flatMap {
      _.insert(ordered = false) // TODO: how do we recover if an item fails bulk insert?
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
  val collectionName = "reference-data-lists"

  sealed trait ListRepositoryWriteResult

  case object SuccessfulWrite                                          extends ListRepositoryWriteResult
  case class PartialWriteFailure(insertFailures: Seq[GenericListItem]) extends ListRepositoryWriteResult
  case class FailedWrite(multiBulkWriteResult: MultiBulkWriteResult)   extends ListRepositoryWriteResult
}
