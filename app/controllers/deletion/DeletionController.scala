/*
 * Copyright 2022 HM Revenue & Customs
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

package controllers.deletion

import play.api.mvc.Action
import play.api.mvc.AnyContent
import play.api.mvc.ControllerComponents
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import javax.inject.Inject
import scala.concurrent.ExecutionContext
import scala.concurrent.Future

class DeletionController @Inject() (
  cc: ControllerComponents
)(implicit ec: ExecutionContext)
    extends BackendController(cc) {

  def delete(): Action[AnyContent] =
    Action.async {
      // get all distinct list names from versions collection
      // find latest version id for each of these list names
      // delete any documents from versions and reference-data-lists that do not include one of these version ids
      Future.successful(Ok)
    }
}
