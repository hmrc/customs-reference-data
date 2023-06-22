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

import base.SpecBase
import generators.BaseGenerators
import generators.ModelArbitraryInstances
import models.FilterParams
import models.testOnly.Country
import models.testOnly.CustomsOffice
import models.testOnly.Role
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks
import play.api.test.Helpers._

class ListRetrievalServiceSpec extends SpecBase with ModelArbitraryInstances with BaseGenerators with ScalaCheckDrivenPropertyChecks {

  "getCustomsOffice" - {

    "must return many of type CustomsOffice" in {

      running(baseApplicationBuilder) {
        application =>
          val service = application.injector.instanceOf[ListRetrievalService]

          val result = service.getCustomsOffice
          result.nonEmpty mustBe true
          result.foreach(
            co => co.isInstanceOf[CustomsOffice] mustBe true
          )
      }
    }

    "getCustomsOfficeWithFilter" - {

      "must return many of type CustomsOffice where matches filters" in {

        val filterParams1: FilterParams = FilterParams(parameters = Seq("data.countryId" -> "GB"))
        val filterParams2: FilterParams = FilterParams(parameters = Seq("data.countryId" -> "GB", "data.roles.role" -> "DEP"))

        running(baseApplicationBuilder) {
          application =>
            val service = application.injector.instanceOf[ListRetrievalService]

            val fullList: Seq[CustomsOffice]     = service.getCustomsOffice
            val country: Seq[CustomsOffice]      = service.getCustomsOfficeWithFilter(filterParams1)
            val countryRoles: Seq[CustomsOffice] = service.getCustomsOfficeWithFilter(filterParams2)

            country.nonEmpty mustBe true
            country.foreach(
              co => co.isInstanceOf[CustomsOffice] mustBe true
            )
            country.length < fullList.length mustBe true
            country.count(x => x.countryId != "GB") mustBe 0

            countryRoles.nonEmpty mustBe true
            countryRoles.foreach(
              co => co.isInstanceOf[CustomsOffice] mustBe true
            )
            countryRoles.length < fullList.length mustBe true
            countryRoles.length < country.length mustBe true
            countryRoles.count(x => !x.roles.contains(Role("DEP"))) mustBe 0
        }
      }
    }

    "getCountryCodesCommonTransit" - {

      "must return CTC country codes" in {

        running(baseApplicationBuilder) {
          application =>
            val service = application.injector.instanceOf[ListRetrievalService]

            val result = service.getCountryCodesCommonTransit
            result.nonEmpty mustBe true
            result.foreach(c => c.isInstanceOf[Country] mustBe true)
        }
      }
    }

    "getCountryCustomsSecurityAgreementArea" - {

      "must return getCountryCustomsSecurityAgreementArea country codes" in {

        running(baseApplicationBuilder) {
          application =>
            val service = application.injector.instanceOf[ListRetrievalService]

            val result = service.getCountryCustomsSecurityAgreementArea
            result.nonEmpty mustBe true
            result.foreach(c => c.isInstanceOf[Country] mustBe true)
        }
      }
    }
  }
}
