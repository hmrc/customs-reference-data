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
import models.testOnly.Country
import models.testOnly.CustomsOffice
import models.testOnly.Role
import play.api.Environment

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class ListRetrievalService @Inject() (
)(implicit ec: ExecutionContext, override val env: Environment, config: ResourceConfig)
    extends ResourceService {

  def getCustomsOffice: Seq[CustomsOffice] = getData[CustomsOffice](config.customsOfficeP5)

  def getCustomsOfficeWithFilter(filterParams: FilterParams): Seq[CustomsOffice] = {

    val data = getData[CustomsOffice](config.customsOfficeP5)

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

  def getCountryCodesCommonTransit: Seq[Country] = getData[Country](config.countryCodesCTC)

  def getCountryCustomsSecurityAgreementArea: Seq[Country] =
    getData[Country](config.countryCustomsOfficeSecurityAgreementArea)
}