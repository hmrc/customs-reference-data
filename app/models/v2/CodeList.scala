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

package models.v2

import models.ApiDataSource
import models.ApiDataSource.ColDataFeed
import models.ApiDataSource.RefDataFeed
import play.api.libs.json.JsArray
import play.api.libs.json.JsObject
import play.api.libs.json.Json
import play.api.libs.json.Json.JsValueWrapper

import scala.xml.MetaData
import scala.xml.Node

sealed trait CodeList {

  val name: String

  val source: ApiDataSource

  def json(entry: Node): Seq[JsObject] =
    fields(entry)
      .map {
        _.flatMap {
          case (key, Some(value)) => Some((key, value))
          case _                  => None
        }
      }
      .map {
        fields => Json.obj(fields: _*)
      }

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
    val codeKey: String = "code"
    val codeName: String

    override def fields(entry: Node): Seq[Seq[(String, Option[JsValueWrapper])]] =
      super.fields(entry).map {
        _ :+ codeKey -> getDataItem(entry, codeName)
      }
  }

  sealed trait StandardCodeList extends SingleFieldCodeList {
    val descriptionKey: String = "description"

    override def fields(entry: Node): Seq[Seq[(String, Option[JsValueWrapper])]] =
      super.fields(entry).map {
        _ :+ descriptionKey -> getAttribute(entry, "description")(_("lang").map(_.text).contains("en"))
      }
  }

  case class AdditionalInformation(name: String) extends StandardCodeList {
    override val codeName: String      = "AdditionalInformationCode"
    override val source: ApiDataSource = RefDataFeed
  }

  case class AdditionalReference(name: String) extends StandardCodeList {
    override val codeKey: String       = "documentType"
    override val codeName: String      = "DocumentType"
    override val source: ApiDataSource = RefDataFeed
  }

  case class AdditionalSupplyChainActorRoleCode(name: String) extends StandardCodeList {
    override val codeKey: String       = "role"
    override val codeName: String      = "Role"
    override val source: ApiDataSource = RefDataFeed
  }

  case class AuthorisationTypeDeparture(name: String) extends StandardCodeList {
    override val codeName: String      = "AuthorisationType"
    override val source: ApiDataSource = RefDataFeed
  }

  case class AuthorisationTypeDestination(name: String) extends StandardCodeList {
    override val codeName: String      = "AuthorisationType"
    override val source: ApiDataSource = RefDataFeed
  }

  case class BusinessRejectionTypeDepExp(name: String) extends StandardCodeList {
    override val codeName: String      = "BusinessRejectionTypeDepExpCode"
    override val source: ApiDataSource = RefDataFeed
  }

  case class CUSCode(name: String) extends SingleFieldCodeList {
    override val codeName: String      = "CUSCode"
    override val source: ApiDataSource = RefDataFeed
  }

  case class ControlType(name: String) extends StandardCodeList {
    override val codeName: String      = "Code"
    override val source: ApiDataSource = RefDataFeed
  }

  case class CountryAddressPostcodeBased(name: String) extends StandardCodeList {
    override val codeName: String      = "CountryCode"
    override val source: ApiDataSource = RefDataFeed
  }

  case class CountryAddressPostcodeOnly(name: String) extends StandardCodeList {
    override val codeName: String      = "CountryCode"
    override val source: ApiDataSource = RefDataFeed
  }

  case class CountryCodesCTC(name: String) extends StandardCodeList {
    override val codeName: String      = "CountryCode"
    override val source: ApiDataSource = RefDataFeed
  }

  case class CountryCodesCommonTransit(name: String) extends StandardCodeList {
    override val codeName: String      = "CountryCode"
    override val source: ApiDataSource = RefDataFeed
  }

  case class CountryCodesCommunity(name: String) extends StandardCodeList {
    override val codeName: String      = "CountryCode"
    override val source: ApiDataSource = RefDataFeed
  }

  case class CountryCodesForAddress(name: String) extends StandardCodeList {
    override val codeName: String      = "CountryCode"
    override val source: ApiDataSource = RefDataFeed
  }

  case class CountryCodesFullList(name: String) extends StandardCodeList {
    override val codeName: String      = "CountryCode"
    override val source: ApiDataSource = RefDataFeed
  }

  case class CountryCustomsSecurityAgreementArea(name: String) extends StandardCodeList {
    override val codeName: String      = "CountryCode"
    override val source: ApiDataSource = RefDataFeed
  }

  case class CountryWithoutZip(name: String) extends SingleFieldCodeList {
    override val codeName: String      = "CountryCode"
    override val source: ApiDataSource = RefDataFeed
  }

  case class CurrencyCodes(name: String) extends StandardCodeList {
    override val codeKey: String       = "currency"
    override val codeName: String      = "Currency"
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
                val array = nodes.map(_.text).distinct.foldLeft(JsArray()) {
                  case (acc, node) =>
                    JsArray(acc.value :+ Json.obj("role" -> node))
                }
                Some(array)
              }
            )
          }
      }
    }
  }

  case class DeclarationType(name: String) extends StandardCodeList {
    override val codeName: String      = "DeclarationTypeCode"
    override val source: ApiDataSource = RefDataFeed
  }

  case class DeclarationTypeAdditional(name: String) extends StandardCodeList {
    override val codeName: String      = "Code"
    override val source: ApiDataSource = RefDataFeed
  }

  case class DeclarationTypeItemLevel(name: String) extends StandardCodeList {
    override val codeName: String      = "DeclarationTypeCode"
    override val source: ApiDataSource = RefDataFeed
  }

  case class DeclarationTypeSecurity(name: String) extends StandardCodeList {
    override val codeName: String      = "DeclarationTypeSecurityCode"
    override val source: ApiDataSource = RefDataFeed
  }

  case class DocumentTypeExcise(name: String) extends StandardCodeList {
    override val codeName: String      = "PreviousDocumentTypeCode"
    override val source: ApiDataSource = RefDataFeed
  }

  case class FunctionalErrorCodesIeCA(name: String) extends StandardCodeList {
    override val codeName: String      = "Code"
    override val source: ApiDataSource = RefDataFeed
  }

  case class GuaranteeType(name: String) extends StandardCodeList {
    override val codeName: String      = "GuaranteeTypeCode"
    override val source: ApiDataSource = RefDataFeed
  }

  case class GuaranteeTypeWithGRN(name: String) extends StandardCodeList {
    override val codeName: String      = "GuaranteeTypeCode"
    override val source: ApiDataSource = RefDataFeed
  }

  case class GuaranteeTypeEUNonTIR(name: String) extends StandardCodeList {
    override val codeName: String      = "GuaranteeTypeCode"
    override val source: ApiDataSource = RefDataFeed
  }

  case class GuaranteeTypeCTC(name: String) extends StandardCodeList {
    override val codeName: String      = "GuaranteeTypeCode"
    override val source: ApiDataSource = RefDataFeed
  }

  case class GuaranteeTypeWithReference(name: String) extends StandardCodeList {
    override val codeName: String      = "GuaranteeTypeCode"
    override val source: ApiDataSource = RefDataFeed
  }

  case class HScode(name: String) extends SingleFieldCodeList {
    override val codeName: String      = "Code"
    override val source: ApiDataSource = RefDataFeed
  }

  case class IncidentCode(name: String) extends StandardCodeList {
    override val codeName: String      = "Code"
    override val source: ApiDataSource = RefDataFeed
  }

  case class InvalidGuaranteeReason(name: String) extends StandardCodeList {
    override val codeName: String      = "InvalidGuaranteeReasonCode"
    override val source: ApiDataSource = RefDataFeed
  }

  case class KindOfPackages(name: String) extends StandardCodeList {
    override val codeName: String      = "KindOfPackages"
    override val source: ApiDataSource = RefDataFeed
  }

  case class KindOfPackagesBulk(name: String) extends StandardCodeList {
    override val codeName: String      = "KindOfPackages"
    override val source: ApiDataSource = RefDataFeed
  }

  case class KindOfPackagesUnpacked(name: String) extends StandardCodeList {
    override val codeName: String      = "KindOfPackages"
    override val source: ApiDataSource = RefDataFeed
  }

  case class Nationality(name: String) extends StandardCodeList {
    override val codeName: String      = "CountryCode"
    override val source: ApiDataSource = RefDataFeed
  }

  case class PreviousDocumentExportType(name: String) extends StandardCodeList {
    override val codeName: String      = "PreviousDocumentTypeCode"
    override val source: ApiDataSource = RefDataFeed
  }

  case class PreviousDocumentType(name: String) extends StandardCodeList {
    override val codeName: String      = "PreviousDocumentTypeCode"
    override val source: ApiDataSource = RefDataFeed
  }

  case class PreviousDocumentUnionGoods(name: String) extends StandardCodeList {
    override val codeName: String      = "PreviousDocumentTypeCode"
    override val source: ApiDataSource = RefDataFeed
  }

  case class QualifierOfIdentificationIncident(name: String) extends StandardCodeList {
    override val codeKey: String       = "qualifier"
    override val codeName: String      = "QualifierOfTheIdentification"
    override val source: ApiDataSource = RefDataFeed
  }

  case class QualifierOfTheIdentification(name: String) extends StandardCodeList {
    override val codeKey: String       = "qualifier"
    override val codeName: String      = "QualifierOfTheIdentification"
    override val source: ApiDataSource = RefDataFeed
  }

  case class RejectionCodeDepartureExport(name: String) extends StandardCodeList {
    override val codeName: String      = "RejectionDepartureExportCode"
    override val source: ApiDataSource = RefDataFeed
  }

  case class RepresentativeStatusCode(name: String) extends StandardCodeList {
    override val codeName: String      = "RepresentativeStatusCode"
    override val source: ApiDataSource = RefDataFeed
  }

  case class RequestedDocumentType(name: String) extends StandardCodeList {
    override val codeName: String      = "DocumentType"
    override val source: ApiDataSource = RefDataFeed
  }

  case class SpecificCircumstanceIndicatorCode(name: String) extends StandardCodeList {
    override val codeName: String      = "Code"
    override val source: ApiDataSource = RefDataFeed
  }

  case class SupportingDocumentType(name: String) extends StandardCodeList {
    override val codeName: String      = "SupportingDocumentCode"
    override val source: ApiDataSource = RefDataFeed
  }

  case class TransportChargesMethodOfPayment(name: String) extends StandardCodeList {
    override val codeKey: String       = "method"
    override val codeName: String      = "TransportChargesMethodOfPayment"
    override val source: ApiDataSource = RefDataFeed
  }

  case class TransportDocumentType(name: String) extends StandardCodeList {
    override val codeName: String      = "Type"
    override val source: ApiDataSource = RefDataFeed
  }

  case class TransportModeCode(name: String) extends StandardCodeList {
    override val codeName: String      = "TransportModeCode"
    override val source: ApiDataSource = RefDataFeed
  }

  case class TypeOfIdentificationOfMeansOfTransport(name: String) extends StandardCodeList {
    override val codeKey: String       = "type"
    override val codeName: String      = "TypeOfIdentification"
    override val source: ApiDataSource = RefDataFeed
  }

  case class TypeOfIdentificationOfMeansOfTransportActive(name: String) extends StandardCodeList {
    override val codeName: String      = "TypeOfIdentificationofMeansOfTransportActiveCode"
    override val source: ApiDataSource = RefDataFeed
  }

  case class TypeOfLocation(name: String) extends StandardCodeList {
    override val codeKey: String       = "type"
    override val codeName: String      = "TypeOfLocation"
    override val source: ApiDataSource = RefDataFeed
  }

  case class UnDangerousGoodsCode(name: String) extends StandardCodeList {
    override val codeName: String      = "UnDangerousGoodsCode"
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
    override val codeName: String      = "Unit"
    override val source: ApiDataSource = RefDataFeed
  }

  def apply(name: String): Option[CodeList] =
    name match {
      case "AdditionalInformation"                        => Some(AdditionalInformation(name))
      case "AdditionalReference"                          => Some(AdditionalReference(name))
      case "AdditionalSupplyChainActorRoleCode"           => Some(AdditionalSupplyChainActorRoleCode(name))
      case "AuthorisationTypeDeparture"                   => Some(AuthorisationTypeDeparture(name))
      case "AuthorisationTypeDestination"                 => Some(AuthorisationTypeDestination(name))
      case "BusinessRejectionTypeDepExp"                  => Some(BusinessRejectionTypeDepExp(name))
      case "CUSCode"                                      => Some(CUSCode(name))
      case "ControlType"                                  => Some(ControlType(name))
      case "CountryAddressPostcodeBased"                  => Some(CountryAddressPostcodeBased(name))
      case "CountryAddressPostcodeOnly"                   => Some(CountryAddressPostcodeOnly(name))
      case "CountryCodesCTC"                              => Some(CountryCodesCTC(name))
      case "CountryCodesCommonTransit"                    => Some(CountryCodesCommonTransit(name))
      case "CountryCodesCommunity"                        => Some(CountryCodesCommunity(name))
      case "CountryCodesForAddress"                       => Some(CountryCodesForAddress(name))
      case "CountryCodesFullList"                         => Some(CountryCodesFullList(name))
      case "CountryCustomsSecurityAgreementArea"          => Some(CountryCustomsSecurityAgreementArea(name))
      case "CountryWithoutZip"                            => Some(CountryWithoutZip(name))
      case "CurrencyCodes"                                => Some(CurrencyCodes(name))
      case "CustomsOffices"                               => Some(CustomsOffices("customsOffices"))
      case "DeclarationType"                              => Some(DeclarationType(name))
      case "DeclarationTypeAdditional"                    => Some(DeclarationTypeAdditional(name))
      case "DeclarationTypeItemLevel"                     => Some(DeclarationTypeItemLevel(name))
      case "DeclarationTypeSecurity"                      => Some(DeclarationTypeSecurity(name))
      case "DocumentTypeExcise"                           => Some(DocumentTypeExcise(name))
      case "FunctionalErrorCodesIeCA"                     => Some(FunctionalErrorCodesIeCA(name))
      case "GuaranteeType"                                => Some(GuaranteeType(name))
      case "GuaranteeTypeWithGRN"                         => Some(GuaranteeTypeWithGRN(name))
      case "GuaranteeTypeEUNonTIR"                        => Some(GuaranteeTypeEUNonTIR(name))
      case "GuaranteeTypeCTC"                             => Some(GuaranteeTypeCTC(name))
      case "GuaranteeTypeWithReference"                   => Some(GuaranteeTypeWithReference(name))
      case "HScode"                                       => Some(HScode(name))
      case "IncidentCode"                                 => Some(IncidentCode(name))
      case "InvalidGuaranteeReason"                       => Some(InvalidGuaranteeReason(name))
      case "KindOfPackages"                               => Some(KindOfPackages(name))
      case "KindOfPackagesBulk"                           => Some(KindOfPackagesBulk(name))
      case "KindOfPackagesUnpacked"                       => Some(KindOfPackagesUnpacked(name))
      case "Nationality"                                  => Some(Nationality(name))
      case "PreviousDocumentExportType"                   => Some(PreviousDocumentExportType(name))
      case "PreviousDocumentType"                         => Some(PreviousDocumentType(name))
      case "PreviousDocumentUnionGoods"                   => Some(PreviousDocumentUnionGoods(name))
      case "QualifierOfIdentificationIncident"            => Some(QualifierOfIdentificationIncident(name))
      case "QualifierOfTheIdentification"                 => Some(QualifierOfTheIdentification(name))
      case "RejectionCodeDepartureExport"                 => Some(RejectionCodeDepartureExport(name))
      case "RepresentativeStatusCode"                     => Some(RepresentativeStatusCode(name))
      case "RequestedDocumentType"                        => Some(RequestedDocumentType(name))
      case "SpecificCircumstanceIndicatorCode"            => Some(SpecificCircumstanceIndicatorCode(name))
      case "SupportingDocumentType"                       => Some(SupportingDocumentType(name))
      case "TransportChargesMethodOfPayment"              => Some(TransportChargesMethodOfPayment(name))
      case "TransportDocumentType"                        => Some(TransportDocumentType(name))
      case "TransportModeCode"                            => Some(TransportModeCode(name))
      case "TypeOfIdentificationOfMeansOfTransport"       => Some(TypeOfIdentificationOfMeansOfTransport(name))
      case "TypeOfIdentificationofMeansOfTransportActive" => Some(TypeOfIdentificationOfMeansOfTransportActive(name))
      case "TypeOfLocation"                               => Some(TypeOfLocation(name))
      case "UnDangerousGoodsCode"                         => Some(UnDangerousGoodsCode(name))
      case "UnLocodeExtended"                             => Some(UnLocodeExtended(name))
      case "Unit"                                         => Some(Unit(name))
      case _                                              => None
    }
}
