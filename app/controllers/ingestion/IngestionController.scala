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

package controllers.ingestion

import cats.data.EitherT
import controllers.actions.{AuthenticateEISToken, LogHeaders, ValidateAcceptHeader}
import models.*
import play.api.Logging
import play.api.libs.json.JsValue
import play.api.libs.json.Json
import play.api.mvc.Action
import play.api.mvc.BodyParser
import play.api.mvc.ControllerComponents
import play.api.mvc.PlayBodyParsers
import services.ingestion.ReferenceDataService
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import scala.concurrent.ExecutionContext
import scala.concurrent.Future

abstract class IngestionController(
  cc: ControllerComponents,
  referenceDataService: ReferenceDataService,
  logHeaders: LogHeaders,
  authenticateEISToken: AuthenticateEISToken,
  validateAcceptHeader: ValidateAcceptHeader
)(implicit ec: ExecutionContext)
    extends BackendController(cc)
    with Logging {

  def parseRequestBody(parse: PlayBodyParsers): BodyParser[JsValue]

  val schema: SimpleJsonSchemaProvider

  val source: ApiDataSource

  def post(): Action[JsValue] =
    (logHeaders andThen authenticateEISToken andThen validateAcceptHeader).async(parseRequestBody(parse)) {
      implicit request =>
        (
          for {
            validate <- EitherT.fromEither[Future](referenceDataService.validate(schema, request.body))
            referenceDataPayload = ReferenceDataListsPayload(validate)
            insert <- EitherT(referenceDataService.insert(source, referenceDataPayload))
          } yield insert
        ).value.map {
          case Right(_) =>
            logger.info("[controllers.ingestion.IngestionController]: Success")
            Accepted
          case Left(writeError: MongoError) =>
            val response = Json.toJson(writeError)
            logger.error(s"[controllers.ingestion.IngestionController]: Failed to save the data list because of error: ${Json.stringify(response)}")
            InternalServerError(response)
          case Left(errorDetails: ErrorDetails) =>
            val response = Json.toJson(errorDetails)
            logger.error(s"[controllers.ingestion.IngestionController]: Failed because of error: ${Json.stringify(response)}")
            BadRequest(response)
        }
    }
}
