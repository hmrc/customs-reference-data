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

package models

import base.SpecBase
import generators.ModelArbitraryInstances
import generators.ModelGenerators._
import org.scalacheck.Gen
import org.scalatest.matchers.MatchResult
import org.scalatest.matchers.Matcher
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks

import java.time.Instant

class ReferenceDataPayloadSpec extends SpecBase with ScalaCheckDrivenPropertyChecks with ModelArbitraryInstances {

  "ReferenceDataListsPayload" - {
    "messageInformation" in {
      val referenceDataListsPayload = genReferenceDataListsPayload(1, 1).sample.value

      referenceDataListsPayload.messageInformation mustBe a[MessageInformation]
    }

    "listNames" - {
      "returns all the listNames for all lists" in {

        val versionId = VersionId("1")
        val createdOn = Instant.now()

        forAll(Gen.choose(1, 5), Gen.choose(1, 5)) {
          (numberOfLists, numberOfListItems) =>
            forAll(genReferenceDataListsJson(numberOfLists, numberOfListItems)) {
              data =>
                val referenceDataPayload = ReferenceDataListsPayload(data)

                val expectedResult = referenceDataPayload.toIterable(versionId, createdOn).flatMap(_.map(_.listName)).toSeq.distinct.toSet

                referenceDataPayload.listNames.toSet mustEqual expectedResult
            }
        }
      }
    }

    "toIterable" - {
      "returns an iterator of the lists with list entries" in {

        val versionId = VersionId("1")
        val createdOn = Instant.now()

        forAll(Gen.choose(1, 10), Gen.choose(1, 10)) {
          (numberOfLists, numberOfListItems) =>
            forAll(genReferenceDataListsJson(numberOfLists, numberOfListItems)) {
              data =>
                val referenceDataPayload = ReferenceDataListsPayload(data)

                val referenceDataLists = referenceDataPayload.toIterable(versionId, createdOn)

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

  }

  def haveVersionId(expectedVersionId: VersionId): Matcher[GenericListItem] =
    left =>
      MatchResult(
        left.versionId == expectedVersionId,
        s"""Expected GenericListItem with VersionId `${left.versionId}` to equal $expectedVersionId""",
        s"""Expected GenericListItem had VersionId `${left.versionId}` equal $expectedVersionId"""
      )

}
