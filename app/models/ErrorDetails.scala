/*
 * Copyright 2021 HM Revenue & Customs
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

import org.leadpony.justify.api.Problem
import play.api.libs.functional.syntax._
import play.api.libs.json._

final class SchemaErrorDetails(val message: String, val path: String) {
  val code: String = "SCHEMA_ERROR"
}

object SchemaErrorDetails {
  def apply(message: String, path: String): SchemaErrorDetails = new SchemaErrorDetails(message, path)

  implicit val writes: OWrites[SchemaErrorDetails] = (
    (__ \ "code").write[String] and
      (__ \ "message").write[String] and
      (__ \ "path").write[String]
  )(arg => (arg.code, arg.message, arg.path))
}

sealed trait ErrorDetails {
  def code: String
  def message: String
  def errors: Option[Seq[SchemaErrorDetails]]
}

object ErrorDetails {

  implicit def writes[T <: ErrorDetails]: OWrites[T] =
    (
      (__ \ "code").write[String] and
        (__ \ "message").write[String] and
        (__ \ "errors").writeNullable[Seq[SchemaErrorDetails]]
    )(arg => (arg.code, arg.message, arg.errors))
}

case class InvalidJsonError(_message: String) extends ErrorDetails {
  override def code: String = "INVALID_JSON"

  override def message: String = _message

  override def errors: Option[Seq[SchemaErrorDetails]] = None
}

case class SchemaValidationError(_errors: Seq[SchemaErrorDetails]) extends ErrorDetails {
  override def code: String = "SCHEMA_ERROR"

  override def message: String = "The JSON request was not conformant with the schema. Schematic errors are detailed in the errors property below."

  override def errors: Option[Seq[SchemaErrorDetails]] = Some(_errors)
}

object SchemaValidationError {

  def fromJsonSchemaProblems(problems: Seq[Problem]): SchemaValidationError = {
    val _errors: Seq[SchemaErrorDetails] = problems.map {
      problem =>
        SchemaErrorDetails(problem.getMessage(), problem.getPointer())
    }

    SchemaValidationError(_errors)
  }
}

case class OtherError(_message: String) extends ErrorDetails {
  override def code: String = "OTHER_ERROR"

  override def message: String = _message

  override def errors: Option[Seq[SchemaErrorDetails]] = None
}

case class WriteError(_message: String) extends ErrorDetails {
  override def code: String = "OTHER_ERROR"

  override def message: String = _message

  override def errors: Option[Seq[SchemaErrorDetails]] = None
}
