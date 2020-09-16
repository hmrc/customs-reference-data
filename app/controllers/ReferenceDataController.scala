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

package controllers

import javax.inject.Inject
import models.CTCUP06Schema
import models.CTCUP08Schema
import models.ReferenceDataPayload
import models.ResponseErrorMessage
import models.ResponseErrorType.OtherError
import play.api.Logger
import play.api.libs.json.JsValue
import play.api.libs.json.Json
import play.api.mvc.Action
import play.api.mvc.ControllerComponents
import play.api.mvc.RawBuffer
import services.ReferenceDataService
import services.SchemaValidationService
import services.ReferenceDataService.DataProcessingResult.DataProcessingFailed
import services.ReferenceDataService.DataProcessingResult.DataProcessingSuccessful
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import scala.concurrent.ExecutionContext
import scala.concurrent.Future

class ReferenceDataController @Inject() (
  cc: ControllerComponents,
  referenceDataService: ReferenceDataService,
  schemaValidationService: SchemaValidationService,
  cTCUP06Schema: CTCUP06Schema,
  cTCUP08Schema: CTCUP08Schema
)(implicit ec: ExecutionContext)
    extends BackendController(cc) {

  private val referenceDataListsLogger = Logger("ReferenceDataLists")
  private val customsOfficeListsLogger = Logger("CustomsOfficeLists")

  def referenceDataLists(): Action[RawBuffer] =
    Action(parse.raw(maxLength = 1024 * 400)).async {
      implicit request =>
        schemaValidationService
          .validate(cTCUP06Schema, request.body.asBytes().get)
          .map(ReferenceDataPayload(_))
          .fold(
            error => {
              referenceDataListsLogger.error(Json.toJsObject(error).toString())
              Future.successful(BadRequest(Json.toJsObject(error)))
            },
            refData =>
              referenceDataService
                .insert(refData)
                .map {
                  case DataProcessingSuccessful => Accepted
                  case DataProcessingFailed =>
                    referenceDataListsLogger.error("Failed to save the data list because of internal error")
                    InternalServerError(Json.toJsObject(ResponseErrorMessage(OtherError, None)))
                }
          )
    }

  def customsOfficeLists(): Action[JsValue] = ???

}
