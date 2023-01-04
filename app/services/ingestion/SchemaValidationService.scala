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

package services.ingestion

import jakarta.json.JsonReader
import jakarta.json.stream.JsonParsingException
import models.ErrorDetails
import models.InvalidJsonError
import models.JsonSchemaProvider
import models.SchemaValidationError
import org.leadpony.justify.api.JsonValidationService
import org.leadpony.justify.api.Problem
import org.leadpony.justify.api.ProblemHandler
import play.api.libs.json.JsObject
import play.api.libs.json.JsValue

import java.io.StringReader
import javax.inject.Inject
import scala.collection.mutable.ListBuffer

private[ingestion] class SchemaValidationService @Inject() (jsonValidationService: JsonValidationService) {

  private def problemHandler(schemaValidationProblems: ListBuffer[Problem]): ProblemHandler =
    _.forEach {
      problem =>
        schemaValidationProblems += problem
    }

  def validate(schema: JsonSchemaProvider, json: JsValue): Either[ErrorDetails, JsObject] = {

    val schemaValidationProblems = ListBuffer.empty[Problem]

    val jsonReader: JsonReader =
      jsonValidationService.createReader(new StringReader(json.toString), schema.schema, problemHandler(schemaValidationProblems))

    try {
      jsonReader.read()

      if (schemaValidationProblems.isEmpty)
        json
          .validate[JsObject]
          .asEither
          .fold(
            validationError => Left(InvalidJsonError(validationError.mkString("Errors while parsing json: ", " ; ", ""))),
            jsObject => Right(jsObject)
          )
      else Left(SchemaValidationError.fromJsonSchemaProblems(schemaValidationProblems.toList))

    } catch {
      case exc: JsonParsingException =>
        Left(InvalidJsonError(exc.getMessage))
    } finally jsonReader.close()

  }
}
