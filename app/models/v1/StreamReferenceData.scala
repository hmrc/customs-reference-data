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

package models.v1

import akka.NotUsed
import akka.stream.scaladsl.Flow
import akka.util.ByteString
import models.ListName
import models.MetaData
import play.api.libs.json.Json
import play.api.libs.json.Writes

class StreamReferenceData(listName: ListName, metaData: MetaData) {

  private lazy val jsonFormat: String =
    s"""
       |{
       |   "_links": {
       |     "self": {
       |       "href": "${removeVersionFromHref(controllers.consumption.v1.routes.ListRetrievalController.get(listName).url)}"
       |     }
       |   },
       |   "meta": ${Json.toJsObject(metaData)},
       |   "id": "${listName.listName}",
       |   "data": [
       |""".stripMargin

  def nestInJson[A: Writes]: Flow[A, ByteString, NotUsed] =
    Flow
      .apply[A]
      .map(a => ByteString(Json.stringify(Json.toJson(a))))
      .intersperse(ByteString(jsonFormat), ByteString(","), ByteString("]}"))

  private def removeVersionFromHref(href: String): String = href.substring(5)
}

object StreamReferenceData {
  def apply(listName: ListName, metaData: MetaData): StreamReferenceData = new StreamReferenceData(listName: ListName, metaData: MetaData)
}
