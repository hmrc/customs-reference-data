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

import connectors.CrdlCacheConnector
import controllers.actions.VersionedAction
import models.Phase.{Phase5, Phase6}
import models.{CodeList, FilterParams}
import play.api.Logging
import play.api.http.HttpEntity
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import services.consumption.testOnly.ListRetrievalService
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

class ListRetrievalController @Inject() (
  cc: ControllerComponents,
  versionedAction: VersionedAction,
  listRetrievalService: ListRetrievalService,
  crdlConnector: CrdlCacheConnector
)(implicit ec: ExecutionContext)
    extends BackendController(cc)
    with Logging {

  def get(codeList: CodeList, filter: Option[FilterParams]): Action[AnyContent] =
    versionedAction.async {
      implicit request =>
        request.phase match {
          case Phase5 =>
            Future.successful {
              listRetrievalService.get(codeList, filter) match {
                case Success(json) =>
                  Ok(Json.obj("data" -> json))
                case Failure(exception) =>
                  logger.error(exception.getMessage)
                  NotFound
              }
            }
          case Phase6 =>
            crdlConnector.get(codeList, filter.getOrElse(FilterParams())).map {
              source => Ok.sendEntity(HttpEntity.Streamed(source, None, Some("application/json")))
            }
        }
    }

}
