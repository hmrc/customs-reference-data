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

package config

import javax.inject.Inject
import javax.inject.Singleton
import play.api.Configuration
import play.api.libs.json.JsValue
import play.api.mvc.BodyParser
import play.api.mvc.PlayBodyParsers

@Singleton
class ReferenceDataControllerParserConfig @Inject() (config: Configuration)() {

  private val referenceDataLists =
    config.getOptional[Long]("controllers.controllers.ingestion.ReferenceDataController.referenceDataLists.maxLength")

  def referenceDataParser(parse: PlayBodyParsers): BodyParser[JsValue] =
    referenceDataLists.fold(parse.json)(
      maxLength => parse.json(maxLength = maxLength)
    )

  private val customsOfficeLists =
    config.getOptional[Long]("controllers.controllers.ingestion.ReferenceDataController.customsOfficeLists.maxLength")

  def customsOfficeParser(parse: PlayBodyParsers): BodyParser[JsValue] =
    customsOfficeLists.fold(parse.json)(
      maxLength => parse.json(maxLength = maxLength)
    )

}
