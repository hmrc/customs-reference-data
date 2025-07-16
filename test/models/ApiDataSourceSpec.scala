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
import models.ApiDataSource.ColDataFeed
import models.ApiDataSource.RefDataFeed
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.libs.json.JsString
import play.api.libs.json.JsSuccess
import play.api.libs.json.Json

class ApiDataSourceSpec extends SpecBase with ScalaCheckPropertyChecks {

  "ApiDataSource" - {

    "when casting to string" - {
      "when of type RefDataFeed" - {
        "must return RefDataFeed" in {
          RefDataFeed.toString mustEqual "RefDataFeed"
        }
      }

      "when of type ColDataFeed" - {
        "must return ColDataFeed" in {
          ColDataFeed.toString mustEqual "ColDataFeed"
        }
      }
    }

    "when reading from json" - {
      "must read successfully" - {
        "when RefDataFeed" in {
          JsString("RefDataFeed").validate[ApiDataSource] mustEqual JsSuccess(RefDataFeed)
        }

        "when ColDataFeed" in {
          JsString("ColDataFeed").validate[ApiDataSource] mustEqual JsSuccess(ColDataFeed)
        }
      }

      "must read unsuccessfully" - {
        "when something else" in {
          forAll(arbitrary[String]) {
            str =>
              a[NoSuchElementException] mustBe thrownBy(JsString(str).validate[ApiDataSource])
          }
        }
      }
    }

    "when writing to json" - {
      "when RefDataFeed" - {
        "must return JsString('RefDataFeed')" in {
          val source: ApiDataSource = RefDataFeed
          Json.toJson(source) mustEqual JsString("RefDataFeed")
        }
      }

      "when ColDataFeed" - {
        "must return JsString('ColDataFeed')" in {
          val source: ApiDataSource = ColDataFeed
          Json.toJson(source) mustEqual JsString("ColDataFeed")
        }
      }
    }
  }
}
