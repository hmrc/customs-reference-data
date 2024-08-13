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

package controllers.ingestion.v2.testOnly

import models.ApiDataSource
import models.ApiDataSource.ColDataFeed
import models.ApiDataSource.RefDataFeed
import play.api.libs.json.Json.JsValueWrapper
import play.api.libs.json.JsArray
import play.api.libs.json.Json

import scala.xml.MetaData
import scala.xml.Node

sealed trait CodeList {

  val name: String

  val source: ApiDataSource

  def fields(entry: Node): Seq[Seq[(String, Option[JsValueWrapper])]] =
    Seq(
      Seq(
        "state"      -> Some((entry \\ "state").text),
        "activeFrom" -> Some((entry \\ "activeFrom").text)
      )
    )

  protected def getAttribute(entry: Node, key: String)(f: MetaData => Boolean): Option[JsValueWrapper] =
    (entry \\ key).find(x => f(x.attributes)).map(_.text)

  protected def getDataItem(entry: Node, key: String): Option[JsValueWrapper] =
    getAttribute(entry, "dataItem")(_("name").map(_.text).contains(key))
}

object CodeList {

  sealed trait SingleFieldCodeList extends CodeList {
    val code: String = "code"

    override def fields(entry: Node): Seq[Seq[(String, Option[JsValueWrapper])]] =
      super.fields(entry).map {
        _ :+ code -> Some((entry \ "dataItem").text)
      }
  }

  sealed trait StandardCodeList extends SingleFieldCodeList {
    val description: String = "description"

    override def fields(entry: Node): Seq[Seq[(String, Option[JsValueWrapper])]] =
      super.fields(entry).map {
        _ :+ description -> getAttribute(entry, "description")(_("lang").map(_.text).contains("en"))
      }
  }

  case class AdditionalInformation(name: String) extends StandardCodeList {
    override val source: ApiDataSource = RefDataFeed
  }

  case class AdditionalReference(name: String) extends StandardCodeList {
    override val code: String          = "documentType"
    override val source: ApiDataSource = RefDataFeed
  }

  case class AdditionalSupplyChainActorRoleCode(name: String) extends StandardCodeList {
    override val code: String          = "role"
    override val source: ApiDataSource = RefDataFeed
  }

  case class AuthorisationTypeDeparture(name: String) extends StandardCodeList {
    override val source: ApiDataSource = RefDataFeed
  }

  case class AuthorisationTypeDestination(name: String) extends StandardCodeList {
    override val source: ApiDataSource = RefDataFeed
  }

  case class BusinessRejectionTypeDepExp(name: String) extends StandardCodeList {
    override val source: ApiDataSource = RefDataFeed
  }

  case class CUSCode(name: String) extends SingleFieldCodeList {
    override val source: ApiDataSource = RefDataFeed
  }

  case class ControlType(name: String) extends StandardCodeList {
    override val source: ApiDataSource = RefDataFeed
  }

  case class CountryAddressPostcodeBased(name: String) extends StandardCodeList {
    override val source: ApiDataSource = RefDataFeed
  }

  case class CountryAddressPostcodeOnly(name: String) extends StandardCodeList {
    override val source: ApiDataSource = RefDataFeed
  }

  case class CountryCodesCTC(name: String) extends StandardCodeList {
    override val source: ApiDataSource = RefDataFeed
  }

  case class CountryCodesCommonTransit(name: String) extends StandardCodeList {
    override val source: ApiDataSource = RefDataFeed
  }

  case class CountryCodesCommunity(name: String) extends StandardCodeList {
    override val source: ApiDataSource = RefDataFeed
  }

  case class CountryCodesForAddress(name: String) extends StandardCodeList {
    override val source: ApiDataSource = RefDataFeed
  }

  case class CountryCodesFullList(name: String) extends StandardCodeList {
    override val source: ApiDataSource = RefDataFeed
  }

  case class CountryCustomsSecurityAgreementArea(name: String) extends StandardCodeList {
    override val source: ApiDataSource = RefDataFeed
  }

  case class CountryWithoutZip(name: String) extends SingleFieldCodeList {
    override val source: ApiDataSource = RefDataFeed
  }

  case class CurrencyCodes(name: String) extends StandardCodeList {
    override val code: String          = "currency"
    override val source: ApiDataSource = RefDataFeed
  }

