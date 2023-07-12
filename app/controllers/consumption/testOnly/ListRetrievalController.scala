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

package controllers.consumption.testOnly

import models.FilterParams
import models.ListName
import play.api.libs.json.Json
import play.api.mvc.Action
import play.api.mvc.AnyContent
import play.api.mvc.ControllerComponents
import services.consumption.testOnly.ListRetrievalService
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import javax.inject.Inject
import scala.concurrent.ExecutionContext
import scala.concurrent.Future

class ListRetrievalController @Inject() (
  cc: ControllerComponents,
  listRetrievalService: ListRetrievalService
)(implicit ec: ExecutionContext)
    extends BackendController(cc) {

  def get(listName: ListName): Action[AnyContent] =
    Action {
      listName.listName match {
        case "CustomsOffices" =>
          Ok(Json.obj("data" -> Json.toJson(listRetrievalService.getCustomsOffice)))
        case "CountryCodesFullList" =>
          Ok(Json.obj("data" -> Json.toJson(listRetrievalService.getCountryCodesFullList)))
        case "CountryCodesCommonTransit" =>
          Ok(Json.obj("data" -> Json.toJson(listRetrievalService.getCountryCodesCommonTransit)))
        case "CountryCodesCTC" =>
          Ok(Json.obj("data" -> Json.toJson(listRetrievalService.getCountryCodesCTC)))
        case "CountryCodesCommunity" =>
          Ok(Json.obj("data" -> Json.toJson(listRetrievalService.getCountryCodesCommunity)))
        case "CountryCodesForAddress" =>
          Ok(Json.obj("data" -> Json.toJson(listRetrievalService.getCountryCodesForAddress)))
        case "CountryCustomsSecurityAgreementArea" =>
          Ok(Json.obj("data" -> Json.toJson(listRetrievalService.getCountryCustomsSecurityAgreementArea)))
        case "CountryAddressPostcodeBased" =>
          Ok(Json.obj("data" -> Json.toJson(listRetrievalService.getCountryAddressPostcodeBased)))
        case "CountryWithoutZip" =>
          Ok(Json.obj("data" -> Json.toJson(listRetrievalService.getCountryWithoutZip)))
        case "UnLocodeExtended" =>
          Ok(Json.obj("data" -> Json.toJson(listRetrievalService.getUnLocodeExtended)))
        case "Nationality" =>
          Ok(Json.obj("data" -> Json.toJson(listRetrievalService.getNationality)))
        case "PreviousDocumentType" =>
          Ok(Json.obj("data" -> Json.toJson(listRetrievalService.getPreviousDocumentType)))
        case "SupportingDocumentType" =>
          Ok(Json.obj("data" -> Json.toJson(listRetrievalService.getSupportingDocumentType)))
        case "TransportDocumentType" =>
          Ok(Json.obj("data" -> Json.toJson(listRetrievalService.getTransportDocumentType)))
        case "KindOfPackages" =>
          Ok(Json.obj("data" -> Json.toJson(listRetrievalService.getKindOfPackages)))
        case "KindOfPackagesBulk" =>
          Ok(Json.obj("data" -> Json.toJson(listRetrievalService.getKindOfPackagesBulk)))
        case "KindOfPackagesUnpacked" =>
          Ok(Json.obj("data" -> Json.toJson(listRetrievalService.getKindOfPackagesUnpacked)))
        case "AdditionalReference" =>
          Ok(Json.obj("data" -> Json.toJson(listRetrievalService.getAdditionalReference)))
        case "AdditionalInformation" =>
          Ok(Json.obj("data" -> Json.toJson(listRetrievalService.getAdditionalInformation)))
        case "Unit" =>
          Ok(Json.obj("data" -> Json.toJson(listRetrievalService.getUnit)))
        case "CurrencyCodes" =>
          Ok(Json.obj("data" -> Json.toJson(listRetrievalService.getCurrencyCodes)))
        case "ControlType" =>
          Ok(Json.obj("data" -> Json.toJson(listRetrievalService.getControlType)))
        case _ => NotFound
      }
    }

  def getFiltered(listName: ListName, filterParams: FilterParams): Action[AnyContent] =
    Action {
      listName.listName match {
        case "CustomsOffices" =>
          Ok(Json.obj("data" -> Json.toJson(listRetrievalService.getCustomsOfficeWithFilter(filterParams))))
        case "CountryCodesFullList" =>
          Ok(Json.obj("data" -> Json.toJson(listRetrievalService.getCountryCodesWithFilter(filterParams))))
        case _ => NotFound
      }
    }
}
