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
import reactivemongo.api.indexes.IndexType
import reactivemongo.play.json.collection.JSONCollection

import javax.inject.Inject
import javax.inject.Singleton
import scala.concurrent.ExecutionContext
import scala.concurrent.Future

@Singleton
private[repositories] class VersionCollection @Inject() (mongo: ReactiveMongoApi)(implicit ec: ExecutionContext) extends (() => Future[JSONCollection]) {

  override def apply(): Future[JSONCollection] =
    started.flatMap {
      _ => collection
    }

  private lazy val collection = mongo.database.map(_.collection[JSONCollection](VersionCollection.collectionName))

  private val listNameAndSnapshotDateCompoundIndex: Index.Default =
    IndexBuilder.index(
      key = Seq("listNames.listName" -> IndexType.Ascending, "snapshotDate" -> IndexType.Descending),
      name = Some("list-name-and-snapshot-date-compound-index")
    )

  private val sourceAndSnapshotDateCompoundIndex: Index.Default =
    IndexBuilder.index(
      key = Seq("source" -> IndexType.Ascending, "snapshotDate" -> IndexType.Descending),
      name = Some("source-and-snapshot-date-compound-index")
    )

  private lazy val started: Future[Unit] =
    for {
      jsonCollection <- collection
      _              <- jsonCollection.indexesManager.ensure(listNameAndSnapshotDateCompoundIndex)
      _              <- jsonCollection.indexesManager.ensure(sourceAndSnapshotDateCompoundIndex)
    } yield ()
}

object VersionCollection {
  val collectionName = "versions"
}
