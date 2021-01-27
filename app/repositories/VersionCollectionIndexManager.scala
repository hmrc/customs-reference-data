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
import reactivemongo.api.indexes.Index
import reactivemongo.api.indexes.IndexType
import reactivemongo.bson.BSONDocument

import scala.concurrent.ExecutionContext
import scala.concurrent.Future

@Singleton
class VersionCollectionIndexManager @Inject() (versionCollection: VersionCollection)(implicit ec: ExecutionContext) {

  private val versionId_index: Index.Default = Index(
    key = Seq("versionId" -> IndexType.Ascending),
    name = Some("versionId_index"),
    unique = true,
    background = true,
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
    background = true,
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
    background = true,
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
    versionCollection().flatMap(
      _.indexesManager
        .ensure(index)
    )

  val started: Future[Unit] =
    for {
      _ <- addIndex(versionId_index)
      _ <- addIndex(snapshotDate_index)
      _ <- addIndex(listNames_index)
    } yield ()

}
