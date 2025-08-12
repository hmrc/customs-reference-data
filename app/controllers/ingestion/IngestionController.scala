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
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{Action, BodyParser, ControllerComponents, PlayBodyParsers}
import services.ingestion.{ReferenceDataService, SchemaValidationService}
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import scala.concurrent.{ExecutionContext, Future}

abstract class IngestionController(
  cc: ControllerComponents,
  referenceDataService: ReferenceDataService,
  schemaValidationService: SchemaValidationService,
  logHeaders: LogHeaders,
  authenticateEISToken: AuthenticateEISToken,
  validateAcceptHeader: ValidateAcceptHeader
)(implicit ec: ExecutionContext)
    extends BackendController(cc)
    with Logging {

  def parseRequestBody(parse: PlayBodyParsers): BodyParser[JsValue]

  val schemaProvider: JsonSchemaProvider

  val source: ApiDataSource

  def post(): Action[JsValue] =
    (logHeaders andThen authenticateEISToken andThen validateAcceptHeader).async(parseRequestBody(parse)) {
      implicit request =>
        (
          for {
            validate <- EitherT.fromEither[Future](schemaValidationService.validate(schemaProvider.getSchema, request.body))
            referenceDataPayload = ReferenceDataListsPayload(validate)
            insert <- EitherT(referenceDataService.insert(source, referenceDataPayload))
          } yield insert
        ).value.map {
          case Right(_) =>
            logger.info("[controllers.ingestion.IngestionController]: Success")
            Accepted
          case Left(error: BadRequestError) =>
            val response = Json.toJson(error)
            logger.error(s"[controllers.ingestion.IngestionController]: Failed because of error: ${Json.stringify(response)}")
            BadRequest(response)
          case Left(error) =>
            val response = Json.toJson(error)
            logger.error(s"[controllers.ingestion.IngestionController]: Failed to save the data list because of error: ${Json.stringify(response)}")
            InternalServerError(response)
        }
    }
}
