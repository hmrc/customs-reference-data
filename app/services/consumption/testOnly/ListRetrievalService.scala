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

import models.*
import play.api.libs.json.*

import javax.inject.Inject
import scala.util.Try

class ListRetrievalService @Inject() (resourceService: ResourceService) {

  def get(codeList: CodeList, filterParams: Option[FilterParams]): Try[JsArray] = {
    filterParams match {
      case None =>
        resourceService.getJson(codeList.listName)
      case Some(filterParams) =>
        resourceService.getJson(codeList.listName).map {
          json =>
            val filteredValues = json.value.filter {
              value =>
                filterParams.parameters.forall {
                  case (filterParamKey, filterParamValues) =>
                    val nodes = filterParamKey.split("\\.").tail // removes "data" from path nodes
                    val values = nodes.tail.foldLeft(value \\ nodes.head) {
                      case (acc, node) => acc.flatMap(_ \\ node)
                    }
                    values.exists(filterParamValues.map(JsString.apply).contains(_))
                }
            }
            JsArray(filteredValues)
        }
    }
  }.map(_.unescapeXml)

}
