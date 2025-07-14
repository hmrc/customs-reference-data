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

package services.consumption.testOnly

import base.SpecBase
import models.CodeList.RefDataCodeList
import models.Phase.*
import models.{CodeList, FilterParams, ListName}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.{JsArray, Json}
import play.api.test.Helpers.*

import scala.util.Success

class ListRetrievalServiceSpec extends SpecBase with ScalaCheckPropertyChecks {

  "when phase 5" - {

    val listNameGen = Gen.oneOf(
      "AdditionalInformation",
      "AdditionalReference",
      "AdditionalSupplyChainActorRoleCode",
      "AuthorisationTypeDeparture",
      "ControlType",
      "CountryAddressPostcodeBased",
      "CountryCodesCommonTransit",
      "CountryCodesCommunity",
      "CountryCodesCTC",
      "CountryCodesForAddress",
      "CountryCodesFullList",
      "CountryCustomsSecurityAgreementArea",
      "CountryWithoutZip",
      "CurrencyCodes",
      "CUSCode",
      "CustomsOffices",
      "DeclarationType",
      "DeclarationTypeAdditional",
      "DeclarationTypeItemLevel",
      "DeclarationTypeSecurity",
      "FunctionalErrorCodesIeCA",
      "GuaranteeType",
      "HScode",
      "KindOfPackages",
      "KindOfPackagesBulk",
      "KindOfPackagesUnpacked",
      "Nationality",
      "PreviousDocumentExportType",
      "PreviousDocumentType",
      "QualifierOfTheIdentification",
      "SpecificCircumstanceIndicatorCode",
      "SupportingDocumentType",
      "TransportChargesMethodOfPayment",
      "TransportDocumentType",
      "TransportModeCode",
      "TypeOfIdentificationOfMeansOfTransport",
      "TypeOfIdentificationofMeansOfTransportActive",
      "TypeOfLocation",
      "UnDangerousGoodsCode",
      "Unit",
      "UnLocodeExtended"
    )

    "get (without filter)" - {
      "should succeed" - {
        "when code list found" in {
          running(baseApplicationBuilder) {
            application =>
              val service = application.injector.instanceOf[ListRetrievalService]
              forAll(listNameGen) {
                listName =>
                  val codeList = CodeList(listName)
                  val result   = service.get(codeList, Phase5, None)
                  result.isSuccess mustEqual true
              }
          }
        }
      }

      "should fail" - {
        "when code list not found" in {
          running(baseApplicationBuilder) {
            application =>
              val service  = application.injector.instanceOf[ListRetrievalService]
              val codeList = RefDataCodeList(ListName("foo"), "foo")
              val result   = service.get(codeList, Phase5, None)
              result.isSuccess mustEqual false
          }
        }
      }
    }

    "get (with filter)" - {
      "when customs offices" - {

        val codeList = CodeList("CustomsOffices")

        val customsOffices = Json
          .parse("""
              |[
              |  {
              |    "name": "CO1",
              |    "id": "XI1",
              |    "countryId": "XI",
              |    "roles": [
              |      {
              |        "role": "DEP"
              |      },
              |      {
              |        "role": "TRA"
              |      }
              |    ]
              |  },
              |  {
              |    "name": "CO2",
              |    "id": "GB1",
              |    "countryId": "GB",
              |    "roles": [
              |      {
              |        "role": "TRA"
              |      },
              |      {
              |        "role": "AUT"
              |      }
              |    ]
              |  },
              |  {
              |    "name": "CO3",
              |    "id": "GB2",
              |    "countryId": "GB",
              |    "roles": [
              |      {
              |        "role": "TRA"
              |      },
              |      {
              |        "role": "DES"
              |      }
              |    ]
              |  }
              |]
              |""".stripMargin)
          .as[JsArray]

        "must return values that match the filter" - {
          "when one filter" in {
            val mockResourceService = mock[ResourceService]
            when(mockResourceService.getJson(any(), any())).thenReturn(Success(customsOffices))

            val app = baseApplicationBuilder
              .apply {
                new GuiceApplicationBuilder()
                  .overrides(bind[ResourceService].toInstance(mockResourceService))
              }
              .build()

            running(app) {
              val service      = app.injector.instanceOf[ListRetrievalService]
              val filterParams = FilterParams(Seq("data.countryId" -> Seq("GB")))
              val result       = service.get(codeList, Phase5, Some(filterParams))
              result.get mustEqual Json.parse("""
                  |[
                  |  {
                  |    "name": "CO2",
                  |    "id": "GB1",
                  |    "countryId": "GB",
                  |    "roles": [
                  |      {
                  |        "role": "TRA"
                  |      },
                  |      {
                  |        "role": "AUT"
                  |      }
                  |    ]
                  |  },
                  |  {
                  |    "name": "CO3",
                  |    "id": "GB2",
                  |    "countryId": "GB",
                  |    "roles": [
                  |      {
                  |        "role": "TRA"
                  |      },
                  |      {
                  |        "role": "DES"
                  |      }
                  |    ]
                  |  }
                  |]
                  |""".stripMargin)
            }
          }

          "when multiple filters" in {
            val mockResourceService = mock[ResourceService]
            when(mockResourceService.getJson(any(), any())).thenReturn(Success(customsOffices))

            val app = baseApplicationBuilder
              .apply {
                new GuiceApplicationBuilder()
                  .overrides(bind[ResourceService].toInstance(mockResourceService))
              }
              .build()

            running(app) {
              val service      = app.injector.instanceOf[ListRetrievalService]
              val filterParams = FilterParams(Seq("data.countryId" -> Seq("GB"), "data.roles.role" -> Seq("AUT", "DES")))
              val result       = service.get(codeList, Phase5, Some(filterParams))
              result.get mustEqual Json.parse("""
                  |[
                  |  {
                  |    "name": "CO2",
                  |    "id": "GB1",
                  |    "countryId": "GB",
                  |    "roles": [
                  |      {
                  |        "role": "TRA"
                  |      },
                  |      {
                  |        "role": "AUT"
                  |      }
                  |    ]
                  |  },
                  |  {
                  |    "name": "CO3",
                  |    "id": "GB2",
                  |    "countryId": "GB",
                  |    "roles": [
                  |      {
                  |        "role": "TRA"
                  |      },
                  |      {
                  |        "role": "DES"
                  |      }
                  |    ]
                  |  }
                  |]
                  |""".stripMargin)
            }
          }
        }
      }

      "when countries" - {

        val codeList = CodeList("CountryCodesFullList")

        "must return values that match the filter" in {
          running(baseApplicationBuilder) {
            app =>
              val service      = app.injector.instanceOf[ListRetrievalService]
              val filterParams = FilterParams(Seq("data.code" -> Seq("AD")))
              val result       = service.get(codeList, Phase5, Some(filterParams))
              result.get mustEqual Json.parse("""
                  |[
                  |  {
                  |    "code": "AD",
                  |    "description": "Andorra"
                  |  }
                  |]
                  |""".stripMargin)
          }
        }
      }

      "when security types" - {

        val codeList = CodeList("DeclarationTypeSecurity")

        "must return values (with unescaped XML)" - {
          "when no filtering" in {
            running(baseApplicationBuilder) {
              app =>
                val service = app.injector.instanceOf[ListRetrievalService]
                val result  = service.get(codeList, Phase5, None)
                result.get mustEqual Json.parse("""
                    |[
                    |  {
                    |    "code": "0",
                    |    "description": "Not used for safety and security purposes"
                    |  },
                    |  {
                    |    "code": "1",
                    |    "description": "ENS"
                    |  },
                    |  {
                    |    "code": "2",
                    |    "description": "EXS"
                    |  },
                    |  {
                    |    "code": "3",
                    |    "description": "ENS & EXS"
                    |  }
                    |]
                    |""".stripMargin)
            }
          }

          "when filtering" in {
            running(baseApplicationBuilder) {
              app =>
                val service      = app.injector.instanceOf[ListRetrievalService]
                val filterParams = FilterParams(Seq("data.code" -> Seq("3")))
                val result       = service.get(codeList, Phase5, Some(filterParams))
                result.get mustEqual Json.parse("""
                    |[
                    |  {
                    |    "code": "3",
                    |    "description": "ENS & EXS"
                    |  }
                    |]
                    |""".stripMargin)
            }
          }
        }
      }
    }
  }

