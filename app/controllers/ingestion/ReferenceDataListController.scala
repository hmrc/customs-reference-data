/*
 * Copyright 2021 HM Revenue & Customs
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

import cats.data._
import cats.implicits._
import config.ReferenceDataControllerParserConfig
import javax.inject.Inject
import logging.Logging
import models.ApiDataSource.RefDataFeed
import models.CTCUP06Schema
import models.ErrorDetails
import models.ReferenceDataListsPayload
import models.WriteError
import play.api.libs.json.JsValue
import play.api.libs.json.Json
import play.api.mvc.Action
import play.api.mvc.ControllerComponents
import services.ingestion.ReferenceDataService
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import scala.concurrent.ExecutionContext
import scala.concurrent.Future

class ReferenceDataListController @Inject() (
  cc: ControllerComponents,
  referenceDataService: ReferenceDataService,
  parseConfig: ReferenceDataControllerParserConfig,
  cTCUP06Schema: CTCUP06Schema
)(implicit ec: ExecutionContext)
    extends BackendController(cc)
    with Logging {

  import parseConfig._

  def referenceDataLists(): Action[JsValue] =
    Action(referenceDataParser(parse)).async {
      implicit request =>
        (
          for {
            validate <- EitherT.fromEither[Future](referenceDataService.validate(cTCUP06Schema, request.body))
            referenceDataPayload = ReferenceDataListsPayload(validate)
            insert <- EitherT.fromOptionF(referenceDataService.insert(RefDataFeed, referenceDataPayload), ()).swap
          } yield insert
        ).value.map {
          case Right(_) => Accepted
          case Left(writeError: WriteError) =>
            logger.info(s"Failed to save the data list because of error: ${writeError.message}")
            InternalServerError(Json.toJsObject(writeError))
          case Left(errorDetails: ErrorDetails) =>
            logger.info(errorDetails.message)
            BadRequest(Json.toJsObject(errorDetails))
        }
    }
}
