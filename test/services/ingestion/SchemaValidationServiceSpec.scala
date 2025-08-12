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

import base.SpecBase
import models.{JsonSchemaProvider, SchemaValidationError}
import org.leadpony.justify.api.JsonValidationService
import org.scalatest.EitherValues
import play.api.Environment
import play.api.Mode.Test
import play.api.inject.{bind, SimpleModule}
import play.api.libs.json.Json

import java.io.File

class SchemaValidationServiceSpec extends SpecBase with EitherValues {

  "validate" - {
    "returns true when the json matches the schema specifications" in {
      val json = Json.obj(
        "firstName" -> "firstName_value",
        "lastName"  -> "lastName_value",
        "age"       -> 21
      )

      val jsonValidationService = JsonValidationService.newInstance()

      val testJsonSchemaProvider = new TestJsonSchemaProvider()

      val service = new SchemaValidationService(jsonValidationService)

      service.validate(testJsonSchemaProvider.getSchema, json).value mustEqual json
    }

    "returns SchemaValidationError with description of the problem when the json does not match the schema specifications" in {

      val json = Json.obj(
        "firstName" -> "firstName_value",
        "lastName"  -> "lastName_value",
        "age"       -> "INVALID_VALUE",
        "level1" -> Json.obj(
          "level1_arr" -> Json.arr(1)
        )
      )

      val jsonValidationService = JsonValidationService.newInstance()

      val testJsonSchemaProvider = new TestJsonSchemaProvider()

      val service = new SchemaValidationService(jsonValidationService)

      service.validate(testJsonSchemaProvider.getSchema, json).left.value mustBe a[SchemaValidationError]
    }
  }
}

class TestJsonSchemaProvider extends JsonSchemaProvider {

  override val env: Environment = Environment(
    rootPath = new File("."),
    classLoader = getClass.getClassLoader,
    mode = Test
  )

  override val jsonValidationService: JsonValidationService = JsonValidationService.newInstance()

  override val path: String = "test.schema.json"
}

class TestModule
    extends SimpleModule(
      (_, _) => Seq(bind[TestJsonSchemaProvider].toSelf.eagerly())
    )
