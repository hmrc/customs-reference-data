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

import controllers.ingestion.v2.CommonIngestionController
import play.api.mvc.Action
import play.api.mvc.ControllerComponents
import services.ingestion.v2.ReferenceDataService
import utils.XmlToJsonConverter

import scala.concurrent.ExecutionContext
import scala.xml.NodeSeq

abstract class TestOnlyIngestionController[T <: XmlToJsonConverter](
  cc: ControllerComponents,
  referenceDataService: ReferenceDataService,
  converter: T
)(implicit ec: ExecutionContext)
    extends CommonIngestionController(cc, referenceDataService) {

  def post(): Action[NodeSeq] =
    Action(parse.xml).async {
      request =>
        val json = converter.convert(request.body)
        validateAndSave(json)
    }
}
