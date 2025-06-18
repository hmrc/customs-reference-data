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
import play.api.mvc.PathBindable

class ListNameSpec extends SpecBase {

  private val pathBindable = implicitly[PathBindable[ListName]]

  "must bind from path" in {
    val result = pathBindable.bind("listName", "AdditionalReference")

    result.value mustEqual ListName("AdditionalReference")
  }

  "must unbind from path" in {
    val listName = ListName("AdditionalReference")

    val result = pathBindable.unbind("listName", listName)

    result mustEqual "AdditionalReference"
  }

  "must get list code from list name" - {
    "when AdditionalInformation" in {
      val listName = ListName("AdditionalInformation")
      val result   = listName.code
      result.value mustEqual "CL239"
    }

    "when AdditionalInformationCodeSubset" in {
      val listName = ListName("AdditionalInformationCodeSubset")
      val result   = listName.code
      result.value mustEqual "CL752"
    }

    "when AdditionalReference" in {
      val listName = ListName("AdditionalReference")
      val result   = listName.code
      result.value mustEqual "CL380"
    }

    "when AdditionalSupplyChainActorRoleCode" in {
      val listName = ListName("AdditionalSupplyChainActorRoleCode")
      val result   = listName.code
      result.value mustEqual "CL704"
    }

    "when AuthorisationTypeDeparture" in {
      val listName = ListName("AuthorisationTypeDeparture")
      val result   = listName.code
      result.value mustEqual "CL235"
    }

    "when AuthorisationTypeDestination" in {
      val listName = ListName("AuthorisationTypeDestination")
      val result   = listName.code
      result.value mustEqual "CL236"
    }

    "when BusinessRejectionTypeDepExp" in {
      val listName = ListName("BusinessRejectionTypeDepExp")
      val result   = listName.code
      result.value mustEqual "CL560"
    }

    "when BusinessRejectionTypeTED2Dep" in {
      val listName = ListName("BusinessRejectionTypeTED2Dep")
      val result   = listName.code
      result.value mustEqual "CL561"
    }

    "when BusinessRejectionTypeTra" in {
      val listName = ListName("BusinessRejectionTypeTra")
      val result   = listName.code
      result.value mustEqual "CL580"
    }

    "when ControlType" in {
      val listName = ListName("ControlType")
      val result   = listName.code
      result.value mustEqual "CL716"
    }

    "when CountryAddressPostcodeBased" in {
      val listName = ListName("CountryAddressPostcodeBased")
      val result   = listName.code
      result.value mustEqual "CL190"
    }

    "when CountryAddressPostcodeOnly" in {
      val listName = ListName("CountryAddressPostcodeOnly")
      val result   = listName.code
      result.value mustEqual "CL198"
    }

    "when CountryCodesCommonTransit" in {
      val listName = ListName("CountryCodesCommonTransit")
      val result   = listName.code
      result.value mustEqual "CL009"
    }

    "when CountryCodesCommunity" in {
      val listName = ListName("CountryCodesCommunity")
      val result   = listName.code
      result.value mustEqual "CL010"
    }

    "when CountryCodesCTC" in {
      val listName = ListName("CountryCodesCTC")
      val result   = listName.code
      result.value mustEqual "CL112"
    }

    "when CountryCodesForAddress" in {
      val listName = ListName("CountryCodesForAddress")
      val result   = listName.code
      result.value mustEqual "CL248"
    }

    "when CountryCodesFullList" in {
      val listName = ListName("CountryCodesFullList")
      val result   = listName.code
      result.value mustEqual "CL008"
    }

    "when CountryCodesOptOut" in {
      val listName = ListName("CountryCodesOptOut")
      val result   = listName.code
      result.value mustEqual "CL167"
    }

    "when CountryCodesWithAddress" in {
      val listName = ListName("CountryCodesWithAddress")
      val result   = listName.code
      result.value mustEqual "CL199"
    }

    "when CountryCustomsSecurityAgreementArea" in {
      val listName = ListName("CountryCustomsSecurityAgreementArea")
      val result   = listName.code
      result.value mustEqual "CL147"
    }

    "when CountryPlaceOfLoadingNotRequired" in {
      val listName = ListName("CountryPlaceOfLoadingNotRequired")
      val result   = listName.code
      result.value mustEqual "CL289"
    }

    "when CountryWithoutZip" in {
      val listName = ListName("CountryWithoutZip")
      val result   = listName.code
      result.value mustEqual "CL505"
    }

    "when CurrencyCodes" in {
      val listName = ListName("CurrencyCodes")
      val result   = listName.code
      result.value mustEqual "CL048"
    }

    "when CUSCode" in {
      val listName = ListName("CUSCode")
      val result   = listName.code
      result.value mustEqual "CL016"
    }

    "when CustomsOfficeEnquiry" in {
      val listName = ListName("CustomsOfficeEnquiry")
      val result   = listName.code
      result.value mustEqual "CL176"
    }

    "when CustomsOffices" in {
      val listName = ListName("CustomsOffices")
      val result   = listName.code
      result.value mustEqual "CL141"
    }

    "when DeclarationType" in {
      val listName = ListName("DeclarationType")
      val result   = listName.code
      result.value mustEqual "CL231"
    }

    "when DeclarationTypeAdditional" in {
      val listName = ListName("DeclarationTypeAdditional")
      val result   = listName.code
      result.value mustEqual "CL042"
    }

    "when DeclarationTypeItemLevel" in {
      val listName = ListName("DeclarationTypeItemLevel")
      val result   = listName.code
      result.value mustEqual "CL232"
    }

    "when DeclarationTypeSecurity" in {
      val listName = ListName("DeclarationTypeSecurity")
      val result   = listName.code
      result.value mustEqual "CL217"
    }

    "when DocumentTypeExcise" in {
      val listName = ListName("DocumentTypeExcise")
      val result   = listName.code
      result.value mustEqual "CL234"
    }

    "when FunctionalErrorCodesIeCA" in {
      val listName = ListName("FunctionalErrorCodesIeCA")
      val result   = listName.code
      result.value mustEqual "CL180"
    }

    "when FunctionErrorCodesTED" in {
      val listName = ListName("FunctionErrorCodesTED")
      val result   = listName.code
      result.value mustEqual "CL437"
    }

    "when GuaranteeType" in {
      val listName = ListName("GuaranteeType")
      val result   = listName.code
      result.value mustEqual "CL251"
    }

    "when GuaranteeTypeCTC" in {
      val listName = ListName("GuaranteeTypeCTC")
      val result   = listName.code
      result.value mustEqual "CL229"
    }

    "when GuaranteeTypeEUNonTIR" in {
      val listName = ListName("GuaranteeTypeEUNonTIR")
      val result   = listName.code
      result.value mustEqual "CL230"
    }

    "when GuaranteeTypeWithGRN" in {
      val listName = ListName("GuaranteeTypeWithGRN")
      val result   = listName.code
      result.value mustEqual "CL286"
    }

    "when GuaranteeTypeWithReference" in {
      val listName = ListName("GuaranteeTypeWithReference")
      val result   = listName.code
      result.value mustEqual "CL076"
    }

    "when HScode" in {
      val listName = ListName("HScode")
      val result   = listName.code
      result.value mustEqual "CL152"
    }

    "when IncidentCode" in {
      val listName = ListName("IncidentCode")
      val result   = listName.code
      result.value mustEqual "CL019"
    }

    "when InvalidGuaranteeReason" in {
      val listName = ListName("InvalidGuaranteeReason")
      val result   = listName.code
      result.value mustEqual "CL252"
    }

    "when KindOfPackages" in {
      val listName = ListName("KindOfPackages")
      val result   = listName.code
      result.value mustEqual "CL017"
    }

    "when KindOfPackagesBulk" in {
      val listName = ListName("KindOfPackagesBulk")
      val result   = listName.code
      result.value mustEqual "CL181"
    }

    "when KindOfPackagesUnpacked" in {
      val listName = ListName("KindOfPackagesUnpacked")
      val result   = listName.code
      result.value mustEqual "CL182"
    }

    "when Nationality" in {
      val listName = ListName("Nationality")
      val result   = listName.code
      result.value mustEqual "CL165"
    }

    "when PreviousDocumentExportType" in {
      val listName = ListName("PreviousDocumentExportType")
      val result   = listName.code
      result.value mustEqual "CL228"
    }

    "when PreviousDocumentType" in {
      val listName = ListName("PreviousDocumentType")
      val result   = listName.code
      result.value mustEqual "CL214"
    }

    "when PreviousDocumentUnionGoods" in {
      val listName = ListName("PreviousDocumentUnionGoods")
      val result   = listName.code
      result.value mustEqual "CL178"
    }

    "when QualifierOfIdentificationIncident" in {
      val listName = ListName("QualifierOfIdentificationIncident")
      val result   = listName.code
      result.value mustEqual "CL038"
    }

    "when QualifierOfTheIdentification" in {
      val listName = ListName("QualifierOfTheIdentification")
      val result   = listName.code
      result.value mustEqual "CL326"
    }

    "when RejectionCodeDepartureExport" in {
      val listName = ListName("RejectionCodeDepartureExport")
      val result   = listName.code
      result.value mustEqual "CL226"
    }

    "when RejectionCodeTransit" in {
      val listName = ListName("RejectionCodeTransit")
      val result   = listName.code
      result.value mustEqual "CL581"
    }

    "when RepresentativeStatusCode" in {
      val listName = ListName("RepresentativeStatusCode")
      val result   = listName.code
      result.value mustEqual "CL094"
    }

    "when RequestedDocumentType" in {
      val listName = ListName("RequestedDocumentType")
      val result   = listName.code
      result.value mustEqual "CL215"
    }

    "when Role" in {
      val listName = ListName("Role")
      val result   = listName.code
      result.value mustEqual "CL056"
    }

    "when SpecificCircumstanceIndicatorCode" in {
      val listName = ListName("SpecificCircumstanceIndicatorCode")
      val result   = listName.code
      result.value mustEqual "CL296"
    }

    "when SupportingDocumentType" in {
      val listName = ListName("SupportingDocumentType")
      val result   = listName.code
      result.value mustEqual "CL213"
    }

    "when TransportChargesMethodOfPayment" in {
      val listName = ListName("TransportChargesMethodOfPayment")
      val result   = listName.code
      result.value mustEqual "CL116"
    }

    "when TransportDocumentType" in {
      val listName = ListName("TransportDocumentType")
      val result   = listName.code
      result.value mustEqual "CL754"
    }

    "when TransportModeCode" in {
      val listName = ListName("TransportModeCode")
      val result   = listName.code
      result.value mustEqual "CL218"
    }

    "when TypeOfIdentificationOfMeansOfTransport" in {
      val listName = ListName("TypeOfIdentificationOfMeansOfTransport")
      val result   = listName.code
      result.value mustEqual "CL750"
    }

    "when TypeOfIdentificationofMeansOfTransportActive" in {
      val listName = ListName("TypeOfIdentificationofMeansOfTransportActive")
      val result   = listName.code
      result.value mustEqual "CL219"
    }

    "when TypeOfLocation" in {
      val listName = ListName("TypeOfLocation")
      val result   = listName.code
      result.value mustEqual "CL347"
    }

    "when UnDangerousGoodsCode" in {
      val listName = ListName("UnDangerousGoodsCode")
      val result   = listName.code
      result.value mustEqual "CL101"
    }

    "when Unit" in {
      val listName = ListName("Unit")
      val result   = listName.code
      result.value mustEqual "CL349"
    }

    "when UnLocodeExtended" in {
      val listName = ListName("UnLocodeExtended")
      val result   = listName.code
      result.value mustEqual "CL244"
    }

    "when XmlErrorCodes" in {
      val listName = ListName("XmlErrorCodes")
      val result   = listName.code
      result.value mustEqual "CL030"
    }

    "when an unrecognised list name" in {
      val listName = ListName("foo")
      val result   = listName.code
      result must not be defined
    }
  }
}
