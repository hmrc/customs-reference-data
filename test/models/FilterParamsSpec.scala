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

package models

import base.SpecBase
import play.api.mvc.QueryStringBindable

class FilterParamsSpec extends SpecBase {

  private val queryStringBindable = implicitly[QueryStringBindable[FilterParams]]

  "must bind from query params" - {
    "when no values" in {

      val result = queryStringBindable.bind("filter", Map())

      result must not be defined
    }

    "when one value" in {
      val parameters = Seq(
        "foo" -> Seq("bar")
      )

      val expectedResult = new FilterParams(parameters)

      val result = queryStringBindable.bind("filter", Map("foo" -> Seq("bar")))

      result.value.value mustEqual expectedResult
    }

    "when multiple values" in {
      val parameters = Seq(
        "foo" -> Seq("bar", "baz")
      )

      val expectedResult = new FilterParams(parameters)

      val result = queryStringBindable.bind("filter", Map("foo" -> Seq("bar", "baz")))

      result.value.value mustEqual expectedResult
    }
  }

  "must unbind from filter params" - {
    "when one value" in {
      val parameters = Seq(
        "foo" -> Seq("bar")
      )

      val filterParams = new FilterParams(parameters)

      val result = queryStringBindable.unbind("filter", filterParams)

      result mustEqual "foo=bar"
    }

    "when multiple values" in {
      val parameters = Seq(
        "foo" -> Seq("bar", "baz")
      )

      val filterParams = new FilterParams(parameters)

      val result = queryStringBindable.unbind("filter", filterParams)

      result mustEqual "foo=bar&foo=baz"
    }

    "when multiple params" in {
      val parameters = Seq(
        "country"    -> Seq("GB", "XI"),
        "roles.role" -> Seq("TRA", "DES")
      )

      val filterParams = new FilterParams(parameters)

      val result = queryStringBindable.unbind("filter", filterParams)

      result mustEqual "country=GB&country=XI&roles.role=TRA&roles.role=DES"
    }
  }

  "must convert to Seq[(String, String)]" - {
    "when one query with one value" in {
      val parameters = Seq(
        "country" -> Seq("GB")
      )

      val filterParams = FilterParams(parameters)

      val result = filterParams.toList

      result mustEqual Seq(
        "country" -> "GB"
      )
    }

    "when one query with multiple values" in {
      val parameters = Seq(
        "country" -> Seq("GB", "XI")
      )

      val filterParams = FilterParams(parameters)

      val result = filterParams.toList

      result mustEqual Seq(
        "country" -> "GB",
        "country" -> "XI"
      )
    }

    "when two queries with multiple values" in {
      val parameters = Seq(
        "country"    -> Seq("GB", "XI"),
        "roles.role" -> Seq("TRA", "DES")
      )

      val filterParams = FilterParams(parameters)

      val result = filterParams.toList

      result mustEqual Seq(
        "country"    -> "GB",
        "country"    -> "XI",
        "roles.role" -> "TRA",
        "roles.role" -> "DES"
      )
    }
  }
}
