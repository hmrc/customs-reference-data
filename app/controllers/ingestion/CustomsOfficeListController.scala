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

import config.ReferenceDataControllerParserConfig
import javax.inject.Inject
import models._
import play.api.Logger
import play.api.libs.json.Json
import play.api.mvc.Action
import play.api.mvc.ControllerComponents
import play.api.mvc.RawBuffer
import services.ingestion.ReferenceDataService
import services.ingestion.ReferenceDataService.DataProcessingResult.DataProcessingFailed
import services.ingestion.ReferenceDataService.DataProcessingResult.DataProcessingSuccessful
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import scala.concurrent.ExecutionContext
import scala.concurrent.Future

class CustomsOfficeListController @Inject() (
  cc: ControllerComponents,
  referenceDataService: ReferenceDataService,
  parseConfig: ReferenceDataControllerParserConfig,
  cTCUP08Schema: CTCUP08Schema
)(implicit ec: ExecutionContext)
    extends BackendController(cc) {

  import parseConfig._

  private val customsOfficeListsLogger = Logger("CustomsOfficeLists")

  def customsOfficeLists(): Action[RawBuffer] =
    Action(customsOfficeParser(parse)).async {
      implicit request =>
        val requestBody = request.body.asBytes().map(_.toArray) match {
          case Some(body) => referenceDataService.validateAndDecompress(cTCUP08Schema, body)
          case _          => Left(OtherError("Payload larger than memory threshold"))
        }

        requestBody match {
          case Right(jsObject) =>
            referenceDataService
              .insert(ReferenceDataListsPayload(jsObject))
              .map {
                case DataProcessingSuccessful => Accepted
                case DataProcessingFailed =>
                  customsOfficeListsLogger.error("Failed to save the data list because of internal error")
                  InternalServerError(Json.toJsObject(OtherError("Failed in processing the data list")))
              }
          case Left(error) =>
            customsOfficeListsLogger.error(Json.toJsObject(error).toString())
            Future.successful(BadRequest(Json.toJsObject(error)))
        }
    }
}
