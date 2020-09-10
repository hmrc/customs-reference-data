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
import org.scalacheck.Gen
import org.scalatest.matchers.MatchResult
import org.scalatest.matchers.Matcher
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks
import play.api.libs.json.JsObject
import play.api.libs.json.Json

class ReferenceDataPayloadSpec extends SpecBase with ScalaCheckDrivenPropertyChecks with ModelArbitraryInstances {

  "toIterable" - {
    "returns an iterator of the lists with list entries" in {

      val versionId = VersionId("1")

      forAll(Gen.choose(1, 10), Gen.choose(1, 10)) {
        (numberOfLists, numberOfListItems) =>
          forAll(genReferenceDataJson(numberOfLists, numberOfListItems)) {
            data =>
              val referenceDataPayload = ReferenceDataPayload(data)

              val referenceDataLists = referenceDataPayload.toIterable(versionId)

              referenceDataLists.foreach {
                x =>
                  x.length mustEqual numberOfListItems

                  x.foreach {
                    _ must haveVersionId(versionId)
                  }
              }

              referenceDataLists.size mustEqual numberOfLists
          }
      }
    }
  }

  def haveVersionId(expectedVersionId: VersionId): Matcher[GenericListItem] =
    left =>
      MatchResult(
        left.versionId == expectedVersionId,
        s"""Expected GenericListItem with VersionId `${left.versionId}` to equal $expectedVersionId""",
        s"""Expected GenericListItem had VersionId `${left.versionId}` equal $expectedVersionId"""
      )

}
