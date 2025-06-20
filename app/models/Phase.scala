/*
 * Copyright 2025 HM Revenue & Customs
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

trait Phase {
  val directory: String
}

object Phase {

  def apply(version: String): Option[Phase] =
    version match {
      case "1.0" => Some(Phase5)
      case "2.0" => Some(Phase6)
      case _     => None
    }

  case object Phase5 extends Phase {
    override val directory: String = "phase-5"
  }

  case object Phase6 extends Phase {
    override val directory: String = "phase-6"
  }

}