  case class CustomsOffices(name: String) extends CodeList {

    override val source: ApiDataSource = ColDataFeed

    override def fields(entry: Node): Seq[Seq[(String, Option[JsValueWrapper])]] = {
      val offices = (entry \ "dataGroup").filter(_.attributes("name").map(_.text).contains("CustomsOfficeLsd"))
      offices.foldLeft[Seq[Seq[(String, Option[JsValueWrapper])]]](Nil) {
        case (acc, office) =>
          acc ++ super.fields(entry).map {
            _ ++ Seq(
              "languageCode" -> getDataItem(office, "LanguageCode"),
              "name"         -> getDataItem(office, "CustomsOfficeUsualName"),
              "phoneNumber"  -> getDataItem(entry, "PhoneNumber"),
              "id"           -> getDataItem(entry, "ReferenceNumber"),
              "countryId"    -> getDataItem(entry, "CountryCode"),
              "roles" -> {
                val nodes = (entry \\ "dataItem").filter(_.attributes("name").map(_.text).contains("Role"))
                val array = nodes.foldLeft(JsArray()) {
                  case (acc, node) =>
                    JsArray(acc.value :+ Json.obj("role" -> node.text))
                }
                Some(array)
              }
            )
          }
      }
    }
  }

  case class DeclarationType(name: String) extends StandardCodeList {
    override val source: ApiDataSource = RefDataFeed
  }

  case class DeclarationTypeAdditional(name: String) extends StandardCodeList {
    override val source: ApiDataSource = RefDataFeed
  }

  case class DeclarationTypeItemLevel(name: String) extends StandardCodeList {
    override val source: ApiDataSource = RefDataFeed
  }

  case class DeclarationTypeSecurity(name: String) extends StandardCodeList {
    override val source: ApiDataSource = RefDataFeed
  }

  case class DocumentTypeExcise(name: String) extends StandardCodeList {
    override val source: ApiDataSource = RefDataFeed
  }

  case class FunctionalErrorCodesIeCA(name: String) extends StandardCodeList {
    override val source: ApiDataSource = RefDataFeed
  }

  case class GuaranteeType(name: String) extends StandardCodeList {
    override val source: ApiDataSource = RefDataFeed
  }

  case class GuaranteeTypeWithGRN(name: String) extends StandardCodeList {
    override val source: ApiDataSource = RefDataFeed
  }

  case class GuaranteeTypeEUNonTIR(name: String) extends StandardCodeList {
    override val source: ApiDataSource = RefDataFeed
  }

  case class GuaranteeTypeCTC(name: String) extends StandardCodeList {
    override val source: ApiDataSource = RefDataFeed
  }

  case class GuaranteeTypeWithReference(name: String) extends StandardCodeList {
    override val source: ApiDataSource = RefDataFeed
  }

  case class HScode(name: String) extends SingleFieldCodeList {
    override val source: ApiDataSource = RefDataFeed
  }

  case class IncidentCode(name: String) extends StandardCodeList {
    override val source: ApiDataSource = RefDataFeed
  }

  case class InvalidGuaranteeReason(name: String) extends StandardCodeList {
    override val source: ApiDataSource = RefDataFeed
  }

  case class KindOfPackages(name: String) extends StandardCodeList {
    override val source: ApiDataSource = RefDataFeed
  }

  case class KindOfPackagesBulk(name: String) extends StandardCodeList {
    override val source: ApiDataSource = RefDataFeed
  }

  case class KindOfPackagesUnpacked(name: String) extends StandardCodeList {
    override val source: ApiDataSource = RefDataFeed
  }

  case class Nationality(name: String) extends StandardCodeList {
    override val source: ApiDataSource = RefDataFeed
  }

  case class PreviousDocumentExportType(name: String) extends StandardCodeList {
    override val source: ApiDataSource = RefDataFeed
  }

  case class PreviousDocumentType(name: String) extends StandardCodeList {
    override val source: ApiDataSource = RefDataFeed
  }

  case class PreviousDocumentUnionGoods(name: String) extends StandardCodeList {
    override val source: ApiDataSource = RefDataFeed
  }

  case class QualifierOfIdentificationIncident(name: String) extends StandardCodeList {
    override val code: String          = "qualifier"
    override val source: ApiDataSource = RefDataFeed
  }

  case class QualifierOfTheIdentification(name: String) extends StandardCodeList {
    override val code: String          = "qualifier"
    override val source: ApiDataSource = RefDataFeed
  }

  case class RejectionCodeDepartureExport(name: String) extends StandardCodeList {
    override val source: ApiDataSource = RefDataFeed
  }

