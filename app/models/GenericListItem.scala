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

import play.api.libs.functional.syntax._
import play.api.libs.json._
import uk.gov.hmrc.mongo.play.json.formats.MongoJavatimeFormats

import java.time.LocalDateTime

case class GenericListItem(
  listName: ListName,
  messageInformation: MessageInformation,
  versionId: VersionId,
  data: JsObject,
  createdOn: LocalDateTime
)

object GenericListItem {

  implicit val writes: OWrites[GenericListItem] = (
    __.write[ListName] and
      __.write[MessageInformation] and
      __.write[VersionId] and
      (__ \ "data").write[JsObject] and
      (__ \ "createdOn").write[LocalDateTime](MongoJavatimeFormats.localDateTimeWrites)
  )(unlift(GenericListItem.unapply))

  implicit val reads: Reads[GenericListItem] = (
    __.read[ListName] and
      __.read[MessageInformation] and
      __.read[VersionId] and
      (__ \ "data").read[JsObject] and
      (__ \ "createdOn").read[LocalDateTime](MongoJavatimeFormats.localDateTimeReads)
  )(GenericListItem.apply _)

  implicit val format: Format[GenericListItem] = Format(reads, writes)
}
