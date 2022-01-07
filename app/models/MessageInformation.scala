/*
 * Copyright 2022 HM Revenue & Customs
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

import play.api.libs.json.Json
import play.api.libs.json.OWrites
import play.api.libs.json.Reads
import play.api.libs.json.__
import play.api.libs.functional.syntax._

case class MessageInformation(messageId: String, snapshotDate: LocalDate)

object MessageInformation extends MongoDateTimeFormats {

  implicit val oWritesMessageInformation: OWrites[MessageInformation] =
    messageInformation =>
      Json.obj(
        "messageID"    -> messageInformation.messageId,
        "snapshotDate" -> messageInformation.snapshotDate
      )

  implicit val reads: Reads[MessageInformation] =
    ((__ \ "messageID").read[String] and
      (__ \ "snapshotDate").read[LocalDate])(MessageInformation(_, _))

}
