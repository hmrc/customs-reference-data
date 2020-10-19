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

import akka.NotUsed
import akka.stream.scaladsl.Flow
import akka.util.ByteString
import play.api.libs.json.JsObject
import play.api.libs.json.Json

class StreamReferenceData {

  private def jsonFormat(listName: ListName, metaData: MetaData): String =
    s"""
       |{
       |   "_links": {
       |     "self": {
       |       "href": "customs-reference-data/lists/${listName.listName}"
       |     }
       |   },
       |   "meta": ${Json.toJsObject(metaData)},
       |   "id": "${listName.listName}",
       |   "data": [
       |""".stripMargin

  def wrapInJson(listName: ListName, metaData: MetaData): Flow[JsObject, ByteString, NotUsed] =
    Flow
      .apply[JsObject]
      .map(r => ByteString(Json.stringify(r)))
      .intersperse(ByteString(jsonFormat(listName, metaData)), ByteString(","), ByteString("]}"))
}

object StreamReferenceData {
  def apply(): StreamReferenceData = new StreamReferenceData()
}
