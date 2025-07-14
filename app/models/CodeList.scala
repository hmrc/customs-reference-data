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

package models

import play.api.mvc.PathBindable

import scala.util.Try

sealed trait CodeList {
  val listName: ListName
}

object CodeList {

  def apply(value: String): CodeList =
    value match {
      case "CustomsOffices" =>
        ColDataCodeList
      case _ =>
        listNames
          .get(value)
          .map(RefDataCodeList(ListName(value), _))
          .getOrElse(throw IllegalArgumentException(s"$value is not a valid code list name"))
    }

  case class RefDataCodeList(listName: ListName, code: String) extends CodeList

  case object ColDataCodeList extends CodeList {
    override val listName: ListName = ListName("CustomsOffices")
  }

  implicit lazy val pathBindable: PathBindable[CodeList] = new PathBindable[CodeList] {

    override def bind(key: String, value: String): Either[String, CodeList] =
      Try(CodeList(value)).toEither.left.map(_.getMessage)

    override def unbind(key: String, value: CodeList): String =
      value.listName.toString
  }

  val listNames: Map[String, String] = Map(
    "AdditionalInformation"                        -> "CL239",
    "AdditionalInformationCodeSubset"              -> "CL752",
    "AdditionalReference"                          -> "CL380",
    "AdditionalSupplyChainActorRoleCode"           -> "CL704",
    "AuthorisationTypeDeparture"                   -> "CL235",
    "AuthorisationTypeDestination"                 -> "CL236",
    "BusinessRejectionTypeDepExp"                  -> "CL560",
    "BusinessRejectionTypeTED2Dep"                 -> "CL561",
    "BusinessRejectionTypeTra"                     -> "CL580",
    "ControlType"                                  -> "CL716",
    "CountryAddressPostcodeBased"                  -> "CL190",
    "CountryAddressPostcodeOnly"                   -> "CL198",
    "CountryCodesCommonTransit"                    -> "CL009",
    "CountryCodesCommunity"                        -> "CL010",
    "CountryCodesCTC"                              -> "CL112",
    "CountryCodesForAddress"                       -> "CL248",
    "CountryCodesFullList"                         -> "CL008",
    "CountryCodesOptOut"                           -> "CL167",
    "CountryCodesWithAddress"                      -> "CL199",
    "CountryCustomsSecurityAgreementArea"          -> "CL147",
    "CountryPlaceOfLoadingNotRequired"             -> "CL289",
    "CountryWithoutZip"                            -> "CL505",
    "CurrencyCodes"                                -> "CL048",
    "CUSCode"                                      -> "CL016",
    "DeclarationType"                              -> "CL231",
    "DeclarationTypeAdditional"                    -> "CL042",
    "DeclarationTypeItemLevel"                     -> "CL232",
    "DeclarationTypeSecurity"                      -> "CL217",
    "DocumentTypeExcise"                           -> "CL234",
    "FunctionalErrorCodesIeCA"                     -> "CL180",
    "FunctionErrorCodesTED"                        -> "CL437",
    "GuaranteeType"                                -> "CL251",
    "GuaranteeTypeCTC"                             -> "CL229",
    "GuaranteeTypeEUNonTIR"                        -> "CL230",
    "GuaranteeTypeWithGRN"                         -> "CL286",
    "GuaranteeTypeWithReference"                   -> "CL076",
    "HScode"                                       -> "CL152",
    "IncidentCode"                                 -> "CL019",
    "InvalidGuaranteeReason"                       -> "CL252",
    "KindOfPackages"                               -> "CL017",
    "KindOfPackagesBulk"                           -> "CL181",
    "KindOfPackagesUnpacked"                       -> "CL182",
    "Nationality"                                  -> "CL165",
    "PreviousDocumentExportType"                   -> "CL228",
    "PreviousDocumentType"                         -> "CL214",
    "PreviousDocumentUnionGoods"                   -> "CL178",
    "QualifierOfIdentificationIncident"            -> "CL038",
    "QualifierOfTheIdentification"                 -> "CL326",
    "RejectionCodeDepartureExport"                 -> "CL226",
    "RejectionCodeTransit"                         -> "CL581",
    "RepresentativeStatusCode"                     -> "CL094",
    "RequestedDocumentType"                        -> "CL215",
    "Role"                                         -> "CL056",
    "SpecificCircumstanceIndicatorCode"            -> "CL296",
    "SupportingDocumentType"                       -> "CL213",
    "TransportChargesMethodOfPayment"              -> "CL116",
    "TransportDocumentType"                        -> "CL754",
    "TransportModeCode"                            -> "CL218",
    "TypeOfIdentificationOfMeansOfTransport"       -> "CL750",
    "TypeOfIdentificationofMeansOfTransportActive" -> "CL219",
    "TypeOfLocation"                               -> "CL347",
    "UnDangerousGoodsCode"                         -> "CL101",
    "Unit"                                         -> "CL349",
    "UnLocodeExtended"                             -> "CL244",
    "XmlErrorCodes"                                -> "CL030"
  )
}
