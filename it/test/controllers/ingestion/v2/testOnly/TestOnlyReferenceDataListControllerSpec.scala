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

package controllers.ingestion.v2.testOnly

import play.api.http.Status._

class TestOnlyReferenceDataListControllerSpec extends TestOnlyIngestionControllerSpec {

  override val validGzipFile: String = "/reference/v2/reference_data.xml.gz"
  override val validXmlFile: String  = "/reference/v2/reference_data.xml"

  private val url = s"$baseUrl/customs-reference-data/test-only/reference-data-lists"

  "v2 reference data test-only ingestion endpoint" - {
    "when gzipped xml is schema valid" - {
      "must respond with 202 status" in {
        val headers = Seq(
          "Accept"           -> "application/vnd.hmrc.2.0+gzip",
          "Content-Encoding" -> "gzip",
          "Content-Type"     -> "application/xml"
        )

        val response =
          wsClient
            .url(url)
            .withHttpHeaders(headers: _*)
            .post(file(validGzipFile))
            .futureValue

        response.status mustBe ACCEPTED

        countDocuments mustBe 55
      }
    }

    "when xml is schema valid" - {
      "must respond with 202 status" in {
        val headers = Seq(
          "Accept"       -> "application/vnd.hmrc.2.0+gzip",
          "Content-Type" -> "application/xml"
        )

        val response =
          wsClient
            .url(url)
            .withHttpHeaders(headers: _*)
            .post(file(validXmlFile))
            .futureValue

        response.status mustBe ACCEPTED

        countDocuments mustBe 55
      }
    }

    "when gzipped xml is schema invalid" - {
      "must respond with 400 status" in {
        val headers = Seq(
          "Accept"           -> "application/vnd.hmrc.2.0+gzip",
          "Content-Encoding" -> "gzip",
          "Content-Type"     -> "application/xml"
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
  }
}
