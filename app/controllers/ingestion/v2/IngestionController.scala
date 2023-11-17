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

package controllers.ingestion.v2

import actions.AuthenticateEISToken
import cats.data.EitherT
import cats.implicits._
import models._
import play.api.Logging
import play.api.libs.json.JsValue
import play.api.libs.json.Json
import play.api.mvc.Action
import play.api.mvc.BodyParser
import play.api.mvc.ControllerComponents
import play.api.mvc.PlayBodyParsers
import services.ingestion.v2.ReferenceDataService
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import javax.inject.Inject
import scala.concurrent.ExecutionContext
import scala.concurrent.Future

abstract class IngestionController @Inject() (
  cc: ControllerComponents,
  referenceDataService: ReferenceDataService,
  authenticateEISToken: AuthenticateEISToken
)(implicit ec: ExecutionContext)
    extends BackendController(cc)
    with Logging {

  def parseRequestBody(parse: PlayBodyParsers): BodyParser[JsValue]

  val schema: SimpleJsonSchemaProvider

  val source: ApiDataSource

  def post(): Action[JsValue] =
    authenticateEISToken(parseRequestBody(parse)).async {
      implicit request =>
        logger.debug(s"Headers: ${request.headers.headers.mkString}")
        (
          for {
            validate <- EitherT.fromEither[Future](referenceDataService.validate(schema, request.body))
            referenceDataPayload = ReferenceDataListsPayload(validate)
            insert <- referenceDataService.insert(source, referenceDataPayload)
          } yield insert
        ).value.map {
          case Right(_) =>
            logger.info("[controllers.ingestion.v2.IngestionController]: Success")
            Accepted
          case Left(writeError: WriteError) =>
            logger.error(s"[controllers.ingestion.v2.IngestionController]: Failed to save the data list because of error: ${writeError.message}")
            InternalServerError(Json.toJsObject(writeError))
          case Left(errorDetails: ErrorDetails) =>
            logger.error(s"[controllers.ingestion.v2.IngestionController]: Failed because of error: ${errorDetails.message}")
            BadRequest(Json.toJsObject(errorDetails))
        }
    }
}
