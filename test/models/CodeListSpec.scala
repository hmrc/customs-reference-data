/*
 * Copyright 2024 HM Revenue & Customs
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
import models.CodeList.{ColDataCodeList, RefDataCodeList}
import play.api.mvc.PathBindable

class CodeListSpec extends SpecBase {

  "pathBindable" - {

    val pathBindable = implicitly[PathBindable[CodeList]]

    "bind" - {
      "must succeed" - {
        "when ref data code" - {
          "when AdditionalInformation" in {
            val listName = "AdditionalInformation"
            val result   = pathBindable.bind("codeList", listName)
            result.value mustEqual RefDataCodeList(ListName(listName), "CL239")
          }

          "when AdditionalInformationCodeSubset" in {
            val listName = "AdditionalInformationCodeSubset"
            val result   = pathBindable.bind("codeList", listName)
            result.value mustEqual RefDataCodeList(ListName(listName), "CL752")
          }

          "when AdditionalReference" in {
            val listName = "AdditionalReference"
            val result   = pathBindable.bind("codeList", listName)
            result.value mustEqual RefDataCodeList(ListName(listName), "CL380")
          }

          "when AdditionalSupplyChainActorRoleCode" in {
            val listName = "AdditionalSupplyChainActorRoleCode"
            val result   = pathBindable.bind("codeList", listName)
            result.value mustEqual RefDataCodeList(ListName(listName), "CL704")
          }

          "when AuthorisationTypeDeparture" in {
            val listName = "AuthorisationTypeDeparture"
            val result   = pathBindable.bind("codeList", listName)
            result.value mustEqual RefDataCodeList(ListName(listName), "CL235")
          }

          "when AuthorisationTypeDestination" in {
            val listName = "AuthorisationTypeDestination"
            val result   = pathBindable.bind("codeList", listName)
            result.value mustEqual RefDataCodeList(ListName(listName), "CL236")
          }

          "when BusinessRejectionTypeDepExp" in {
            val listName = "BusinessRejectionTypeDepExp"
            val result   = pathBindable.bind("codeList", listName)
            result.value mustEqual RefDataCodeList(ListName(listName), "CL560")
          }

          "when BusinessRejectionTypeTED2Dep" in {
            val listName = "BusinessRejectionTypeTED2Dep"
            val result   = pathBindable.bind("codeList", listName)
            result.value mustEqual RefDataCodeList(ListName(listName), "CL561")
          }

          "when BusinessRejectionTypeTra" in {
            val listName = "BusinessRejectionTypeTra"
            val result   = pathBindable.bind("codeList", listName)
            result.value mustEqual RefDataCodeList(ListName(listName), "CL580")
          }

          "when ControlType" in {
            val listName = "ControlType"
            val result   = pathBindable.bind("codeList", listName)
            result.value mustEqual RefDataCodeList(ListName(listName), "CL716")
          }

          "when CountryAddressPostcodeBased" in {
            val listName = "CountryAddressPostcodeBased"
            val result   = pathBindable.bind("codeList", listName)
            result.value mustEqual RefDataCodeList(ListName(listName), "CL190")
          }

          "when CountryAddressPostcodeOnly" in {
            val listName = "CountryAddressPostcodeOnly"
            val result   = pathBindable.bind("codeList", listName)
            result.value mustEqual RefDataCodeList(ListName(listName), "CL198")
          }

          "when CountryCodesCommonTransit" in {
            val listName = "CountryCodesCommonTransit"
            val result   = pathBindable.bind("codeList", listName)
            result.value mustEqual RefDataCodeList(ListName(listName), "CL009")
          }

          "when CountryCodesCommunity" in {
            val listName = "CountryCodesCommunity"
            val result   = pathBindable.bind("codeList", listName)
            result.value mustEqual RefDataCodeList(ListName(listName), "CL010")
          }

          "when CountryCodesCTC" in {
            val listName = "CountryCodesCTC"
            val result   = pathBindable.bind("codeList", listName)
            result.value mustEqual RefDataCodeList(ListName(listName), "CL112")
          }

          "when CountryCodesForAddress" in {
            val listName = "CountryCodesForAddress"
            val result   = pathBindable.bind("codeList", listName)
            result.value mustEqual RefDataCodeList(ListName(listName), "CL248")
          }

          "when CountryCodesFullList" in {
            val listName = "CountryCodesFullList"
            val result   = pathBindable.bind("codeList", listName)
            result.value mustEqual RefDataCodeList(ListName(listName), "CL008")
          }

          "when CountryCodesOptOut" in {
            val listName = "CountryCodesOptOut"
            val result   = pathBindable.bind("codeList", listName)
            result.value mustEqual RefDataCodeList(ListName(listName), "CL167")
          }

          "when CountryCodesWithAddress" in {
            val listName = "CountryCodesWithAddress"
            val result   = pathBindable.bind("codeList", listName)
            result.value mustEqual RefDataCodeList(ListName(listName), "CL199")
          }

          "when CountryCustomsSecurityAgreementArea" in {
            val listName = "CountryCustomsSecurityAgreementArea"
            val result   = pathBindable.bind("codeList", listName)
            result.value mustEqual RefDataCodeList(ListName(listName), "CL147")
          }

          "when CountryPlaceOfLoadingNotRequired" in {
            val listName = "CountryPlaceOfLoadingNotRequired"
            val result   = pathBindable.bind("codeList", listName)
            result.value mustEqual RefDataCodeList(ListName(listName), "CL289")
          }

          "when CountryWithoutZip" in {
            val listName = "CountryWithoutZip"
            val result   = pathBindable.bind("codeList", listName)
            result.value mustEqual RefDataCodeList(ListName(listName), "CL505")
          }

          "when CurrencyCodes" in {
            val listName = "CurrencyCodes"
            val result   = pathBindable.bind("codeList", listName)
            result.value mustEqual RefDataCodeList(ListName(listName), "CL048")
          }

          "when CUSCode" in {
            val listName = "CUSCode"
            val result   = pathBindable.bind("codeList", listName)
            result.value mustEqual RefDataCodeList(ListName(listName), "CL016")
          }

          "when CustomsOffices" in {
            val listName = "CustomsOffices"
            val result   = pathBindable.bind("codeList", listName)
            result.value mustEqual ColDataCodeList
          }

          "when DeclarationType" in {
            val listName = "DeclarationType"
            val result   = pathBindable.bind("codeList", listName)
            result.value mustEqual RefDataCodeList(ListName(listName), "CL231")
          }

          "when DeclarationTypeAdditional" in {
            val listName = "DeclarationTypeAdditional"
            val result   = pathBindable.bind("codeList", listName)
            result.value mustEqual RefDataCodeList(ListName(listName), "CL042")
          }

          "when DeclarationTypeItemLevel" in {
            val listName = "DeclarationTypeItemLevel"
            val result   = pathBindable.bind("codeList", listName)
            result.value mustEqual RefDataCodeList(ListName(listName), "CL232")
          }

          "when DeclarationTypeSecurity" in {
            val listName = "DeclarationTypeSecurity"
            val result   = pathBindable.bind("codeList", listName)
            result.value mustEqual RefDataCodeList(ListName(listName), "CL217")
          }

          "when DocumentTypeExcise" in {
            val listName = "DocumentTypeExcise"
            val result   = pathBindable.bind("codeList", listName)
            result.value mustEqual RefDataCodeList(ListName(listName), "CL234")
          }

          "when FunctionalErrorCodesIeCA" in {
            val listName = "FunctionalErrorCodesIeCA"
            val result   = pathBindable.bind("codeList", listName)
            result.value mustEqual RefDataCodeList(ListName(listName), "CL180")
          }

          "when FunctionErrorCodesTED" in {
            val listName = "FunctionErrorCodesTED"
            val result   = pathBindable.bind("codeList", listName)
            result.value mustEqual RefDataCodeList(ListName(listName), "CL437")
          }

          "when GuaranteeType" in {
            val listName = "GuaranteeType"
            val result   = pathBindable.bind("codeList", listName)
            result.value mustEqual RefDataCodeList(ListName(listName), "CL251")
          }

          "when GuaranteeTypeCTC" in {
            val listName = "GuaranteeTypeCTC"
            val result   = pathBindable.bind("codeList", listName)
            result.value mustEqual RefDataCodeList(ListName(listName), "CL229")
          }

          "when GuaranteeTypeEUNonTIR" in {
            val listName = "GuaranteeTypeEUNonTIR"
            val result   = pathBindable.bind("codeList", listName)
            result.value mustEqual RefDataCodeList(ListName(listName), "CL230")
          }

          "when GuaranteeTypeWithGRN" in {
            val listName = "GuaranteeTypeWithGRN"
            val result   = pathBindable.bind("codeList", listName)
            result.value mustEqual RefDataCodeList(ListName(listName), "CL286")
          }

          "when GuaranteeTypeWithReference" in {
            val listName = "GuaranteeTypeWithReference"
            val result   = pathBindable.bind("codeList", listName)
            result.value mustEqual RefDataCodeList(ListName(listName), "CL076")
          }

          "when HScode" in {
            val listName = "HScode"
            val result   = pathBindable.bind("codeList", listName)
            result.value mustEqual RefDataCodeList(ListName(listName), "CL152")
          }

          "when IncidentCode" in {
            val listName = "IncidentCode"
            val result   = pathBindable.bind("codeList", listName)
            result.value mustEqual RefDataCodeList(ListName(listName), "CL019")
          }

          "when InvalidGuaranteeReason" in {
            val listName = "InvalidGuaranteeReason"
            val result   = pathBindable.bind("codeList", listName)
            result.value mustEqual RefDataCodeList(ListName(listName), "CL252")
          }

          "when KindOfPackages" in {
            val listName = "KindOfPackages"
            val result   = pathBindable.bind("codeList", listName)
            result.value mustEqual RefDataCodeList(ListName(listName), "CL017")
          }

          "when KindOfPackagesBulk" in {
            val listName = "KindOfPackagesBulk"
            val result   = pathBindable.bind("codeList", listName)
            result.value mustEqual RefDataCodeList(ListName(listName), "CL181")
          }

          "when KindOfPackagesUnpacked" in {
            val listName = "KindOfPackagesUnpacked"
            val result   = pathBindable.bind("codeList", listName)
            result.value mustEqual RefDataCodeList(ListName(listName), "CL182")
          }

          "when Nationality" in {
            val listName = "Nationality"
            val result   = pathBindable.bind("codeList", listName)
            result.value mustEqual RefDataCodeList(ListName(listName), "CL165")
          }

          "when PreviousDocumentExportType" in {
            val listName = "PreviousDocumentExportType"
            val result   = pathBindable.bind("codeList", listName)
            result.value mustEqual RefDataCodeList(ListName(listName), "CL228")
          }

          "when PreviousDocumentType" in {
            val listName = "PreviousDocumentType"
            val result   = pathBindable.bind("codeList", listName)
            result.value mustEqual RefDataCodeList(ListName(listName), "CL214")
          }

          "when PreviousDocumentUnionGoods" in {
            val listName = "PreviousDocumentUnionGoods"
            val result   = pathBindable.bind("codeList", listName)
            result.value mustEqual RefDataCodeList(ListName(listName), "CL178")
          }

          "when QualifierOfIdentificationIncident" in {
            val listName = "QualifierOfIdentificationIncident"
            val result   = pathBindable.bind("codeList", listName)
            result.value mustEqual RefDataCodeList(ListName(listName), "CL038")
          }

          "when QualifierOfTheIdentification" in {
            val listName = "QualifierOfTheIdentification"
            val result   = pathBindable.bind("codeList", listName)
            result.value mustEqual RefDataCodeList(ListName(listName), "CL326")
          }

          "when RejectionCodeDepartureExport" in {
            val listName = "RejectionCodeDepartureExport"
            val result   = pathBindable.bind("codeList", listName)
            result.value mustEqual RefDataCodeList(ListName(listName), "CL226")
          }

          "when RejectionCodeTransit" in {
            val listName = "RejectionCodeTransit"
            val result   = pathBindable.bind("codeList", listName)
            result.value mustEqual RefDataCodeList(ListName(listName), "CL581")
          }

          "when RepresentativeStatusCode" in {
            val listName = "RepresentativeStatusCode"
            val result   = pathBindable.bind("codeList", listName)
            result.value mustEqual RefDataCodeList(ListName(listName), "CL094")
          }

          "when RequestedDocumentType" in {
            val listName = "RequestedDocumentType"
            val result   = pathBindable.bind("codeList", listName)
            result.value mustEqual RefDataCodeList(ListName(listName), "CL215")
          }

          "when Role" in {
            val listName = "Role"
            val result   = pathBindable.bind("codeList", listName)
            result.value mustEqual RefDataCodeList(ListName(listName), "CL056")
          }

          "when SpecificCircumstanceIndicatorCode" in {
            val listName = "SpecificCircumstanceIndicatorCode"
            val result   = pathBindable.bind("codeList", listName)
            result.value mustEqual RefDataCodeList(ListName(listName), "CL296")
          }

          "when SupportingDocumentType" in {
            val listName = "SupportingDocumentType"
            val result   = pathBindable.bind("codeList", listName)
            result.value mustEqual RefDataCodeList(ListName(listName), "CL213")
          }

          "when TransportChargesMethodOfPayment" in {
            val listName = "TransportChargesMethodOfPayment"
            val result   = pathBindable.bind("codeList", listName)
            result.value mustEqual RefDataCodeList(ListName(listName), "CL116")
          }

          "when TransportDocumentType" in {
            val listName = "TransportDocumentType"
            val result   = pathBindable.bind("codeList", listName)
            result.value mustEqual RefDataCodeList(ListName(listName), "CL754")
          }

          "when TransportModeCode" in {
            val listName = "TransportModeCode"
            val result   = pathBindable.bind("codeList", listName)
            result.value mustEqual RefDataCodeList(ListName(listName), "CL218")
          }

          "when TypeOfIdentificationOfMeansOfTransport" in {
            val listName = "TypeOfIdentificationOfMeansOfTransport"
            val result   = pathBindable.bind("codeList", listName)
            result.value mustEqual RefDataCodeList(ListName(listName), "CL750")
          }

          "when TypeOfIdentificationofMeansOfTransportActive" in {
            val listName = "TypeOfIdentificationofMeansOfTransportActive"
            val result   = pathBindable.bind("codeList", listName)
            result.value mustEqual RefDataCodeList(ListName(listName), "CL219")
          }

          "when TypeOfLocation" in {
            val listName = "TypeOfLocation"
            val result   = pathBindable.bind("codeList", listName)
            result.value mustEqual RefDataCodeList(ListName(listName), "CL347")
          }

          "when UnDangerousGoodsCode" in {
            val listName = "UnDangerousGoodsCode"
            val result   = pathBindable.bind("codeList", listName)
            result.value mustEqual RefDataCodeList(ListName(listName), "CL101")
          }

          "when Unit" in {
            val listName = "Unit"
            val result   = pathBindable.bind("codeList", listName)
            result.value mustEqual RefDataCodeList(ListName(listName), "CL349")
          }

          "when UnLocodeExtended" in {
            val listName = "UnLocodeExtended"
            val result   = pathBindable.bind("codeList", listName)
            result.value mustEqual RefDataCodeList(ListName(listName), "CL244")
          }

          "when XmlErrorCodes" in {
            val listName = "XmlErrorCodes"
            val result   = pathBindable.bind("codeList", listName)
            result.value mustEqual RefDataCodeList(ListName(listName), "CL030")
          }
        }

        "when col data code list" in {
          val listName = "CustomsOffices"

          val result = pathBindable.bind("codeList", listName)

          result.value mustEqual ColDataCodeList
        }
      }

      "must fail" - {
        "when code list doesn't exist" in {
          val listName = "foo"

          val result = pathBindable.bind("codeList", listName)

          result.left.value mustEqual s"$listName is not a valid code list name"
        }
      }
    }

    "unbind" - {
      "must unbind from path" in {
        val listName = "AdditionalReference"
        val codeList = RefDataCodeList(ListName(listName), "CL380")

        val result = pathBindable.unbind("codeList", codeList)

        result mustEqual listName
      }
    }
  }
}
