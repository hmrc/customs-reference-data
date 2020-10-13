/*
 * Copyright 2020 HM Revenue & Customs
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

package controllers.ingestion

import akka.util.ByteString
import config.ReferenceDataControllerParserConfig
import javax.inject.Inject
import models.ApiDataSource.RefDataFeed
import models.CTCUP06Schema
import models.OtherError
import models.ReferenceDataListsPayload
import play.api.Logger
import play.api.libs.json.Json
import play.api.mvc.Action
import play.api.mvc.ControllerComponents
import services.ingestion.ReferenceDataService
import services.ingestion.ReferenceDataService.DataProcessingResult.DataProcessingFailed
import services.ingestion.ReferenceDataService.DataProcessingResult.DataProcessingSuccessful
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import scala.concurrent.ExecutionContext
import scala.concurrent.Future

class ReferenceDataListController @Inject() (
  cc: ControllerComponents,
  referenceDataService: ReferenceDataService,
  parseConfig: ReferenceDataControllerParserConfig,
  cTCUP06Schema: CTCUP06Schema
)(implicit ec: ExecutionContext)
    extends BackendController(cc) {

  import parseConfig._

  private val referenceDataListsLogger = Logger("ReferenceDataLists")

  def referenceDataLists(): Action[ByteString] =
    Action(referenceDataParser(parse)).async {
      implicit request =>
        referenceDataService.validateAndDecompress(cTCUP06Schema, request.body.toArray) match {
          case Right(jsObject) =>
            referenceDataService
              .insert(RefDataFeed, ReferenceDataListsPayload(jsObject))
              .map {
                case DataProcessingSuccessful => Accepted
                case DataProcessingFailed =>
                  referenceDataListsLogger.error("Failed to save the data list because of internal error")
                  InternalServerError(Json.toJsObject(OtherError("Failed in processing the data list")))
              }
          case Left(error) =>
            referenceDataListsLogger.error(Json.toJsObject(error).toString())
            Future.successful(BadRequest(Json.toJsObject(error)))
        }
    }
}
