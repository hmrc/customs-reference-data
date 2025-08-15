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

package services.consumption.testOnly

import models.ListName
import play.api.Environment
import play.api.libs.json.{JsArray, Json}

import javax.inject.Inject
import scala.io.Source
import scala.util.{Failure, Try}

class ResourceService @Inject() (env: Environment) {

  def getJson(listName: ListName): Try[JsArray] =
    env
      .resourceAsStream(s"resources/phase-5/$listName.json")
      .map {
        inputStream =>
          val rawData = Source.fromInputStream(inputStream).mkString
          Json.parse(rawData)
      }
      .map(_.validate[JsArray].asTry)
      .getOrElse(Failure(new Exception(s"Could not find code list $listName")))
}
