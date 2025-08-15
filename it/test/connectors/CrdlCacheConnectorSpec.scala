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
import com.github.tomakehurst.wiremock.client.WireMock.{equalTo, get, okJson, urlEqualTo}
import models.CodeList.ColDataCodeList
import models.{CodeList, FilterParams}
import org.apache.pekko.stream.Materializer
import org.apache.pekko.stream.scaladsl.Sink
import org.apache.pekko.util.ByteString
import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import play.api.test.Helpers.AUTHORIZATION

class CrdlCacheConnectorSpec extends ItSpecBase with GuiceOneServerPerSuite with WireMockServerHandler {

  override def guiceApplicationBuilder: GuiceApplicationBuilder =
    super.guiceApplicationBuilder
      .configure(
        "microservice.services.crdl-cache.port" -> server.port(),
        "internal-auth.token"                   -> "crdl-cache-test-token"
      )

  override def fakeApplication(): Application = guiceApplicationBuilder.build()

  private lazy val connector = app.injector.instanceOf[CrdlCacheConnector]

  implicit private lazy val mat: Materializer = app.injector.instanceOf[Materializer]

  "get" - {

    "must return response JSON" - {
      "when a RefDataCodeList" - {
        "when no query parameters" in {
          val codeList = CodeList("AdditionalInformation")

          val filterParams = FilterParams(Nil)

          val url = "/crdl-cache/lists/CL239"

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
              .withHeader(AUTHORIZATION, equalTo("crdl-cache-test-token"))
              .willReturn(okJson(Json.stringify(json)))
          )

          val result = connector.get(codeList, filterParams).futureValue.runWith(Sink.fold(ByteString.empty)(_ ++ _)).futureValue

          Json.parse(result.toArray) mustEqual json
        }

        "when one query parameter" in {
          val codeList = CodeList("AdditionalInformation")

          val filterParams = FilterParams(Seq("keys" -> Seq("00200")))

          val url = "/crdl-cache/lists/CL239?keys=00200"

          val json = Json.parse("""
              |[
              |  {
              |    "key": "00200",
              |    "value": "Several occurrences of documents and parties",
              |    "properties": {
              |      "state": "valid"
              |    }
              |  }
              |]
              |""".stripMargin)

          server.stubFor(
            get(urlEqualTo(url))
              .withHeader(AUTHORIZATION, equalTo("crdl-cache-test-token"))
              .willReturn(okJson(Json.stringify(json)))
          )

          val result = connector.get(codeList, filterParams).futureValue.runWith(Sink.fold(ByteString.empty)(_ ++ _)).futureValue

          Json.parse(result.toArray) mustEqual json
        }

        "when multiple query parameters" in {
          val codeList = CodeList("AdditionalInformation")

          val filterParams = FilterParams(Seq("keys" -> Seq("00200", "00700")))

          val url = "/crdl-cache/lists/CL239?keys=00200&keys=00700"

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
              |  }
              |]
              |""".stripMargin)

          server.stubFor(
            get(urlEqualTo(url))
              .withHeader(AUTHORIZATION, equalTo("crdl-cache-test-token"))
              .willReturn(okJson(Json.stringify(json)))
          )

          val result = connector.get(codeList, filterParams).futureValue.runWith(Sink.fold(ByteString.empty)(_ ++ _)).futureValue

          Json.parse(result.toArray) mustEqual json
        }
      }

      "when a ColDataCodeList" - {

        val codeList = ColDataCodeList

        "when no query parameters" in {
          val filterParams = FilterParams(Nil)

          val url = "/crdl-cache/offices"

          val json = Json.parse("""
              |[
              |  {
              |    "languageCode": "EN",
              |    "customsOfficeLsd": {
              |      "customsOfficeUsualName": "CUSTOMS OFFICE SANT JULIÀ DE LÒRIA"
              |    },
              |    "phoneNumber": "+ (376) 84 1090",
              |    "referenceNumber": "AD000001",
              |    "countryCode": "AD",
              |    "customsOfficeTimetable": {
              |      "customsOfficeTimetableLine": [
              |        {
              |          "customsOfficeRoleTrafficCompetence": [
              |            {
              |              "roleName": "AUT"
              |            },
              |            {
              |              "roleName": "DEP"
              |            },
              |            {
              |              "roleName": "DES"
              |            },
              |            {
              |              "roleName": "TRA"
              |            }
              |          ]
              |        }
              |      ]
              |    }
              |  },
              |  {
              |    "languageCode": "EN",
              |    "customsOfficeLsd": {
              |      "customsOfficeUsualName": "DCNJ PORTA"
              |    },
              |    "phoneNumber": "+ (376) 755125",
              |    "referenceNumber": "AD000002",
              |    "countryCode": "AD",
              |    "customsOfficeTimetable": {
              |      "customsOfficeTimetableLine": [
              |        {
              |          "customsOfficeRoleTrafficCompetence": [
              |            {
              |              "roleName": "DEP"
              |            },
              |            {
              |              "roleName": "DES"
              |            },
              |            {
              |              "roleName": "TRA"
              |            }
              |          ]
              |        }
              |      ]
              |    }
              |  }
              |]
              |""".stripMargin)

          server.stubFor(
            get(urlEqualTo(url))
              .withHeader(AUTHORIZATION, equalTo("crdl-cache-test-token"))
              .willReturn(okJson(Json.stringify(json)))
          )

          val result = connector.get(codeList, filterParams).futureValue.runWith(Sink.fold(ByteString.empty)(_ ++ _)).futureValue

          Json.parse(result.toArray) mustEqual json
        }

        "when one query parameter" in {
          val filterParams = FilterParams(Seq("referenceNumber" -> Seq("AD000001")))

          val url = "/crdl-cache/offices?referenceNumber=AD000001"

          val json = Json.parse("""
              |[
              |  {
              |    "languageCode": "EN",
              |    "customsOfficeLsd": {
              |      "customsOfficeUsualName": "CUSTOMS OFFICE SANT JULIÀ DE LÒRIA"
              |    },
              |    "phoneNumber": "+ (376) 84 1090",
              |    "referenceNumber": "AD000001",
              |    "countryCode": "AD",
              |    "customsOfficeTimetable": {
              |      "customsOfficeTimetableLine": [
              |        {
              |          "customsOfficeRoleTrafficCompetence": [
              |            {
              |              "roleName": "AUT"
              |            },
              |            {
              |              "roleName": "DEP"
              |            },
              |            {
              |              "roleName": "DES"
              |            },
              |            {
              |              "roleName": "TRA"
              |            }
              |          ]
              |        }
              |      ]
              |    }
              |  }
              |]
              |""".stripMargin)

          server.stubFor(
            get(urlEqualTo(url))
              .withHeader(AUTHORIZATION, equalTo("crdl-cache-test-token"))
              .willReturn(okJson(Json.stringify(json)))
          )

          val result = connector.get(codeList, filterParams).futureValue.runWith(Sink.fold(ByteString.empty)(_ ++ _)).futureValue

          Json.parse(result.toArray) mustEqual json
        }

        "when multiple query parameters" in {
          val filterParams = FilterParams(Seq("referenceNumber" -> Seq("AD000001", "AD000002")))

          val url = "/crdl-cache/offices?referenceNumber=AD000001&referenceNumber=AD000002"

          val json = Json.parse("""
              |[
              |  {
              |    "languageCode": "EN",
              |    "customsOfficeLsd": {
              |      "customsOfficeUsualName": "CUSTOMS OFFICE SANT JULIÀ DE LÒRIA"
              |    },
              |    "phoneNumber": "+ (376) 84 1090",
              |    "referenceNumber": "AD000001",
              |    "countryCode": "AD",
              |    "customsOfficeTimetable": {
              |      "customsOfficeTimetableLine": [
              |        {
              |          "customsOfficeRoleTrafficCompetence": [
              |            {
              |              "roleName": "AUT"
              |            },
              |            {
              |              "roleName": "DEP"
              |            },
              |            {
              |              "roleName": "DES"
              |            },
              |            {
              |              "roleName": "TRA"
              |            }
              |          ]
              |        }
              |      ]
              |    }
              |  },
              |  {
              |    "languageCode": "EN",
              |    "customsOfficeLsd": {
              |      "customsOfficeUsualName": "DCNJ PORTA"
              |    },
              |    "phoneNumber": "+ (376) 755125",
              |    "referenceNumber": "AD000002",
              |    "countryCode": "AD",
              |    "customsOfficeTimetable": {
              |      "customsOfficeTimetableLine": [
              |        {
              |          "customsOfficeRoleTrafficCompetence": [
              |            {
              |              "roleName": "DEP"
              |            },
              |            {
              |              "roleName": "DES"
              |            },
              |            {
              |              "roleName": "TRA"
              |            }
              |          ]
              |        }
              |      ]
              |    }
              |  }
              |]
              |""".stripMargin)

          server.stubFor(
            get(urlEqualTo(url))
              .withHeader(AUTHORIZATION, equalTo("crdl-cache-test-token"))
              .willReturn(okJson(Json.stringify(json)))
          )

          val result = connector.get(codeList, filterParams).futureValue.runWith(Sink.fold(ByteString.empty)(_ ++ _)).futureValue

          Json.parse(result.toArray) mustEqual json
        }
      }
    }
  }
}
