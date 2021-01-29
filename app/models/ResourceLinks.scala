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

package models

import play.api.libs.json.JsObject
import play.api.libs.json.JsString
import play.api.libs.json.Json
import play.api.libs.json.OFormat

case class ResourceLinks(_links: Map[String, JsObject])

object ResourceLinks {

  def apply(listNames: Seq[ListName]): ResourceLinks =
    new ResourceLinks(linkFormatter(listNames))

  private def linkFormatter(listNames: Seq[ListName]): Map[String, JsObject] = {

    def buildUri(listName: Option[String]): String =
      listName.fold("/customs-reference-data/lists") {
        name => s"/customs-reference-data/lists/$name"
      }

    //TODO fix ordering here
    val resourceLinks: Seq[(String, JsObject)] = listNames.map {
      listName => listName.listName -> JsObject(Seq("href" -> JsString(buildUri(Some(listName.listName)))))
    }

    Map(
      "self" -> JsObject(Seq("href" -> JsString(buildUri(None))))
    ) ++ resourceLinks
  }

  implicit val formats: OFormat[ResourceLinks] = Json.format[ResourceLinks]
}
