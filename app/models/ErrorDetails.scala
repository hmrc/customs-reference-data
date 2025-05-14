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

import org.leadpony.justify.api.Problem
import play.api.http.Status.UNAUTHORIZED
import play.api.libs.functional.syntax.*
import play.api.libs.json.*

sealed trait ErrorDetails {
  val code: String
  val message: String
}

object ErrorDetails {

  implicit def writes[T <: ErrorDetails]: OWrites[T] =
    (
      (__ \ "code").write[String] and
        (__ \ "message").write[String]
    )(
      arg => (arg.code, arg.message)
    )
}

sealed trait BadRequestError extends ErrorDetails

case class InvalidJsonError(message: String) extends BadRequestError {
  override val code: String = "INVALID_JSON"
}

case class SchemaValidationError(errors: Seq[SchemaErrorDetails]) extends BadRequestError {
  override val code: String = "SCHEMA_ERROR"

  override val message: String = "The JSON request was not conformant with the schema. Schematic errors are detailed in the errors property below."
}

object SchemaValidationError {

  def fromJsonSchemaProblems(problems: Seq[Problem]): SchemaValidationError = {
    val errors: Seq[SchemaErrorDetails] = problems.map {
      problem =>
        SchemaErrorDetails(problem.getMessage(), problem.getPointer())
    }

    new SchemaValidationError(errors)
  }

  implicit val writes: OWrites[SchemaValidationError] =
    (
      (__ \ "code").write[String] and
        (__ \ "message").write[String] and
        (__ \ "errors").write[Seq[SchemaErrorDetails]]
    )(
      arg => (arg.code, arg.message, arg.errors)
    )
}

case class OtherError(message: String) extends ErrorDetails {
  override val code: String = "OTHER_ERROR"
}

case class UnauthorisedError(message: String) extends ErrorDetails {
  override val code: String = UNAUTHORIZED.toString
}

case class MongoError(message: String) extends ErrorDetails {
  override val code: String = "MONGO_ERROR"
}

case class SchemaErrorDetails(message: String, path: String) {
  val code: String = "SCHEMA_ERROR"
}

object SchemaErrorDetails {

  def apply(message: String, path: String): SchemaErrorDetails =
    new SchemaErrorDetails(message, path)

  implicit val writes: OWrites[SchemaErrorDetails] = (
    (__ \ "code").write[String] and
      (__ \ "message").write[String] and
      (__ \ "path").write[String]
  )(
    arg => (arg.code, arg.message, arg.path)
  )
}

case class ErrorDetailsException(errorDetails: ErrorDetails) extends Exception
