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

package models

import play.api.libs.functional.syntax._
import play.api.libs.json._

case class ReferenceDataList(id: ListName, metaData: MetaData, data: Seq[JsObject])

object ReferenceDataList {

  implicit val writes: OWrites[ReferenceDataList] =
    OWrites[ReferenceDataList] {
      referenceDataList =>
        Json.obj(
          "_links" -> {
            Json.obj(
              "self" ->
                Json.obj(
                  "href" -> s"customs-reference-data/lists/${referenceDataList.id.listName}"
                )
            )
          },
          "id"       -> referenceDataList.id.listName,
          "metaData" -> Json.toJsObject(referenceDataList.metaData),
          "data"     -> referenceDataList.data
        )
    }

  implicit val reads: Reads[ReferenceDataList] =
    (
      (__ \ "id").read[String].map(ListName.apply) and
        (__ \ "metaData").read[MetaData] and
        (__ \ "data").read[List[JsObject]]
    )(ReferenceDataList.apply _)
}
