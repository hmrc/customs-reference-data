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
import play.api.mvc.PathBindable

class ListNameSpec extends SpecBase {

  private val pathBindable = implicitly[PathBindable[ListName]]

  "must bind from path" in {
    val result = pathBindable.bind("listName", "AdditionalReference")

    result.value mustEqual ListName("AdditionalReference")
  }

  "must unbind from path" in {
    val listName = ListName("AdditionalReference")

    val result = pathBindable.unbind("listName", listName)

    result mustEqual "AdditionalReference"
  }

  "must get list code from list name" - {
    "when AdditionalInformation" in {
      val listName = ListName("AdditionalInformation")
      val result   = listName.code
      result.value mustEqual "CL239"
    }

    "when AdditionalInformationCodeSubset" in {
      val listName = ListName("AdditionalInformationCodeSubset")
      val result   = listName.code
      result.value mustEqual "CL752"
    }

    "when AdditionalReference" in {
      val listName = ListName("AdditionalReference")
      val result   = listName.code
      result.value mustEqual "CL380"
    }

    "when an unrecognised list name" in {
      val listName = ListName("foo")
      val result   = listName.code
      result must not be defined
    }
  }
}
