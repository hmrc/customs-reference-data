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

package controllers.consumption

import cats.data.OptionT
import cats.implicits.*
import connectors.CrdlCacheConnector
import controllers.actions.VersionedAction
import models.Phase.*
import models.{CodeList, FilterParams, MetaData, StreamReferenceData}
import play.api.http.HttpEntity
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import services.consumption.ListRetrievalService
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class ListRetrievalController @Inject() (
  cc: ControllerComponents,
  versionedAction: VersionedAction,
  listRetrievalService: ListRetrievalService,
  crdlConnector: CrdlCacheConnector
)(implicit ec: ExecutionContext)
    extends BackendController(cc) {

  def get(codeList: CodeList, filter: Option[FilterParams]): Action[AnyContent] =
    versionedAction.async {
      implicit request =>
        val listName = codeList.listName
        request.phase match {
          case Phase5 =>
            (
              for {
                latestVersion <- OptionT(listRetrievalService.getLatestVersion(listName))
                streamedList = listRetrievalService.getStreamedList(listName, latestVersion.versionId, filter)
                nestJson     = StreamReferenceData(codeList, MetaData(latestVersion))
              } yield streamedList.via(nestJson.nestInJson(filter))
            ).value.map {
              case Some(source) => Ok.sendEntity(HttpEntity.Streamed(source, None, Some("application/json")))
              case None         => NotFound
            }
          case Phase6 =>
            crdlConnector.get(codeList, filter.getOrElse(FilterParams())).map {
              source => Ok.sendEntity(HttpEntity.Streamed(source, None, Some("application/json")))
            }
        }
    }

}
