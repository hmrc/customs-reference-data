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
import play.api.Logging
import play.api.libs.json.Json
import play.api.mvc.Action
import play.api.mvc.AnyContent
import play.api.mvc.ControllerComponents
import services.consumption.testOnly.ListRetrievalService
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import javax.inject.Inject
import scala.util.Failure
import scala.util.Success

class ListRetrievalController @Inject() (
  cc: ControllerComponents,
  listRetrievalService: ListRetrievalService
) extends BackendController(cc)
    with Logging {

  def get(listName: ListName, filterParams: Option[FilterParams]): Action[AnyContent] =
    Action {
      listRetrievalService.get(listName.listName, filterParams) match {
        case Success(json) =>
          Ok(Json.obj("data" -> json))
        case Failure(exception) =>
          logger.error(exception.getMessage)
          NotFound
      }
    }

  @deprecated("Use `get` instead", since = "0.110.0")
  def getFiltered(listName: ListName, filterParams: FilterParams): Action[AnyContent] =
    get(listName, Some(filterParams))
}
