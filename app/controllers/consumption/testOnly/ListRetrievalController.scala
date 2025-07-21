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

import controllers.actions.VersionedAction
import models.Phase.{Phase5, Phase6}
import models.{CodeList, FilterParams}
import play.api.Logging
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import services.consumption.testOnly.ListRetrievalService
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import javax.inject.Inject
import scala.util.{Failure, Success}

class ListRetrievalController @Inject() (
  cc: ControllerComponents,
  listRetrievalService: ListRetrievalService,
  versionedAction: VersionedAction
) extends BackendController(cc)
    with Logging {

  def get(codeList: CodeList, filterParams: Option[FilterParams]): Action[AnyContent] =
    versionedAction {
      implicit request =>
        listRetrievalService.get(codeList, request.phase, filterParams) match {
          case Success(json) =>
            Ok {
              request.phase match {
                case Phase5 => Json.obj("data" -> json)
                case Phase6 => json
              }
            }
          case Failure(exception) =>
            logger.error(exception.getMessage)
            NotFound
        }
    }

}
