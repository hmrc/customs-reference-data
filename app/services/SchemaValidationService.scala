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
import models.JsonSchemaProvider
import org.leadpony.justify.api.JsonValidationService
import org.leadpony.justify.api.Problem
import org.leadpony.justify.api.ProblemHandler
import play.api.libs.json.JsObject
import play.api.libs.json.Json

import scala.collection.JavaConverters._
import scala.collection.mutable.ListBuffer

class SchemaValidationService @Inject() (jsonValidationService: JsonValidationService) {
  import services.SchemaValidationService._

  private def problemHandler(schemaValidationProblems: ListBuffer[Problem]): ProblemHandler =
    _.asScala.foreach {
      problem =>
        schemaValidationProblems += problem
    }

  def validate(schema: JsonSchemaProvider, rawJson: ByteString): Either[ValidationError, JsObject] = {
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
            x => Left(InvalidJson(x.mkString("Errors while parsing json: ", "\n", ""))),
            x => Right(x)
          )
      else Left(SchemaValidationError(schemaValidationProblems.toList))

    } catch {
      case exc: JsonParsingException =>
        Left(InvalidJson(exc.getMessage()))
    } finally jsonReader.close()

  }
}

object SchemaValidationService {

  sealed trait ValidationError
  case class InvalidJson(message: String)                 extends ValidationError
  case class SchemaValidationError(problem: Seq[Problem]) extends ValidationError

}
