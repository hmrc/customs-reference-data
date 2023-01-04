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

import java.time.LocalDate

import play.api.libs.json._

sealed trait ReferenceDataPayload {
  def messageInformation: MessageInformation
  def listNames: Seq[ListName]
  def toIterable(versionId: VersionId): Iterable[Seq[GenericListItem]]
}

class ReferenceDataListsPayload(data: JsObject) extends ReferenceDataPayload {

  override lazy val messageInformation: MessageInformation =
    (for {
      JsString(messageId) <- (data \ "messageInformation" \ "messageID").validate[JsString]
      snapshotDate        <- (data \ "messageInformation" \ "snapshotDate").validate[LocalDate]
    } yield MessageInformation(messageId, snapshotDate))
      .getOrElse(throw new Exception("Failed to convert ReferenceDataListsPayload to MessageInformation"))

  private lazy val lists: JsObject = (data \ "lists").get.as[JsObject]

  override lazy val listNames: Seq[ListName] = lists.keys.map(list => (lists \ list).as[ListName]).toSeq

  override def toIterable(versionId: VersionId): Iterable[Seq[GenericListItem]] =
    lists.values.map(
      list =>
        (for {
          ln <- list.validate[ListName]
          le <- (list \ "listEntries").validate[Vector[JsObject]]
        } yield le.map(GenericListItem(ln, messageInformation, versionId, _))).get
    )
}

object ReferenceDataListsPayload {
  def apply(v1: JsObject): ReferenceDataListsPayload = new ReferenceDataListsPayload(v1)
}
