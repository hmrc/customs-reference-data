/*
 * Copyright 2023 HM Revenue & Customs
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

import org.apache.pekko.NotUsed
import org.apache.pekko.stream.scaladsl.Flow
import org.apache.pekko.util.ByteString
import models.FilterParams
import models.ListName
import models.MetaData
import play.api.libs.json.Json
import play.api.libs.json.Writes

class StreamReferenceData(listName: ListName, metaData: MetaData) {

  private def jsonFormat(filterParams: Option[FilterParams]): String =
    s"""
       |{
       |   "_links": {
       |     "self": {
       |       "href": "${controllers.consumption.routes.ListRetrievalController.get(listName, filterParams).url}"
       |     }
       |   },
       |   "meta": ${Json.toJsObject(metaData)},
       |   "id": "${listName.listName}",
       |   "data": [
       |""".stripMargin

  def nestInJson[A: Writes](filterParams: Option[FilterParams]): Flow[A, ByteString, NotUsed] =
    Flow
      .apply[A]
      .map(a => ByteString(Json.stringify(Json.toJson(a))))
      .intersperse(ByteString(jsonFormat(filterParams)), ByteString(","), ByteString("]}"))

}

object StreamReferenceData {
  def apply(listName: ListName, metaData: MetaData): StreamReferenceData = new StreamReferenceData(listName: ListName, metaData: MetaData)
}
