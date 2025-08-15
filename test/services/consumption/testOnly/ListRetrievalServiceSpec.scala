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
import models.{CodeList, FilterParams, ListName}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.libs.json.{JsArray, Json}

import scala.util.{Failure, Success}

class ListRetrievalServiceSpec extends SpecBase with ScalaCheckPropertyChecks {

  private val mockResourceService = mock[ResourceService]

  "ListRetrievalService" - {

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
          forAll(listNameGen) {
            listName =>
              when(mockResourceService.getJson(any())).thenReturn(Success(JsArray()))
              val service  = new ListRetrievalService(mockResourceService)
              val codeList = CodeList(listName)
              val result   = service.get(codeList, None)
              result.isSuccess mustEqual true
          }
        }
      }

      "should fail" - {
        "when code list not found" in {
          when(mockResourceService.getJson(any())).thenReturn(Failure(new Throwable("")))
          val service  = new ListRetrievalService(mockResourceService)
          val codeList = RefDataCodeList(ListName("foo"), "foo")
          val result   = service.get(codeList, None)
          result.isSuccess mustEqual false
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
            when(mockResourceService.getJson(any())).thenReturn(Success(customsOffices))
            val service      = new ListRetrievalService(mockResourceService)
            val filterParams = FilterParams(Seq("data.countryId" -> Seq("GB")))
            val result       = service.get(codeList, Some(filterParams))
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

          "when multiple filters" in {
            when(mockResourceService.getJson(any())).thenReturn(Success(customsOffices))

            val service      = new ListRetrievalService(mockResourceService)
            val filterParams = FilterParams(Seq("data.countryId" -> Seq("GB"), "data.roles.role" -> Seq("AUT", "DES")))
            val result       = service.get(codeList, Some(filterParams))
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

      "when countries" - {

        val codeList = CodeList("CountryCodesFullList")

        val json = Json
          .parse("""
            |[
            |  {
            |    "code": "AD",
            |    "description": "Andorra"
            |  },
            |  {
            |    "code": "ES",
            |    "description": "Spain"
            |  },
            |  {
            |    "code": "FR",
            |    "description": "France"
            |  }
            |]
            |""".stripMargin)
          .as[JsArray]

        "must return values that match the filter" in {
          when(mockResourceService.getJson(any())).thenReturn(Success(json))
          val service      = new ListRetrievalService(mockResourceService)
          val filterParams = FilterParams(Seq("data.code" -> Seq("AD")))
          val result       = service.get(codeList, Some(filterParams))
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
  }
}
