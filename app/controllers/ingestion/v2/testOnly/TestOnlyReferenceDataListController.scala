/*
 * Copyright 2024 HM Revenue & Customs
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

package controllers.ingestion.v2.testOnly

import controllers.ingestion.v2.testOnly.XmlToJsonConverter.ReferenceDataListXmlToJsonConverter
import models.ApiDataSource
import models.ApiDataSource.RefDataFeed
import models.v2.CTCUP06Schema
import play.api.mvc.Action
import play.api.mvc.ControllerComponents
import services.ingestion.v2.ReferenceDataService

import javax.inject.Inject
import scala.concurrent.ExecutionContext
import scala.xml.NodeSeq

class TestOnlyReferenceDataListController @Inject() (
  cc: ControllerComponents,
  referenceDataService: ReferenceDataService,
  override val schema: CTCUP06Schema,
  converter: ReferenceDataListXmlToJsonConverter
)(implicit ec: ExecutionContext)
    extends IngestionController[ReferenceDataListXmlToJsonConverter](cc, referenceDataService, converter) {

  /**
    * Download all domains from https://ec.europa.eu/taxation_customs/dds2/rd/rd_download_home.jsp?Lang=en
    * Unzip the download, cd into it and run `gzip RD_NCTS-P5.xml`
    * Attach this to the request body as a binary
    */
  override def post(): Action[NodeSeq] = super.post()

  override val source: ApiDataSource = RefDataFeed
}
