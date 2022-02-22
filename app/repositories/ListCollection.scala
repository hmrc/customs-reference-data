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
import reactivemongo.api.bson.collection.BSONSerializationPack
import reactivemongo.api.indexes.Index.Aux
import reactivemongo.api.indexes.Index.Default
import reactivemongo.api.indexes.IndexType

import javax.inject.Inject
import javax.inject.Singleton
import scala.concurrent.ExecutionContext

@Singleton
private[repositories] class ListCollection @Inject() (
  mongo: ReactiveMongoApi
)(implicit ec: ExecutionContext)
    extends Collection(mongo) {

  private val listNameAndVersionIdCompoundIndex: Aux[BSONSerializationPack.type] = IndexBuilder.index(
    key = Seq("listName" -> IndexType.Ascending, "versionId" -> IndexType.Ascending),
    name = Some("list-name-and-version-id-compound-index")
  )

  override val collectionName: String = ListCollection.collectionName

  override val indexes: Seq[Default] = Seq(listNameAndVersionIdCompoundIndex)

  override val indexesToDrop: Seq[String] = Seq(
    "listName_index",
    "versionId_index"
  )
}

object ListCollection {
  val collectionName = "reference-data-lists"
}
