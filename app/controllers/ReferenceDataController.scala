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
import models.ReferenceDataPayload
import play.api.libs.json.JsObject
import play.api.libs.json.JsValue
import play.api.mvc.Action
import play.api.mvc.ControllerComponents
import services.ReferenceDataService
import services.ReferenceDataService.DataProcessingResult.DataProcessingFailed
import services.ReferenceDataService.DataProcessingResult.DataProcessingSuccessful
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import scala.concurrent.ExecutionContext

class ReferenceDataController @Inject() (cc: ControllerComponents, referenceDataService: ReferenceDataService)(implicit ec: ExecutionContext)
    extends BackendController(cc) {

  def post: Action[JsValue] =
    Action(parse.json).async {
      implicit request =>
        val refData = ReferenceDataPayload(request.body.as[JsObject])
        referenceDataService
          .insert(refData)
          .map {
            case DataProcessingSuccessful => Accepted
            case DataProcessingFailed =>
              InternalServerError
          }
    }

}