  case class RepresentativeStatusCode(name: String) extends StandardCodeList {
    override val source: ApiDataSource = RefDataFeed
  }

  case class RequestedDocumentType(name: String) extends StandardCodeList {
    override val source: ApiDataSource = RefDataFeed
  }

  case class SpecificCircumstanceIndicatorCode(name: String) extends StandardCodeList {
    override val source: ApiDataSource = RefDataFeed
  }

  case class SupportingDocumentType(name: String) extends StandardCodeList {
    override val source: ApiDataSource = RefDataFeed
  }

  case class TransportChargesMethodOfPayment(name: String) extends StandardCodeList {
    override val code: String          = "method"
    override val source: ApiDataSource = RefDataFeed
  }

  case class TransportDocumentType(name: String) extends StandardCodeList {
    override val source: ApiDataSource = RefDataFeed
  }

  case class TransportModeCode(name: String) extends StandardCodeList {
    override val source: ApiDataSource = RefDataFeed
  }

  case class TypeOfIdentificationOfMeansOfTransport(name: String) extends StandardCodeList {
    override val code: String          = "type"
    override val source: ApiDataSource = RefDataFeed
  }

  case class TypeOfIdentificationOfMeansOfTransportActive(name: String) extends StandardCodeList {
    override val source: ApiDataSource = RefDataFeed
  }

  case class TypeOfLocation(name: String) extends StandardCodeList {
    override val code: String          = "type"
    override val source: ApiDataSource = RefDataFeed
  }

  case class UnDangerousGoodsCode(name: String) extends StandardCodeList {
    override val source: ApiDataSource = RefDataFeed
  }

  case class UnLocodeExtended(name: String) extends CodeList {

    override val source: ApiDataSource = RefDataFeed

    override def fields(entry: Node): Seq[Seq[(String, Option[JsValueWrapper])]] =
      super.fields(entry).map {
        _ ++ Seq(
          "unLocodeExtendedCode" -> getDataItem(entry, "UnLocodeExtendedCode"),
          "name"                 -> getDataItem(entry, "Name")
        )
      }
  }

  case class Unit(name: String) extends StandardCodeList {
    override val source: ApiDataSource = RefDataFeed
  }

