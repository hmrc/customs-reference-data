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

@deprecated
case class ResponseErrorMessage(errorType: ResponseErrorType, errors: Option[Seq[ResponseErrorDetails]])

object ResponseErrorMessage {

  implicit val writes: OWrites[ResponseErrorMessage] =
    (__.write[ResponseErrorType] and
      (__ \ "errors").writeNullable[Seq[ResponseErrorDetails]])(unlift(ResponseErrorMessage.unapply))

  implicit val reads: Reads[ResponseErrorMessage] =
    (__.read[ResponseErrorType] and
      (__ \ "errors").readNullable[Seq[ResponseErrorDetails]])(ResponseErrorMessage.apply _)
}

@deprecated
sealed abstract class ResponseErrorType(val code: String, val message: String)

object ResponseErrorType {

  def apply(code: String): Option[ResponseErrorType] =
    code match {
      case InvalidJson.code => Some(InvalidJson)
      case SchemaError.code => Some(SchemaError)
      case OtherError.code  => Some(OtherError)
      case _                => None
    }

  def unapply(arg: ResponseErrorType): Some[(String, String)] =
    Some(arg.code -> arg.message)

  implicit val writes: OWrites[ResponseErrorType] =
    ((__ \ "code").write[String] and
      (__ \ "message").write[String])(unlift(ResponseErrorType.unapply))

  implicit val reads: Reads[ResponseErrorType] = {
    case x: JsObject =>
      (x \ "code").validate[JsString].flatMap {
        case JsString(code) =>
          apply(code).map(JsSuccess(_)).getOrElse(JsError((__ \ "code"), "Code type did not match expected values"))
      }
    case y => JsError(s"Expected a JsObject, got a ${y.getClass}")
  }

  case object InvalidJson
      extends ResponseErrorType(
        code = "INVALID_JSON",
        message = "The request body was empty or not valid JSON"
      )

  case object SchemaError
      extends ResponseErrorType(
        code = "SCHEMA_ERROR",
        message = "The JSON request was not conformant with the schema. Schematic errors are detailed in the errors property below."
      )

  case object OtherError
      extends ResponseErrorType(
        code = "OTHER_ERROR",
        message = "Something else was wrong with the client request."
      )
}

case class ResponseErrorDetails(code: String, message: String, path: String)

object ResponseErrorDetails {
  implicit val formats: OFormat[ResponseErrorDetails] = Json.format[ResponseErrorDetails]
}
