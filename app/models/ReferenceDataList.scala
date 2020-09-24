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

package models

import play.api.libs.json.{JsObject, Json, OWrites}

case class ReferenceDataList(id: ListName, metaData: MetaData, data: List[JsObject])

object ReferenceDataList {
  implicit val writes: OWrites[ReferenceDataList] = OWrites(
    referenceDataList =>
      Json.obj(
        "links" -> {
          Json.obj("self" ->
            Json.obj(
              "href" -> s"customs-reference-data/lists/${referenceDataList.id.listName}"
            )
          )
        },
        "id"       -> referenceDataList.id.listName,
        "metaData" -> Json.toJsObject(referenceDataList.metaData),
        "data"     -> referenceDataList.data
      )
  )
}