  def apply(name: String): Option[CodeList] =
    name match {
      case "AdditionalInformation"                        => Some(AdditionalInformation("AdditionalInformation"))
      case "AdditionalReference"                          => Some(AdditionalReference("AdditionalReference"))
      case "AdditionalSupplyChainActorRoleCode"           => Some(AdditionalSupplyChainActorRoleCode("AdditionalSupplyChainActorRoleCode"))
      case "AuthorisationTypeDeparture"                   => Some(AuthorisationTypeDeparture("AuthorisationTypeDeparture"))
      case "AuthorisationTypeDestination"                 => Some(AuthorisationTypeDestination("AuthorisationTypeDestination"))
      case "BusinessRejectionTypeDepExp"                  => Some(BusinessRejectionTypeDepExp("BusinessRejectionTypeDepExp"))
      case "CUSCode"                                      => Some(CUSCode("CUSCode"))
      case "ControlType"                                  => Some(ControlType("ControlType"))
      case "CountryAddressPostcodeBased"                  => Some(CountryAddressPostcodeBased("CountryAddressPostcodeBased"))
      case "CountryAddressPostcodeOnly"                   => Some(CountryAddressPostcodeOnly("CountryAddressPostcodeOnly"))
      case "CountryCodesCTC"                              => Some(CountryCodesCTC("CountryCodesCTC"))
      case "CountryCodesCommonTransit"                    => Some(CountryCodesCommonTransit("CountryCodesCommonTransit"))
      case "CountryCodesCommunity"                        => Some(CountryCodesCommunity("CountryCodesCommunity"))
      case "CountryCodesForAddress"                       => Some(CountryCodesForAddress("CountryCodesForAddress"))
      case "CountryCodesFullList"                         => Some(CountryCodesFullList("CountryCodesFullList"))
      case "CountryCustomsSecurityAgreementArea"          => Some(CountryCustomsSecurityAgreementArea("CountryCustomsSecurityAgreementArea"))
      case "CountryWithoutZip"                            => Some(CountryWithoutZip("CountryWithoutZip"))
      case "CurrencyCodes"                                => Some(CurrencyCodes("CurrencyCodes"))
      case "CustomsOffices"                               => Some(CustomsOffices("customsOffices"))
      case "DeclarationType"                              => Some(DeclarationType("DeclarationType"))
      case "DeclarationTypeAdditional"                    => Some(DeclarationTypeAdditional("DeclarationTypeAdditional"))
      case "DeclarationTypeItemLevel"                     => Some(DeclarationTypeItemLevel("DeclarationTypeItemLevel"))
      case "DeclarationTypeSecurity"                      => Some(DeclarationTypeSecurity("DeclarationTypeSecurity"))
      case "DocumentTypeExcise"                           => Some(DocumentTypeExcise("DocumentTypeExcise"))
      case "FunctionalErrorCodesIeCA"                     => Some(FunctionalErrorCodesIeCA("FunctionalErrorCodesIeCA"))
      case "GuaranteeType"                                => Some(GuaranteeType("GuaranteeType"))
      case "GuaranteeTypeWithGRN"                         => Some(GuaranteeTypeWithGRN("GuaranteeTypeWithGRN"))
      case "GuaranteeTypeEUNonTIR"                        => Some(GuaranteeTypeEUNonTIR("GuaranteeTypeEUNonTIR"))
      case "GuaranteeTypeCTC"                             => Some(GuaranteeTypeCTC("GuaranteeTypeCTC"))
      case "GuaranteeTypeWithReference"                   => Some(GuaranteeTypeWithReference("GuaranteeTypeWithReference"))
      case "HScode"                                       => Some(HScode("HScode"))
      case "IncidentCode"                                 => Some(IncidentCode("IncidentCode"))
      case "InvalidGuaranteeReason"                       => Some(InvalidGuaranteeReason("InvalidGuaranteeReason"))
      case "KindOfPackages"                               => Some(KindOfPackages("KindOfPackages"))
      case "KindOfPackagesBulk"                           => Some(KindOfPackagesBulk("KindOfPackagesBulk"))
      case "KindOfPackagesUnpacked"                       => Some(KindOfPackagesUnpacked("KindOfPackagesUnpacked"))
      case "Nationality"                                  => Some(Nationality("Nationality"))
      case "PreviousDocumentExportType"                   => Some(PreviousDocumentExportType("PreviousDocumentExportType"))
      case "PreviousDocumentType"                         => Some(PreviousDocumentType("PreviousDocumentType"))
      case "PreviousDocumentUnionGoods"                   => Some(PreviousDocumentUnionGoods("PreviousDocumentUnionGoods"))
      case "QualifierOfIdentificationIncident"            => Some(QualifierOfIdentificationIncident("QualifierOfIdentificationIncident"))
      case "QualifierOfTheIdentification"                 => Some(QualifierOfTheIdentification("QualifierOfTheIdentification"))
      case "RejectionCodeDepartureExport"                 => Some(RejectionCodeDepartureExport("RejectionCodeDepartureExport"))
      case "RepresentativeStatusCode"                     => Some(RepresentativeStatusCode("RepresentativeStatusCode"))
      case "RequestedDocumentType"                        => Some(RequestedDocumentType("RequestedDocumentType"))
      case "SpecificCircumstanceIndicatorCode"            => Some(SpecificCircumstanceIndicatorCode("SpecificCircumstanceIndicatorCode"))
      case "SupportingDocumentType"                       => Some(SupportingDocumentType("SupportingDocumentType"))
      case "TransportChargesMethodOfPayment"              => Some(TransportChargesMethodOfPayment("TransportChargesMethodOfPayment"))
      case "TransportDocumentType"                        => Some(TransportDocumentType("TransportDocumentType"))
      case "TransportModeCode"                            => Some(TransportModeCode("TransportModeCode"))
      case "TypeOfIdentificationOfMeansOfTransport"       => Some(TypeOfIdentificationOfMeansOfTransport("TypeOfIdentificationOfMeansOfTransport"))
      case "TypeOfIdentificationofMeansOfTransportActive" => Some(TypeOfIdentificationOfMeansOfTransportActive("TypeOfIdentificationOfMeansOfTransportActive"))
      case "TypeOfLocation"                               => Some(TypeOfLocation("TypeOfLocation"))
      case "UnDangerousGoodsCode"                         => Some(UnDangerousGoodsCode("UnDangerousGoodsCode"))
      case "UnLocodeExtended"                             => Some(UnLocodeExtended("UnLocodeExtended"))
      case "Unit"                                         => Some(Unit("Unit"))
      case _                                              => None
    }
}
