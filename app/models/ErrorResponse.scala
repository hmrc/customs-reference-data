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

import play.api.libs.json._
import play.api.libs.functional.syntax._

final class ErrorDetails(val message: String, val path: String) {
  val code: String = "SCHEMA_ERROR"
}

object ErrorDetails {
  def apply(message: String, path: String): ErrorDetails = new ErrorDetails(message, path)

  implicit val writes: OWrites[ErrorDetails] = (
    (__ \ "code").write[String] and
      (__ \ "message").write[String] and
      (__ \ "path").write[String]
  )(arg => (arg.code, arg.message, arg.path))
}

sealed trait ErrorResponse {
  def code: String
  def message: String
  def errors: Option[Seq[ErrorDetails]]
}

object ErrorResponse {

  implicit val writes: OWrites[ErrorResponse] = (
    (__ \ "code").write[String] and
      (__ \ "message").write[String] and
      (__ \ "errors").writeNullable[Seq[ErrorDetails]]
  )(arg => (arg.code, arg.message, arg.errors))

}

case class InvaildJsonError(_message: String) extends ErrorResponse {
  override def code: String = "INVALID_JSON"

  override def message: String = _message

  override def errors: Option[Seq[ErrorDetails]] = None
}

case class SchemaError(_message: String, _errors: Seq[ErrorDetails]) extends ErrorResponse {
  override def code: String = "SCHEMA_ERROR"

  override def message: String = _message

  override def errors: Option[Seq[ErrorDetails]] = Some(_errors)
}

case class OtherError(_message: String) extends ErrorResponse {
  override def code: String = "OTHER_ERROR"

  override def message: String = _message

  override def errors: Option[Seq[ErrorDetails]] = None
}
