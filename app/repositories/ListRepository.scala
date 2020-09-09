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
import javax.inject.Singleton
import models.GenericListItem
import models.ListName
import models.MetaData
import play.api.libs.json.JsObject
import play.api.libs.json.Json
import reactivemongo.api.Cursor
import reactivemongo.api.commands.MultiBulkWriteResult
import reactivemongo.play.json.ImplicitBSONHandlers.JsObjectDocumentWriter
import repositories.ListRepository.FailedWrite
import repositories.ListRepository.ListRepositoryWriteResult
import repositories.ListRepository.PartialWriteFailure
import repositories.ListRepository.SuccessfulWrite

import scala.concurrent.ExecutionContext
import scala.concurrent.Future

@Singleton
class ListRepository @Inject() (listCollection: ListCollection)(implicit ec: ExecutionContext) {

  def getList(listName: ListName, metaDeta: MetaData): Future[List[JsObject]] = {
    val selector = Json.toJsObject(listName)

    listCollection().flatMap {
      _.find(selector, None)
        .cursor[JsObject]()
        .collect[List](-1, Cursor.FailOnError[List[JsObject]]())
    }
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
