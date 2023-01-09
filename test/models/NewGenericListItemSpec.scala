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

import base.SpecBase
import play.api.libs.json.Json

import java.time.LocalDateTime

class NewGenericListItemSpec extends SpecBase {

  "Json serialization" in {
    val now  = LocalDateTime.of(2020, 1, 1, 9, 0, 0)
    val date = now.toLocalDate

    val listName    = ListName("listNameValue")
    val messageInfo = MessageInformation("messageIdValue", date)
    val versionId   = VersionId("1")

    val listItemJson = Json.obj("key" -> "value")

    val genericListItem = NewGenericListItem(listName, messageInfo, versionId, listItemJson, now)

    val expectedJson = Json.obj(
      "listName"     -> listName.listName,
      "messageID"    -> "messageIdValue",
      "snapshotDate" -> Json.obj(s"$$date" -> Json.obj(s"$$numberLong" -> "1577836800000")),
      "versionId"    -> versionId.versionId,
      "data"         -> listItemJson,
      "createdOn"    -> Json.obj(s"$$date" -> Json.obj(s"$$numberLong" -> "1577869200000"))
    )

    Json.toJsObject(genericListItem) mustEqual expectedJson

  }

}
