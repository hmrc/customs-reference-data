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
import generators.ModelGenerators._
import org.scalacheck.Arbitrary
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks

class ReferenceDataPayloadSpec extends SpecBase with ScalaCheckDrivenPropertyChecks with ModelArbitraryInstances {

  "itIterator" - {
    "returns an iterator of the lists with list entries" in {
      forAll(Gen.choose(1, 10), Gen.choose(1, 10)) {
        (numberOfLists, numberOfListItems) =>
          forAll(genReferenceDataJson(numberOfLists, numberOfListItems)) {
            data =>
              val referenceDataPayload = ReferenceDataPayload(data)

              val referenceDataLists = referenceDataPayload.toIterator()

              val asdf = referenceDataLists.forall {
                _.length == numberOfListItems
              }

              asdf mustEqual true
              referenceDataLists.size mustEqual numberOfLists
          }
      }
    }
  }
}
