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

package controllers.ingestion.v1

import config.ReferenceDataControllerParserConfig
import models.ApiDataSource.RefDataFeed
import models.ApiDataSource
import models.SimpleJsonSchemaProvider
import models.v1.CTCUP06Schema
import play.api.libs.json.JsValue
import play.api.mvc.BodyParser
import play.api.mvc.ControllerComponents
import play.api.mvc.PlayBodyParsers
import services.ingestion.v1.ReferenceDataService

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class ReferenceDataListController @Inject() (
  cc: ControllerComponents,
  referenceDataService: ReferenceDataService,
  parseConfig: ReferenceDataControllerParserConfig,
  cTCUP06Schema: CTCUP06Schema
)(implicit ec: ExecutionContext)
    extends IngestionController(cc, referenceDataService) {

  override def parseRequestBody(parse: PlayBodyParsers): BodyParser[JsValue] = parseConfig.referenceDataParser(parse)

  override val schema: SimpleJsonSchemaProvider =
    cTCUP06Schema

  override val source: ApiDataSource = RefDataFeed
}
