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
import java.util.Locale

import akka.util.ByteString
import jakarta.json.JsonReader
import jakarta.json.stream.JsonParsingException
import javax.inject.Inject
import models.JsonSchemaProvider
import org.leadpony.justify.api.JsonValidationService
import org.leadpony.justify.api.ProblemHandler
import org.leadpony.justify.internal.problem.ProblemRenderer
import scala.collection.JavaConverters._

import scala.collection.mutable.ListBuffer

class SchemaValidationService @Inject() (jsonValidationService: JsonValidationService) {

  private val problemRenderer = ProblemRenderer.DEFAULT_RENDERER // TODO: Remove

  private def problemHandler(schemaValidationProblems: ListBuffer[String]): ProblemHandler =
    _.asScala.foreach {
      problem =>
        schemaValidationProblems += problemRenderer.render(problem, Locale.getDefault())
    }

  def validate(schema: JsonSchemaProvider, rawJson: ByteString): Boolean = {
    val inputStream: ByteArrayInputStream = new ByteArrayInputStream(rawJson.toArray)

    val schemaValidationProblems: ListBuffer[String] = ListBuffer.empty[String]

    val jsonReader: JsonReader = jsonValidationService.createReader(inputStream, schema.schema, problemHandler(schemaValidationProblems))

    try {
      jsonReader.read()

      schemaValidationProblems.isEmpty // TODO: Return errors or valid data
    } catch {
      case exc: JsonParsingException =>
        ??? // TODO: Bubble up this
    } finally jsonReader.close()

  }

}
