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

import play.api.libs.json.Format
import play.api.libs.json.Json

/** UN/LOCODE (United Nations Code for Trade and Transport Locations) */
case class UnLocode(
  unLocodeExtendedCode: String,
  name: String,
  subdivision: Option[String],
  function: String,
  status: String,
  date: String,
  coordinates: Option[String],
  comment: Option[String]
)

object UnLocode {
  implicit val format: Format[UnLocode] = Json.format[UnLocode]
}
