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

import play.modules.reactivemongo.ReactiveMongoApi
import reactivemongo.api.indexes.Index
import reactivemongo.play.json.collection.JSONCollection

import javax.inject.Inject
import scala.concurrent.ExecutionContext
import scala.concurrent.Future

abstract private[repositories] class Collection @Inject() (
  mongo: ReactiveMongoApi
)(implicit ec: ExecutionContext)
    extends (() => Future[JSONCollection]) {

  override def apply(): Future[JSONCollection] =
    for {
      collection <- mongo.database.map(_.collection[JSONCollection](collectionName))
      _          <- ensureIndexes(collection)
      _          <- dropOldIndexes(collection)
    } yield collection

  val collectionName: String

  val indexes: Seq[Index.Default]

  val indexesToDrop: Seq[String]

  def ensureIndexes(jsonCollection: JSONCollection): Future[Unit] =
    indexes.foldLeft(Future.successful(())) {
      (acc, index) =>
        acc.flatMap(_ => jsonCollection.indexesManager.ensure(index).map(_ => ()))
    }

  // TODO - remove after deployment of CTCTRADERS-2934 changes
  def dropOldIndexes(jsonCollection: JSONCollection): Future[Unit] =
    jsonCollection.indexesManager.list().map(_.map(_.name)).flatMap {
      indexNames =>
        indexesToDrop.foldLeft(Future.successful(())) {
          (acc, indexName) =>
            acc.flatMap {
              _ =>
                if (indexNames.exists(_.contains(indexName)))
                  jsonCollection.indexesManager.drop(indexName).map(_ => ())
                else
                  Future.successful(())
            }
        }
    }
}
