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

package services

import java.io.ByteArrayInputStream

import akka.util.ByteString
import jakarta.json.JsonReader
import jakarta.json.stream.JsonParsingException
import javax.inject.Inject
import models.ErrorDetails
import models.InvaildJsonError
import models.JsonSchemaProvider
import models.SchemaValidationError
import org.leadpony.justify.api.JsonValidationService
import org.leadpony.justify.api.Problem
import org.leadpony.justify.api.ProblemHandler
import play.api.libs.json.JsObject
import play.api.libs.json.Json

import scala.collection.JavaConverters._
import scala.collection.mutable.ListBuffer

class SchemaValidationService @Inject() (jsonValidationService: JsonValidationService) {

  private def problemHandler(schemaValidationProblems: ListBuffer[Problem]): ProblemHandler =
    _.asScala.foreach {
      problem =>
        schemaValidationProblems += problem
    }

  def validate(schema: JsonSchemaProvider, rawJson: ByteString): Either[ErrorDetails, JsObject] = {
    val rawJsonByteArray: Array[Byte] = rawJson.toArray

    val schemaValidationProblems = ListBuffer.empty[Problem]

    val jsonReader: JsonReader =
      jsonValidationService.createReader(new ByteArrayInputStream(rawJsonByteArray), schema.schema, problemHandler(schemaValidationProblems))

    try {
      jsonReader.read()

      if (schemaValidationProblems.isEmpty)
        Json
          .parse(rawJson.toArray)
          .validate[JsObject]
          .asEither
          .fold(
            x => Left(InvaildJsonError(x.mkString("Errors while parsing json: ", " ; ", ""))),
            x => Right(x)
          )
      else Left(SchemaValidationError.fromJsonSchemaProblems(schemaValidationProblems.toList))

    } catch {
      case exc: JsonParsingException =>
        Left(InvaildJsonError(exc.getMessage()))
    } finally jsonReader.close()

  }
}
