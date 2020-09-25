/*
 * Copyright 2020 HM Revenue & Customs
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
import generators.ModelArbitraryInstances
import org.scalacheck.Arbitrary.arbitrary
import org.scalatest.OptionValues
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks
import play.api.libs.json.Json

class ReferenceDataListSpec extends SpecBase with ScalaCheckDrivenPropertyChecks with ModelArbitraryInstances with OptionValues {

  "ReferenceDataList" - {

    "must serialise and deserialise" in {
      forAll(arbitrary[ReferenceDataList]) {
        referenceDataList =>
          Json.toJsObject(referenceDataList).validate[ReferenceDataList].get mustBe referenceDataList
      }
    }
  }

}
