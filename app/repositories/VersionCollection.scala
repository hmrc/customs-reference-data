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
import reactivemongo.api.indexes.Index
import reactivemongo.api.indexes.IndexType
import reactivemongo.api.bson.BSONDocument
import reactivemongo.play.json.collection.JSONCollection

import scala.concurrent.ExecutionContext
import scala.concurrent.Future

@Singleton
private[repositories] class VersionCollection @Inject() (mongo: ReactiveMongoApi)(implicit ec: ExecutionContext) extends (() => Future[JSONCollection]) {

  override def apply(): Future[JSONCollection] =
    started.flatMap {
      case true => collection
      case _ =>
        println("Unable to create indices")
        throw (new RuntimeException("Unable to create indices"))
    }

  private lazy val collection = mongo.database.map(_.collection[JSONCollection](VersionCollection.collectionName))

  private val versionId_index: Index.Default = Index(
    key = Seq("versionId" -> IndexType.Ascending),
    name = Some("versionId_index"),
    unique = true,
    background = false,
    sparse = false,
    expireAfterSeconds = None,
    storageEngine = None,
    weights = None,
    defaultLanguage = None,
    languageOverride = None,
    textIndexVersion = None,
    sphereIndexVersion = None,
    bits = None,
    min = None,
    max = None,
    bucketSize = None,
    collation = None,
    wildcardProjection = None,
    version = None,
    partialFilter = None,
    options = BSONDocument.empty
  )

  private val snapshotDate_index: Index.Default = Index(
    key = Seq("snapshotDate" -> IndexType.Ascending),
    name = Some("snapshotDate_index"),
    unique = false,
    background = false,
    sparse = false,
    expireAfterSeconds = None,
    storageEngine = None,
    weights = None,
    defaultLanguage = None,
    languageOverride = None,
    textIndexVersion = None,
    sphereIndexVersion = None,
    bits = None,
    min = None,
    max = None,
    bucketSize = None,
    collation = None,
    wildcardProjection = None,
    version = None,
    partialFilter = None,
    options = BSONDocument.empty
  )

  private val listNames_index: Index.Default = Index(
    key = Seq("listNames.listName" -> IndexType.Ascending),
    name = Some("listNames_index"),
    unique = false,
    background = false,
    sparse = false,
    expireAfterSeconds = None,
    storageEngine = None,
    weights = None,
    defaultLanguage = None,
    languageOverride = None,
    textIndexVersion = None,
    sphereIndexVersion = None,
    bits = None,
    min = None,
    max = None,
    bucketSize = None,
    collation = None,
    wildcardProjection = None,
    version = None,
    partialFilter = None,
    options = BSONDocument.empty
  )

  private def addIndex(index: Index.Default): Future[Boolean] =
    collection.flatMap(
      _.indexesManager
        .ensure(index)
    )

  private val started: Future[Boolean] =
    for {
      versionIdCreated <- addIndex(versionId_index)
      snapshotCreated  <- addIndex(snapshotDate_index)
      listNamesCreated <- addIndex(listNames_index)
    } yield (versionIdCreated && snapshotCreated && listNamesCreated)
}

object VersionCollection {
  val collectionName = "versions"
}
