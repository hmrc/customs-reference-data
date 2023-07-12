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

import models._
import models.testOnly._
import play.api.libs.json._

import javax.inject.Inject
import scala.util.Try

class ListRetrievalService @Inject() (resourceService: ResourceService)(implicit config: ResourceConfig) {

  def get(codeList: String): Try[JsValue] = resourceService.getJson(codeList)

  def getCustomsOfficesWithFilter(filterParams: FilterParams): Seq[CustomsOffice] = {

    val data = resourceService.getData[CustomsOffice](config.customsOffices)

    val country = filterParams.parameters.toMap.get("data.countryId")
    val role    = filterParams.parameters.toMap.get("data.roles.role")

    data.filter(
      x =>
        (country, role) match {
          case (Some(country), Some(role)) => x.countryId == country && x.roles.contains(Role(role))
          case (Some(country), _)          => x.countryId == country
          case (_, Some(role))             => x.roles.contains(Role(role))
          case _                           => true
        }
    )
  }

  def getCountryCodesWithFilter(filterParams: FilterParams): Seq[Country] = {

    val data = resourceService.getData[Country](config.countryCodesFullList)

    val countryCode = filterParams.parameters.toMap.get("data.code")

    data.filter(
      x =>
        countryCode match {
          case Some(code) => x.code == code
          case _          => true
        }
    )
  }
}
