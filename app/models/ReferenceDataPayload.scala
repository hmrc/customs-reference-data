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

import play.api.libs.json._

class ReferenceDataPayload(data: JsObject) {

  lazy val messageInformation: MessageInformation =
    (for {
      JsString(messageId) <- (data \ "messageInformation" \ "messageID").validate[JsString]
      snapshotDate        <- (data \ "messageInformation" \ "snapshotDate").validate[LocalDate]
    } yield MessageInformation(messageId, snapshotDate)).get

  private lazy val lists: JsObject = (data \ "lists").get.as[JsObject]

  def toIterable(versionId: VersionId): Iterable[Seq[GenericListItem]] =
    lists.values.flatMap(
      list =>
        (for {
          ln <- list.validate[ListName]
          le <- (list \ "listEntries").validate[Vector[JsObject]]
        } yield le.map(GenericListItem(ln, messageInformation, versionId, _))).asOpt
    )
}

object ReferenceDataPayload {

  def apply(data: JsObject): ReferenceDataPayload = new ReferenceDataPayload(data)

}
