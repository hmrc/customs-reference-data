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

import akka.util.ByteString
import base.SpecBase
import javax.inject.Inject
import models.SimpleJsonSchemaProvider
import org.leadpony.justify.api.JsonValidationService
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Environment
import play.api.inject.SimpleModule
import play.api.inject.bind
import play.api.libs.json.Json

private[this] class TestJsonSchema @Inject() (env: Environment, jvs: JsonValidationService) extends SimpleJsonSchemaProvider(env, jvs)("test.schema.json")

private[this] class TestModule extends SimpleModule((env, _) => Seq(bind[TestJsonSchema].toSelf.eagerly()))

class SchemaValidationServiceSpec extends SpecBase with GuiceOneAppPerSuite {

  "validate" - {
    "returns true when the json matches the schema specifications" in {
      val json = Json.obj(
        "firstName" -> "firstName_value",
        "lastName"  -> "lastName_value",
        "age"       -> 21
      )

      val service        = app.injector.instanceOf[SchemaValidationService]
      val testJsonSchema = app.injector.instanceOf[TestJsonSchema]

      service.validate(testJsonSchema, ByteString.fromString(json.toString())) mustEqual true

    }

    "returns false when the json does not match the schema specifications" in {
      val json = Json.obj(
        "firstName" -> "firstName_value",
        "lastName"  -> "lastName_value",
        "age"       -> "INVALID_VALUE"
      )

      val service        = app.injector.instanceOf[SchemaValidationService]
      val testJsonSchema = app.injector.instanceOf[TestJsonSchema]

      service.validate(testJsonSchema, ByteString.fromString(json.toString())) mustEqual false

    }

  }

}
