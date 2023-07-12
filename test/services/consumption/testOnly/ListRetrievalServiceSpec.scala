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
import models.FilterParams
import models.testOnly.CustomsOffice
import models.testOnly.Role
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.Helpers._

class ListRetrievalServiceSpec extends SpecBase with ScalaCheckPropertyChecks {

  private val codeListGen = Gen.oneOf(
    "CustomsOffices",
    "CountryCodesCTC",
    "CountryCodesCommonTransit",
    "CountryCustomsSecurityAgreementArea",
    "CountryAddressPostcodeBased",
    "CountryWithoutZip",
    "UnLocodeExtended",
    "CountryCodesFullList",
    "CountryCodesCommunity",
    "CountryCodesForAddress",
    "Nationality",
    "PreviousDocumentType",
    "SupportingDocumentType",
    "TransportDocumentType",
    "KindOfPackages",
    "KindOfPackagesBulk",
    "KindOfPackagesUnpacked",
    "AdditionalReference",
    "AdditionalInformation",
    "Unit",
    "CurrencyCodes",
    "ControlType",
    "SpecificCircumstanceIndicatorCode"
  )

  "get" - {
    "should succeed" - {
      "when code list found" in {
        running(baseApplicationBuilder) {
          application =>
            val service = application.injector.instanceOf[ListRetrievalService]
            forAll(codeListGen) {
              codeList =>
                val result = service.get(codeList)
                if (!result.isSuccess) println(codeList)
                result.isSuccess mustBe true
            }
        }
      }
    }

    "should fail" - {
      "when code list not found" in {
        running(baseApplicationBuilder) {
          application =>
            val service = application.injector.instanceOf[ListRetrievalService]
            val result  = service.get("foo")
            result.isSuccess mustBe false
        }
      }
    }
  }

  "getCustomsOfficesWithFilter" - {

    val customsOffices = Seq(
      CustomsOffice("XI000142", "Belfast EPU", "XI", None, Seq(Role("DEP"), Role("TRA"))),
      CustomsOffice("GB000001", "Central Community Transit Office.", "GB", None, Seq(Role("AUT"), Role("TRA"))),
      CustomsOffice("GB000005", "Chelmsford Airport", "GB", None, Seq(Role("TRA")))
    )

    "must return values that match the filter" - {
      "when one filter" in {
        val mockResourceService = mock[ResourceService]
        when(mockResourceService.getData[CustomsOffice](any())(any())).thenReturn(customsOffices)

        val app = baseApplicationBuilder
          .apply {
            new GuiceApplicationBuilder()
              .overrides(bind[ResourceService].toInstance(mockResourceService))
          }
          .build()

        running(app) {
          val service      = app.injector.instanceOf[ListRetrievalService]
          val filterParams = FilterParams(Seq("data.countryId" -> "GB"))
          val result       = service.getCustomsOfficesWithFilter(filterParams)

          result mustBe Seq(
            CustomsOffice("GB000001", "Central Community Transit Office.", "GB", None, Seq(Role("AUT"), Role("TRA"))),
            CustomsOffice("GB000005", "Chelmsford Airport", "GB", None, Seq(Role("TRA")))
          )
        }
      }

      "when multiple filters" in {
        val mockResourceService = mock[ResourceService]
        when(mockResourceService.getData[CustomsOffice](any())(any())).thenReturn(customsOffices)

        val app = baseApplicationBuilder
          .apply {
            new GuiceApplicationBuilder()
              .overrides(bind[ResourceService].toInstance(mockResourceService))
          }
          .build()

        running(app) {
          val service      = app.injector.instanceOf[ListRetrievalService]
          val filterParams = FilterParams(Seq("data.countryId" -> "GB", "data.roles.role" -> "AUT"))
          val result       = service.getCustomsOfficesWithFilter(filterParams)

          result mustBe Seq(
            CustomsOffice("GB000001", "Central Community Transit Office.", "GB", None, Seq(Role("AUT"), Role("TRA")))
          )
        }
      }
    }
  }
}
