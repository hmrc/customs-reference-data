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
import play.api.libs.json.{JsArray, Json}

import scala.util.{Failure, Success}

class ListRetrievalServiceSpec extends SpecBase with ScalaCheckPropertyChecks {

  private val mockResourceService = mock[ResourceService]

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
          forAll(listNameGen) {
            listName =>
              when(mockResourceService.getJson(any(), any())).thenReturn(Success(JsArray()))
              val service  = new ListRetrievalService(mockResourceService)
              val codeList = CodeList(listName)
              val result   = service.get(codeList, Phase5, None)
              result.isSuccess mustEqual true
          }
        }
      }

      "should fail" - {
        "when code list not found" in {
          when(mockResourceService.getJson(any(), any())).thenReturn(Failure(new Throwable("")))
          val service  = new ListRetrievalService(mockResourceService)
          val codeList = RefDataCodeList(ListName("foo"), "foo")
          val result   = service.get(codeList, Phase5, None)
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
            when(mockResourceService.getJson(any(), any())).thenReturn(Success(customsOffices))
            val service      = new ListRetrievalService(mockResourceService)
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

          "when multiple filters" in {
            when(mockResourceService.getJson(any(), any())).thenReturn(Success(customsOffices))

            val service      = new ListRetrievalService(mockResourceService)
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
          when(mockResourceService.getJson(any(), any())).thenReturn(Success(json))
          val service      = new ListRetrievalService(mockResourceService)
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
  }

  "when phase 6" - {

    val listNameGen = Gen.oneOf(
      "AdditionalInformation",
      "AdditionalReference"
    )

    "get (without filter)" - {
      "should succeed" - {
        "when code list found" in {
          val service = new ListRetrievalService(mockResourceService)
          forAll(listNameGen) {
            listName =>
              val codeList = CodeList(listName)
              val result   = service.get(codeList, Phase6, None)
              result.isSuccess mustEqual true
          }
        }
      }

      "should fail" - {
        "when code list not found" in {
          when(mockResourceService.getJson(any(), any())).thenReturn(Failure(new Throwable("")))
          val service  = new ListRetrievalService(mockResourceService)
          val codeList = RefDataCodeList(ListName("foo"), "foo")
          val result   = service.get(codeList, Phase6, None)
          result.isSuccess mustEqual false
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

        when(mockResourceService.getJson(any(), any())).thenReturn(Success(data))

        val service      = new ListRetrievalService(mockResourceService)
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

        when(mockResourceService.getJson(any(), any())).thenReturn(Success(data))

        val service      = new ListRetrievalService(mockResourceService)
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

        when(mockResourceService.getJson(any(), any())).thenReturn(Success(data))

        val service      = new ListRetrievalService(mockResourceService)
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

      "when customs offices" - {
        "when querying by reference number" in {
          val codeList = CodeList("CustomsOffices")

          val data = Json
            .parse("""
                |[
                |  {
                |    "customsOfficeLsd": {
                |      "languageCode": "EN",
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
                |    "customsOfficeLsd": {
                |      "languageCode": "EN",
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
            .as[JsArray]

          when(mockResourceService.getJson(any(), any())).thenReturn(Success(data))

          val service      = new ListRetrievalService(mockResourceService)
          val filterParams = FilterParams(Seq("referenceNumbers" -> Seq("AD000001")))
          val result       = service.get(codeList, Phase6, Some(filterParams))
          val expectedResult = Json
            .parse("""
                |[
                |  {
                |    "customsOfficeLsd": {
                |      "languageCode": "EN",
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
            .as[JsArray]

          result.get.value.toSet mustEqual expectedResult.value.toSet
        }

        "when querying by role" in {
          val codeList = CodeList("CustomsOffices")

          val data = Json
            .parse("""
                |[
                |  {
                |    "customsOfficeLsd": {
                |      "languageCode": "EN",
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
                |              "roleName": "DES"
                |            },
                |            {
                |              "roleName": "DEP"
                |            }
                |          ]
                |        },
                |        {
                |          "customsOfficeRoleTrafficCompetence": [
                |            {
                |              "roleName": "AUT"
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
                |    "customsOfficeLsd": {
                |      "languageCode": "EN",
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
            .as[JsArray]

          when(mockResourceService.getJson(any(), any())).thenReturn(Success(data))

          val service      = new ListRetrievalService(mockResourceService)
          val filterParams = FilterParams(Seq("roles" -> Seq("AUT")))
          val result       = service.get(codeList, Phase6, Some(filterParams))
          val expectedResult = Json
            .parse("""
                |[
                |  {
                |    "customsOfficeLsd": {
                |      "languageCode": "EN",
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
                |              "roleName": "DES"
                |            },
                |            {
                |              "roleName": "DEP"
                |            }
                |          ]
                |        },
                |        {
                |          "customsOfficeRoleTrafficCompetence": [
                |            {
                |              "roleName": "AUT"
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
            .as[JsArray]

          result.get.value.toSet mustEqual expectedResult.value.toSet
        }

        "when querying by country codes" in {
          val codeList = CodeList("CustomsOffices")

          val data = Json
            .parse("""
                |[
                |  {
                |    "customsOfficeLsd": {
                |      "languageCode": "EN",
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
                |    "customsOfficeLsd": {
                |      "languageCode": "EN",
                |      "customsOfficeUsualName": "Glasgow Airport"
                |    },
                |    "phoneNumber": "+44(0)300 106 3520",
                |    "referenceNumber": "GB000054",
                |    "countryCode": "GB",
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
                |  },
                |  {
                |    "customsOfficeLsd": {
                |      "languageCode": "EN",
                |      "customsOfficeUsualName": "Belfast International Airport"
                |    },
                |    "phoneNumber": "+44 (0)3000 575 988",
                |    "referenceNumber": "XI000014",
                |    "countryCode": "XI",
                |    "customsOfficeTimetable": {
                |      "customsOfficeTimetableLine": [
                |        {
                |          "customsOfficeRoleTrafficCompetence": [
                |            {
                |              "roleName": "ENT"
                |            },
                |            {
                |              "roleName": "EXP"
                |            },
                |            {
                |              "roleName": "EXT"
                |            }
                |          ]
                |        }
                |      ]
                |    }
                |  }
                |]
                |""".stripMargin)
            .as[JsArray]

          when(mockResourceService.getJson(any(), any())).thenReturn(Success(data))

          val service      = new ListRetrievalService(mockResourceService)
          val filterParams = FilterParams(Seq("countryCodes" -> Seq("GB", "XI")))
          val result       = service.get(codeList, Phase6, Some(filterParams))
          val expectedResult = Json
            .parse("""
                |[
                |  {
                |    "customsOfficeLsd": {
                |      "languageCode": "EN",
                |      "customsOfficeUsualName": "Glasgow Airport"
                |    },
                |    "phoneNumber": "+44(0)300 106 3520",
                |    "referenceNumber": "GB000054",
                |    "countryCode": "GB",
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
                |  },
                |  {
                |    "customsOfficeLsd": {
                |      "languageCode": "EN",
                |      "customsOfficeUsualName": "Belfast International Airport"
                |    },
                |    "phoneNumber": "+44 (0)3000 575 988",
                |    "referenceNumber": "XI000014",
                |    "countryCode": "XI",
                |    "customsOfficeTimetable": {
                |      "customsOfficeTimetableLine": [
                |        {
                |          "customsOfficeRoleTrafficCompetence": [
                |            {
                |              "roleName": "ENT"
                |            },
                |            {
                |              "roleName": "EXP"
                |            },
                |            {
                |              "roleName": "EXT"
                |            }
                |          ]
                |        }
                |      ]
                |    }
                |  }
                |]
                |""".stripMargin)
            .as[JsArray]

          result.get.value.toSet mustEqual expectedResult.value.toSet
        }

        "when multiple offices have the same ID" - {
          "must prioritise EN language code if there is one" in {
            val codeList = CodeList("CustomsOffices")

            val data = Json
              .parse("""
                  |[
                  |  {
                  |    "customsOfficeLsd": {
                  |      "languageCode": "EN",
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
                  |    "customsOfficeLsd": {
                  |      "languageCode": "ES",
                  |      "customsOfficeUsualName": "ADUANA DE ST. JULIÀ DE LÒRIA"
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
                  |    "customsOfficeLsd": {
                  |      "languageCode": "FR",
                  |      "customsOfficeUsualName": "BUREAU DE SANT JULIÀ DE LÒRIA"
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
                  |    "customsOfficeLsd": {
                  |      "languageCode": "EN",
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
                  |  },
                  |  {
                  |    "customsOfficeLsd": {
                  |      "languageCode": "ES",
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
                  |  },
                  |  {
                  |    "customsOfficeLsd": {
                  |      "languageCode": "FR",
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
              .as[JsArray]

            when(mockResourceService.getJson(any(), any())).thenReturn(Success(data))

            val service = new ListRetrievalService(mockResourceService)
            val result  = service.get(codeList, Phase6, None)
            val expectedResult = Json
              .parse("""
                  |[
                  |  {
                  |    "customsOfficeLsd": {
                  |      "languageCode": "EN",
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
                  |    "customsOfficeLsd": {
                  |      "languageCode": "EN",
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
              .as[JsArray]

            result.get.value.toSet mustEqual expectedResult.value.toSet
          }

          "must take first language code if there is no EN language code" in {
            val codeList = CodeList("CustomsOffices")

            val data = Json
              .parse("""
                  |[
                  |  {
                  |    "customsOfficeLsd": {
                  |      "languageCode": "ES",
                  |      "customsOfficeUsualName": "ADUANA DE ST. JULIÀ DE LÒRIA"
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
                  |    "customsOfficeLsd": {
                  |      "languageCode": "FR",
                  |      "customsOfficeUsualName": "BUREAU DE SANT JULIÀ DE LÒRIA"
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
                  |    "customsOfficeLsd": {
                  |      "languageCode": "ES",
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
                  |  },
                  |  {
                  |    "customsOfficeLsd": {
                  |      "languageCode": "FR",
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
              .as[JsArray]

            when(mockResourceService.getJson(any(), any())).thenReturn(Success(data))

            val service = new ListRetrievalService(mockResourceService)
            val result  = service.get(codeList, Phase6, None)

            val expectedResult = Json
              .parse("""
                |[
                |  {
                |    "customsOfficeLsd": {
                |      "languageCode": "ES",
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
                |  },
                |  {
                |    "customsOfficeLsd": {
                |      "languageCode": "ES",
                |      "customsOfficeUsualName": "ADUANA DE ST. JULIÀ DE LÒRIA"
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
              .as[JsArray]

            result.get.value.toSet mustEqual expectedResult.value.toSet
          }
        }
      }
    }
  }
}
