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

import base.SpecBase
import play.api.libs.json.Json

class ErrorResponseSpec extends SpecBase {

  "ErrorDetails" - {
    "serialization to json" in {
      val errorDetails = ErrorDetails("testMessage", "path")

      Json.toJsObject(errorDetails) mustEqual Json.obj(
        "code"    -> errorDetails.code,
        "message" -> errorDetails.message,
        "path"    -> errorDetails.path
      )

    }
  }

  "ErrorResponse" - {
    "InvaildJsonError" - {
      "serialization to json" in {
        val message       = "invalid json"
        val errorResponse = InvaildJsonError(_message = message)

        Json.toJsObject(errorResponse) mustEqual Json.obj(
          "code"    -> "INVALID_JSON",
          "message" -> message
        )

      }
    }

    "SchemaError" - {
      "serialization to json" in {
        val message       = "invalid json"
        val errorDetails  = ErrorDetails("testMessage", "path")
        val errorResponse = SchemaError(message, Seq(errorDetails))

        Json.toJsObject(errorResponse) mustEqual Json.obj(
          "code"    -> "SCHEMA_ERROR",
          "message" -> message,
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

    "OtherError" - {
      "serialization to json" in {
        val message       = "some other error"
        val errorResponse = OtherError(_message = message)

        Json.toJsObject(errorResponse) mustEqual Json.obj(
          "code"    -> "OTHER_ERROR",
          "message" -> message
        )

      }
    }
  }

}
