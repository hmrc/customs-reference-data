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
    Action.async {
      listName.listName match {
        case "CustomsOffices" =>
          Future.successful(
            Ok(Json.obj("data" -> Json.toJson(listRetrievalService.getCustomsOffice)))
          )
        case "CountryCodesCommonTransit" =>
          Future.successful(
            Ok(Json.obj("data" -> Json.toJson(listRetrievalService.getCountryCodesCommonTransit)))
          )
        case "CountryCustomsSecurityAgreementArea" =>
          Future.successful(
            Ok(Json.obj("data" -> Json.toJson(listRetrievalService.getCountryCustomsSecurityAgreementArea)))
          )
        case _ => Future.successful(NotFound)
      }
    }

  def getFiltered(listName: ListName, filterParams: FilterParams): Action[AnyContent] =
    Action.async {
      listName.listName match {
        case "CustomsOffices" =>
          Future.successful(
            Ok(
              Json.obj(
                "data" -> Json.toJson(
                  listRetrievalService.getCustomsOfficeWithFilter(filterParams)
                )
              )
            )
          )
        case _ => Future.successful(NotFound)
      }
    }
}