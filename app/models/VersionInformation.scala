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
import play.api.libs.json.Format
import play.api.libs.json.OWrites
import play.api.libs.json.Reads
import play.api.libs.json.__
import uk.gov.hmrc.mongo.play.json.formats.MongoJavatimeFormats

import java.time.LocalDateTime

case class VersionInformation(
  messageInformation: MessageInformation,
  versionId: VersionId,
  createdOn: LocalDateTime,
  source: ApiDataSource,
  listNames: Seq[ListName]
)

object VersionInformation {

  implicit val writes: OWrites[VersionInformation] =
    (
      __.write[MessageInformation] and
        __.write[VersionId] and
        (__ \ "createdOn").write(MongoJavatimeFormats.localDateTimeWrites) and
        (__ \ "source").write[ApiDataSource] and
        (__ \ "listNames").write[Seq[ListName]]
    )(unlift(VersionInformation.unapply))

  implicit val readers: Reads[VersionInformation] =
    (
      __.read[MessageInformation] and
        __.read[VersionId] and
        (__ \ "createdOn").read(MongoJavatimeFormats.localDateTimeReads) and
        (__ \ "source").read[ApiDataSource] and
        (__ \ "listNames").read[Seq[ListName]]
    )(VersionInformation.apply _)

  implicit val format: Format[VersionInformation] = Format(readers, writes)
}
