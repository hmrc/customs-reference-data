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

package models.testOnly

import play.api.libs.json._

case class Role(role: String)

object Role {
  implicit val roleFormatter: OFormat[Role] = Json.format[Role]
}

case class CustomsOffice(
  id: String,
  name: String,
  countryId: String,
  phoneNumber: Option[String],
  roles: Seq[Role]
)

object CustomsOffice {

  implicit val readFromFile: Reads[CustomsOffice] = Json.reads[CustomsOffice]
  implicit val writes: OWrites[CustomsOffice]     = Json.writes[CustomsOffice]

}
