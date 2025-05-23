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

import base.SpecBase
import play.api.libs.json.Json

class ErrorDetailsSpec extends SpecBase {

  "ErrorDetails" - {
    "InvalidJsonError" - {
      "serialization to json" in {
        val message       = "invalid json"
        val errorResponse = InvalidJsonError(message)

        Json.toJsObject(errorResponse) mustEqual Json.obj(
          "code"    -> "INVALID_JSON",
          "message" -> message
        )
      }
    }

    "OtherError" - {
      "serialization to json" in {
        val message       = "some other error"
        val errorResponse = OtherError(message)

        Json.toJsObject(errorResponse) mustEqual Json.obj(
          "code"    -> "OTHER_ERROR",
          "message" -> message
        )
      }
    }

    "UnauthorisedError" - {
      "serialization to json" in {
        val message       = "some unauthorised error"
        val errorResponse = UnauthorisedError(message)

        Json.toJsObject(errorResponse) mustEqual Json.obj(
          "code"    -> "401",
          "message" -> message
        )
      }
    }

    "MongoError" - {
      "serialization to json" in {
        val message       = "some write error"
        val errorResponse = MongoError(message)

        Json.toJsObject(errorResponse) mustEqual Json.obj(
          "code"    -> "MONGO_ERROR",
          "message" -> message
        )
      }
    }

    "SchemaValidationError" - {
      "serialization to json" in {
        val errorDetails  = SchemaErrorDetails("testMessage", "path")
        val errorResponse = SchemaValidationError(Seq(errorDetails))

        Json.toJsObject(errorResponse) mustEqual Json.obj(
          "code"    -> "SCHEMA_ERROR",
          "message" -> "The JSON request was not conformant with the schema. Schematic errors are detailed in the errors property below.",
          "errors" -> Json.arr(
            Json.obj(
              "code"    -> errorDetails.code,
              "message" -> errorDetails.message,
              "path"    -> errorDetails.path
            )
          )
        )
      }
    }
  }

  "SchemaErrorDetails" - {
    "serialization to json" in {
      val errorDetails = SchemaErrorDetails("testMessage", "path")

      Json.toJsObject(errorDetails) mustEqual Json.obj(
        "code"    -> errorDetails.code,
        "message" -> errorDetails.message,
        "path"    -> errorDetails.path
      )
    }
  }
}
