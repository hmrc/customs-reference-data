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

import java.time.LocalDate

import base.SpecBase
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks
import play.api.libs.json.Json

class SingleListSpec extends SpecBase with ScalaCheckDrivenPropertyChecks {

  "toGenericListItem" - {
    "converts a SingleList to toGenericListItem" in {

      val testListName2 = "testListName2"

      val listEntry1 = Json.obj(
        "entryKey" -> "entryValue"
      )

      val messageId    = "74bd0784-8dc9-4eba-a435-9914ace26995"
      val snapshotDate = "2020-07-06"
      val data = Json.obj(
        "messageInformation" -> Json.obj(
          "messageID"    -> messageId,
          "snapshotDate" -> snapshotDate
        ),
        "lists" -> Json
          .obj(
            "testListName1" -> Json.obj(
              "listName" -> "testListName1",
              "listEntries" -> Json.arr(
                Json.obj(
                  "entryKey" -> "entryValue"
                )
              )
            ),
            testListName2 -> Json.obj(
              "listName"    -> testListName2,
              "listEntries" -> Json.arr(listEntry1)
            )
          )
      )

      val referenceDataPayload = ReferenceDataPayload(data)

      val listName           = referenceDataPayload.ListName(testListName2)
      val messageInformation = referenceDataPayload.MessageInformation(messageId, LocalDate.parse(snapshotDate))
      val singleList         = referenceDataPayload.SingleList(listName, messageInformation, Seq(listEntry1))

      val expected = Seq(GenericListItem(listName, messageInformation, listEntry1))

      singleList.toGenericListItem mustEqual expected
    }
  }

}
