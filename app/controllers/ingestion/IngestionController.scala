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
import cats.implicits._
import models._
import play.api.Logging
import play.api.libs.json.JsValue
import play.api.libs.json.Json
import play.api.mvc.Action
import play.api.mvc.BodyParser
import play.api.mvc.ControllerComponents
import play.api.mvc.PlayBodyParsers
import services.ingestion.ReferenceDataService
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import javax.inject.Inject
import scala.concurrent.ExecutionContext
import scala.concurrent.Future

abstract class IngestionController @Inject() (
  cc: ControllerComponents,
  referenceDataService: ReferenceDataService
)(implicit ec: ExecutionContext)
    extends BackendController(cc)
    with Logging {

  def parseRequestBody(parse: PlayBodyParsers): BodyParser[JsValue]

  val schema: SimpleJsonSchemaProvider

  val source: ApiDataSource

  def post(): Action[JsValue] =
    Action(parseRequestBody(parse)).async {
      implicit request =>
        (
          for {
            validate <- EitherT.fromEither[Future](referenceDataService.validate(schema, request.body))
            referenceDataPayload = ReferenceDataListsPayload(validate)
            insert <- EitherT.fromOptionF(referenceDataService.insert(source, referenceDataPayload), ()).swap
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
