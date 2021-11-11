/*
 * Copyright 2021 HM Revenue & Customs
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

import javax.inject.Inject
import javax.inject.Singleton
import play.modules.reactivemongo.ReactiveMongoApi
import reactivemongo.api.bson.collection.BSONSerializationPack
import reactivemongo.api.indexes.Index.Aux
import reactivemongo.api.indexes.IndexType
import reactivemongo.play.json.collection.JSONCollection

import scala.concurrent.ExecutionContext
import scala.concurrent.Future

@Singleton
private[repositories] class ListCollection @Inject() (mongo: ReactiveMongoApi)(implicit ec: ExecutionContext) extends (() => Future[JSONCollection]) {

  private lazy val collection: Future[JSONCollection] = mongo.database.map(_.collection[JSONCollection](ListCollection.collectionName))

  private val listNameIndex: Aux[BSONSerializationPack.type] = IndexBuilder.index(
    key = Seq("listName" -> IndexType.Ascending),
    name = Some("listName_index")
  )

  private val versionIdIndex: Aux[BSONSerializationPack.type] = IndexBuilder.index(
    key = Seq("versionId" -> IndexType.Ascending),
    name = Some("versionId_index")
  )

  private val started: Future[Unit] =
    collection
      .flatMap {
        jsonCollection =>
          for {
            _ <- jsonCollection.indexesManager.ensure(listNameIndex)
            _ <- jsonCollection.indexesManager.ensure(versionIdIndex)
          } yield ()
      }

  override def apply(): Future[JSONCollection] =
    started.flatMap {
      _ => collection
    }

}

object ListCollection {
  val collectionName = "reference-data-lists"
}