  "when phase 6" - {

    val listNameGen = Gen.oneOf(
      "AdditionalInformation",
      "AdditionalReference"
    )

    "get (without filter)" - {
      "should succeed" - {
        "when code list found" in {
          running(baseApplicationBuilder) {
            application =>
              val service = application.injector.instanceOf[ListRetrievalService]
              forAll(listNameGen) {
                listName =>
                  val codeList = CodeList(listName)
                  val result   = service.get(codeList, Phase6, None)
                  result.isSuccess mustEqual true
              }
          }
        }
      }

      "should fail" - {
        "when code list not found" in {
          running(baseApplicationBuilder) {
            application =>
              val service  = application.injector.instanceOf[ListRetrievalService]
              val codeList = RefDataCodeList(ListName("foo"), "foo")
              val result   = service.get(codeList, Phase6, None)
              result.isSuccess mustEqual false
          }
        }
      }
    }

    "get (with filter)" - {

      val codeList = CodeList("AdditionalInformation")

      "when one filter with one value" in {
        val data = Json
          .parse("""
              |[
              |  {
              |    "key": "00200",
              |    "value": "Several occurrences of documents and parties"
              |  },
              |  {
              |    "key": "00700",
              |    "value": "Discharge of inward processing. IP’ and the relevant authorisation number or INF number"
              |  }
              |]
              |""".stripMargin)
          .as[JsArray]

        val mockResourceService = mock[ResourceService]
        when(mockResourceService.getJson(any(), any())).thenReturn(Success(data))

        val app = baseApplicationBuilder
          .apply {
            new GuiceApplicationBuilder()
              .overrides(bind[ResourceService].toInstance(mockResourceService))
          }
          .build()

        running(app) {
          val service      = app.injector.instanceOf[ListRetrievalService]
          val filterParams = FilterParams(Seq("keys" -> Seq("00200")))
          val result       = service.get(codeList, Phase6, Some(filterParams))
          result.get mustEqual Json.parse("""
              |[
              |  {
              |    "key": "00200",
              |    "value": "Several occurrences of documents and parties"
              |  }
              |]
              |""".stripMargin)
        }
      }

      "when one filter with multiple values" in {
        val data = Json
          .parse("""
              |[
              |  {
              |    "key": "00200",
              |    "value": "Several occurrences of documents and parties"
              |  },
              |  {
              |    "key": "00700",
              |    "value": "Discharge of inward processing. IP’ and the relevant authorisation number or INF number"
              |  },
              |  {
              |    "key": "00800",
              |    "value": "Discharge of inward processing (specific commercial policy measures)"
              |  }
              |]
              |""".stripMargin)
          .as[JsArray]

        val mockResourceService = mock[ResourceService]
        when(mockResourceService.getJson(any(), any())).thenReturn(Success(data))

        val app = baseApplicationBuilder
          .apply {
            new GuiceApplicationBuilder()
              .overrides(bind[ResourceService].toInstance(mockResourceService))
          }
          .build()

        running(app) {
          val service      = app.injector.instanceOf[ListRetrievalService]
          val filterParams = FilterParams(Seq("keys" -> Seq("00200", "00700")))
          val result       = service.get(codeList, Phase6, Some(filterParams))
          result.get mustEqual Json.parse("""
              |[
              |  {
              |    "key": "00200",
              |    "value": "Several occurrences of documents and parties"
              |  },
              |  {
              |    "key": "00700",
              |    "value": "Discharge of inward processing. IP’ and the relevant authorisation number or INF number"
              |  }
              |]
              |""".stripMargin)
        }
      }

      "when multiple filters" in {
        val data = Json
          .parse("""
            |[
            |  {
            |    "key": "00200",
            |    "value": "Several occurrences of documents and parties",
            |    "properties": {
            |      "state": "valid"
            |    }
            |  },
            |  {
            |    "key": "00200",
            |    "value": "Several occurrences of documents and parties",
            |    "properties": {
            |      "state": "invalid"
            |    }
            |  }
            |]
            |""".stripMargin)
          .as[JsArray]

        val mockResourceService = mock[ResourceService]
        when(mockResourceService.getJson(any(), any())).thenReturn(Success(data))

        val app = baseApplicationBuilder
          .apply {
            new GuiceApplicationBuilder()
              .overrides(bind[ResourceService].toInstance(mockResourceService))
          }
          .build()

        running(app) {
          val service      = app.injector.instanceOf[ListRetrievalService]
          val filterParams = FilterParams(Seq("keys" -> Seq("00200"), "state" -> Seq("valid")))
          val result       = service.get(codeList, Phase6, Some(filterParams))
          result.get mustEqual Json.parse("""
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
        }
      }

      "when customs offices" in {
        val codeList = CodeList("CustomsOffices")

        val data = Json
          .parse("""
              |[
              |  {
              |    "languageCode": "EN",
              |    "name": "CUSTOMS OFFICE SANT JULIÀ DE LÒRIA",
              |    "phoneNumber": "+ (376) 84 1090",
              |    "id": "AD000001",
              |    "countryId": "AD",
              |    "roles": [
              |      {
              |        "role": "AUT"
              |      },
              |      {
              |        "role": "DEP"
              |      },
              |      {
              |        "role": "DES"
              |      },
              |      {
              |        "role": "TRA"
              |      }
              |    ]
              |  },
              |  {
              |     "languageCode": "EN",
              |     "name": "DCNJ PORTA",
              |     "phoneNumber": "+ (376) 755125",
              |     "eMailAddress": "duana.pasdelacasa@andorra.ad",
              |     "id": "AD000002",
              |     "countryId": "AD",
              |     "roles": [
              |       {
              |         "role": "DEP"
              |       },
              |       {
              |         "role": "DES"
              |       },
              |       {
              |         "role": "TRA"
              |       }
              |     ]
              |  }
              |]
              |""".stripMargin)
          .as[JsArray]

        val mockResourceService = mock[ResourceService]
        when(mockResourceService.getJson(any(), any())).thenReturn(Success(data))

        val app = baseApplicationBuilder
          .apply {
            new GuiceApplicationBuilder()
              .overrides(bind[ResourceService].toInstance(mockResourceService))
          }
          .build()

        running(app) {
          val service      = app.injector.instanceOf[ListRetrievalService]
          val filterParams = FilterParams(Seq("data.id" -> Seq("AD000001")))
          val result       = service.get(codeList, Phase6, Some(filterParams))
          result.get mustEqual Json.parse("""
              |[
              |  {
              |    "languageCode": "EN",
              |    "name": "CUSTOMS OFFICE SANT JULIÀ DE LÒRIA",
              |    "phoneNumber": "+ (376) 84 1090",
              |    "id": "AD000001",
              |    "countryId": "AD",
              |    "roles": [
              |      {
              |        "role": "AUT"
              |      },
              |      {
              |        "role": "DEP"
              |      },
              |      {
              |        "role": "DES"
              |      },
              |      {
              |        "role": "TRA"
              |      }
              |    ]
              |  }
              |]
              |""".stripMargin)
        }
      }
    }
  }
}
