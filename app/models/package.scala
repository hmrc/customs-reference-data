/*
 * Copyright 2024 HM Revenue & Customs
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

import org.apache.commons.text.StringEscapeUtils
import play.api.libs.json._

package object models {

  implicit class RichJsValue[T <: JsValue](value: T) {

    def unescapeXml(implicit rds: Reads[T]): T = {
      def rec(value: JsValue): JsValue =
        value match {
          case JsString(value) =>
            JsString(StringEscapeUtils.unescapeXml(value))
          case JsArray(values) =>
            JsArray(values.map(rec))
          case JsObject(underlying) =>
            JsObject(underlying.map {
              case (key, value) => (key, rec(value))
            })
          case value =>
            value
        }

      rec(value).as[T]
    }
  }
}
