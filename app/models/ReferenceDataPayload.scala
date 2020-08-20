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

import play.api.libs.json.JsArray
import play.api.libs.json.JsObject
import play.api.libs.json.JsString

class ReferenceDataPayload(data: JsObject) {

  case class ListName(name: String)
  case class SingleList(messageInformation: MessageInformation, list: JsArray)
  case class MessageInformation(messageId: String, snapshotDate: LocalDate)

  private lazy val messageInformation: MessageInformation =
    (for {
      JsString(messageId) <- (data \ "messageInformation" \ "messageID").validate[JsString]
      snapshotDate        <- (data \ "messageInformation" \ "snapshotDate").validate[LocalDate]
    } yield MessageInformation(messageId, snapshotDate)).get

  private lazy val lists: JsObject = (data \ "lists").get.as[JsObject]

  def listsNames: collection.Set[ListName] =
    lists.keys.map(ListName)

  def getList(listName: ListName): SingleList =
    SingleList(messageInformation, (lists \ listName.name \ "listEntries").as[JsArray])

}

object ReferenceDataPayload {

  def apply(data: JsObject): ReferenceDataPayload = new ReferenceDataPayload(data)

}
