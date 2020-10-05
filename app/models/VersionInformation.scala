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

import java.time.LocalDateTime

import play.api.libs.functional.syntax.unlift
import play.api.libs.json.OWrites
import play.api.libs.json.Reads
import play.api.libs.json.__
import play.api.libs.functional.syntax._

case class VersionInformation(messageInformation: MessageInformation, versionId: VersionId, createdOn: LocalDateTime, validFor: Seq[ListName])

object VersionInformation extends MongoDateTimeFormats {

  implicit val writes: OWrites[VersionInformation] =
    (
      __.write[MessageInformation] and
        __.write[VersionId] and
        (__ \ "createdOn").write[LocalDateTime] and
        (__ \ "validFor").write[Seq[ListName]]
    )(unlift(VersionInformation.unapply))

  implicit val readers: Reads[VersionInformation] =
    (
      __.read[MessageInformation] and
        __.read[VersionId] and
        (__ \ "createdOn").read[LocalDateTime] and
        (__ \ "validFor").read[Seq[ListName]]
    )(VersionInformation.apply _)
}
