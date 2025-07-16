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

package api

import models._
import play.api.libs.json.JsObject
import play.api.libs.json.Json

import java.time.Instant
import java.time.LocalDate

trait ConsumptionHelper {

  val defaultSnapshotDate: LocalDate = LocalDate.of(2015, 12, 12)
  val defaultMessageId: String       = "message-id-default"

  val defaultMessageInformation: MessageInformation = MessageInformation(defaultMessageId, defaultSnapshotDate)

  val defaultListName: ListName = ListName("AdditionalInformation")

  val firstDefaultDataItem: JsObject  = Json.obj("state" -> "default", "activeFrom" -> "2015-12-12", "code" -> "CS", "description" -> Json.arr())
  val secondDefaultDataItem: JsObject = Json.obj("state" -> "default", "activeFrom" -> "2015-12-12", "code" -> "DE", "description" -> Json.arr())

  val defaultData: Seq[JsObject] = Seq(firstDefaultDataItem, secondDefaultDataItem)

  def getListItem(versionId: VersionId, data: JsObject): GenericListItem =
    GenericListItem(
      defaultListName,
      defaultMessageInformation,
      versionId,
      data,
      Instant.now()
    )

  def basicList(versionId: VersionId): GenericList =
    GenericList(
      name = defaultListName,
      entries = Seq(
        getListItem(versionId, firstDefaultDataItem),
        getListItem(versionId, secondDefaultDataItem)
      )
    )
}
