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

package controllers.ingestion.v2

import play.api.http.Status._

class ReferenceDataListControllerSpec extends IngestionControllerSpec {

  override val validGzipFile: String = "/reference/v2/reference_data.json.gz"
  override val validJsonFile: String = "/reference/v2/reference_data.json"

  private val url = s"$baseUrl/customs-reference-data/reference-data-lists"

  "v2 reference data ingestion endpoint" - {
    "when gzipped json is schema valid" - {
      "must respond with 202 status" in {
        val headers = Seq(
          "Accept"           -> "application/vnd.hmrc.2.0+gzip",
          "Authorization"    -> s"Bearer $bearerToken",
          "Content-Encoding" -> "gzip",
          "Content-Type"     -> "application/json"
        )

        val response =
          wsClient
            .url(url)
            .withHttpHeaders(headers: _*)
            .post(file(validGzipFile))
            .futureValue

        response.status mustBe ACCEPTED

        countDocuments mustBe 167056
      }
    }

    "when json is schema valid" - {
      "must respond with 202 status" in {
        val headers = Seq(
          "Accept"        -> "application/vnd.hmrc.2.0+gzip",
          "Authorization" -> s"Bearer $bearerToken",
          "Content-Type"  -> "application/json"
        )

        val response =
          wsClient
            .url(url)
            .withHttpHeaders(headers: _*)
            .post(file(validJsonFile))
            .futureValue

        response.status mustBe ACCEPTED

        countDocuments mustBe 167056
      }
    }

    "when gzipped json is schema invalid" - {
      "must respond with 400 status" in {
        val headers = Seq(
          "Accept"           -> "application/vnd.hmrc.2.0+gzip",
          "Authorization"    -> s"Bearer $bearerToken",
          "Content-Encoding" -> "gzip",
          "Content-Type"     -> "application/json"
        )

        val response =
          wsClient
            .url(url)
            .withHttpHeaders(headers: _*)
            .post(file(invalidDataFile))
            .futureValue

        response.status mustBe BAD_REQUEST

        countDocuments mustBe 0
      }
    }

    "when Authorization header is missing" - {
      "must respond with 401 status" in {
        val headers = Seq(
          "Accept"           -> "application/vnd.hmrc.2.0+gzip",
          "Content-Encoding" -> "gzip",
          "Content-Type"     -> "application/json"
        )

        val response =
          wsClient
            .url(url)
            .withHttpHeaders(headers: _*)
            .post(file(validGzipFile))
            .futureValue

        response.status mustBe UNAUTHORIZED

        countDocuments mustBe 0
      }
    }
  }
}
