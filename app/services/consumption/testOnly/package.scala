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

package services.consumption

import play.api.libs.json.JsError
import play.api.libs.json.JsResult
import play.api.libs.json.JsSuccess

import scala.util.Failure
import scala.util.Success
import scala.util.Try

package object testOnly {

  implicit class RichJsResult[A](jsResult: JsResult[A]) {

    def asTry: Try[A] =
      jsResult match {
        case JsSuccess(value, _) => Success(value)
        case JsError(errors)     => Failure(new Exception(errors.mkString))
      }
  }
}
