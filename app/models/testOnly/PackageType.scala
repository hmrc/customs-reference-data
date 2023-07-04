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

sealed trait PackageType

object PackageType {

  case object Bulk     extends PackageType
  case object Unpacked extends PackageType
  case object Other    extends PackageType

  implicit val reads: Reads[PackageType] = Reads {
    case JsString("Bulk")     => JsSuccess(Bulk)
    case JsString("Unpacked") => JsSuccess(Unpacked)
    case JsString("Other")    => JsSuccess(Other)
    case _                    => JsError("Invalid package type")
  }

  implicit val writes: Writes[PackageType] = Writes {
    value => JsString(value.toString)
  }
}
