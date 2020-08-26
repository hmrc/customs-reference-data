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

import java.time.LocalDate

import base.SpecBase
import play.api.libs.json.JsObject
import play.api.libs.json.Json

class GenericListItemSpec extends SpecBase with MongoDateTimeFormats {

  "Json serialization" in {
    val date = LocalDate.now()

    val r           = ReferenceDataPayload(JsObject.empty)
    val listName    = ListName("listNameValue")
    val messageInfo = MessageInformation("messageIdValue", date)

    val listItemJson = Json.obj("key" -> "value")

    val genericListItem = GenericListItem(listName, messageInfo, listItemJson)

    val expectedJson = Json.obj(
      "listName"     -> listName.listName,
      "messageID"    -> "messageIdValue",
      "snapshotDate" -> date,
      "data"         -> listItemJson
    )

    Json.toJsObject(genericListItem) mustEqual expectedJson

  }

}
