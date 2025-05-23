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

import config.ReferenceDataControllerParserConfig
import controllers.actions.{AuthenticateEISToken, LogHeaders, ValidateAcceptHeader}
import models.ApiDataSource
import models.ApiDataSource.ColDataFeed
import models.CTCUP08Schema
import play.api.libs.json.JsValue
import play.api.mvc.BodyParser
import play.api.mvc.ControllerComponents
import play.api.mvc.PlayBodyParsers
import services.ingestion.ReferenceDataService

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class CustomsOfficeListController @Inject() (
  cc: ControllerComponents,
  referenceDataService: ReferenceDataService,
  parseConfig: ReferenceDataControllerParserConfig,
  override val schema: CTCUP08Schema,
  logHeaders: LogHeaders,
  authenticateEISToken: AuthenticateEISToken,
  validateAcceptHeader: ValidateAcceptHeader
)(implicit ec: ExecutionContext)
    extends IngestionController(cc, referenceDataService, logHeaders, authenticateEISToken, validateAcceptHeader) {

  override def parseRequestBody(parse: PlayBodyParsers): BodyParser[JsValue] = parseConfig.customsOfficeParser(parse)

  override val source: ApiDataSource = ColDataFeed
}
