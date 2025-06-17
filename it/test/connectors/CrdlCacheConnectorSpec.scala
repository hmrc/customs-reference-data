/*
 * Copyright 2025 HM Revenue & Customs
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

package connectors

import base.{ItSpecBase, WireMockServerHandler}
import com.github.tomakehurst.wiremock.client.WireMock.{get, okJson, urlEqualTo}
import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json

class CrdlCacheConnectorSpec extends ItSpecBase with WireMockServerHandler with GuiceOneServerPerSuite {

  override def guiceApplicationBuilder: GuiceApplicationBuilder =
    super.guiceApplicationBuilder.configure("microservice.services.crdl-cache.port" -> server.port())
  private val connector = app.injector.instanceOf[CrdlCacheConnector]

  "GET" - {
    "when no query parameter" - {
      "must return OK" in {
        val url = "crdl-cache/lists/CL239"

        val json = Json.parse("""
            |[
            |  {
            |    "key": "00200",
            |    "value": "Several occurrences of documents and parties",
            |    "properties": {
            |      "state": "valid"
            |    }
            |  },
            |  {
            |    "key": "00700",
            |    "value": "Discharge of inward processing. IP’ and the relevant authorisation number or INF number",
            |    "properties": {
            |      "state": "valid"
            |    }
            |  },
            |  {
            |    "key": "00800",
            |    "value": "Discharge of inward processing (specific commercial policy measures)",
            |    "properties": {
            |      "state": "valid"
            |    }
            |  }
            |]
            |""".stripMargin)

        server.stubFor(
          get(urlEqualTo(url))
            .willReturn(okJson(Json.stringify(json)))
        )

        val result = connector.get("CL239").futureValue

        result mustEqual json
      }
    }
  }
}
