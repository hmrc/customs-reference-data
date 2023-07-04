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

import play.api.libs.functional.syntax._
import play.api.libs.json._

sealed trait DocumentType {
  val code: String
  val description: Option[String]
  val transportDocument: Option[Boolean]
}

object DocumentType {

  implicit val reads: Reads[DocumentType] = (
    (__ \ "code").read[String] and
      (__ \ "description").readNullable[String] and
      (__ \ "transportDocument").readNullable[Boolean]
  ).apply {
    (code, description, isTransportDocument) =>
      isTransportDocument match {
        case Some(true)  => TransportDocumentType(code, description)
        case Some(false) => SupportingDocumentType(code, description)
        case None        => PreviousDocumentType(code, description)
      }
  }

  implicit val writes: Writes[DocumentType] = Writes {
    case x: PreviousDocumentType =>
      Json.obj("code" -> x.code, "description" -> x.description)
    case x =>
      Json.obj("code" -> x.code, "description" -> x.description, "transportDocument" -> x.transportDocument)
  }
}

case class PreviousDocumentType(code: String, description: Option[String]) extends DocumentType {
  override val transportDocument: Option[Boolean] = None
}

object PreviousDocumentType {
  implicit val format: Format[PreviousDocumentType] = Json.format[PreviousDocumentType]
}

case class SupportingDocumentType(code: String, description: Option[String]) extends DocumentType {
  override val transportDocument: Option[Boolean] = Some(false)
}

object SupportingDocumentType {
  implicit val format: Format[SupportingDocumentType] = Json.format[SupportingDocumentType]
}

case class TransportDocumentType(code: String, description: Option[String]) extends DocumentType {
  override val transportDocument: Option[Boolean] = Some(true)
}

object TransportDocumentType {
  implicit val format: Format[TransportDocumentType] = Json.format[TransportDocumentType]
}
