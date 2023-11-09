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

import base.ItSpecBase
import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import play.api.Application
import play.api.http.Status._
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.ws.WSClient

import java.io.File

class CustomsOfficeListControllerSpec extends ItSpecBase with GuiceOneServerPerSuite {

  private val wsClient = app.injector.instanceOf[WSClient]
  private val baseUrl  = s"http://localhost:$port"

  override def fakeApplication(): Application =
    GuiceApplicationBuilder()
      .configure("metrics.enabled" -> false)
      .build()

  private val bearerToken = "ABC"

  private val url = s"$baseUrl/customs-reference-data/customs-office-lists"

  "v2 customs offices ingestion endpoint" - {
    "when gzipped json is schema valid" - {
      "must respond with 200 status" in {
        val file = new File(getClass.getResource("/reference/v2/customs_offices.json.gz").toURI)

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
            .post(file)
            .futureValue

        response.status mustBe ACCEPTED
      }
    }

    "when gzipped json is schema invalid" - {
      "must respond with 400 status" in {
        val file = new File(getClass.getResource("/reference/invalid.json.gz").toURI)

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
            .post(file)
            .futureValue

        response.status mustBe BAD_REQUEST
      }
    }

    "when Authorization header is missing" - {
      "must respond with 401 status" in {
        val file = new File(getClass.getResource("/reference/v2/customs_offices.json.gz").toURI)

        val headers = Seq(
          "Accept"           -> "application/vnd.hmrc.2.0+gzip",
          "Content-Encoding" -> "gzip",
          "Content-Type"     -> "application/json"
        )

        val response =
          wsClient
            .url(url)
            .withHttpHeaders(headers: _*)
            .post(file)
            .futureValue

        response.status mustBe UNAUTHORIZED
      }
    }
  }

}
