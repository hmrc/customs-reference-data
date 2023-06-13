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

package controllers.consumption.v2

import cats.data.OptionT
import cats.implicits._
import models.ListName
import models.MetaData
import models.v2.StreamReferenceData
import play.api.http.HttpEntity
import play.api.mvc.Action
import play.api.mvc.AnyContent
import play.api.mvc.ControllerComponents
import services.consumption.v2.ListRetrievalService
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class ListRetrievalController @Inject() (
  cc: ControllerComponents,
  listRetrievalService: ListRetrievalService
)(implicit ec: ExecutionContext)
    extends BackendController(cc) {

  def get(listName: ListName): Action[AnyContent] =
    Action.async {
      (
        for {
          latestVersion <- OptionT(listRetrievalService.getLatestVersion(listName))
          streamedList = listRetrievalService.getStreamedList(listName, latestVersion.versionId)
          nestJson     = StreamReferenceData(listName, MetaData(latestVersion))
        } yield streamedList.via(nestJson.nestInJson)
      ).value.map {
        case Some(source) => Ok.sendEntity(HttpEntity.Streamed(source, None, Some("application/json")))
        case None         => NotFound
      }
    }
}
