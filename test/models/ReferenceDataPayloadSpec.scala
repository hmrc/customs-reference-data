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
import play.api.libs.json.JsObject
import play.api.libs.json.Json

class ReferenceDataPayloadSpec extends SpecBase with ScalaCheckDrivenPropertyChecks {

  "listsNames" - {
    "should return an empty Seq when there lists" in {

      val data = Json.obj(
        "messageInformation" -> Json.obj(
          "messageID"    -> "74bd0784-8dc9-4eba-a435-9914ace26995",
          "snapshotDate" -> "2020-07-06"
        ),
        "lists" -> JsObject.empty
      )

      val payload = ReferenceDataPayload(data)

      val expected = Set.empty[ListName]

      payload.listsNames mustEqual expected
    }

    "should return a list of all the list names that are included in the payload" in {

      val data = Json.obj(
        "messageInformation" -> Json.obj(
          "messageID"    -> "74bd0784-8dc9-4eba-a435-9914ace26995",
          "snapshotDate" -> "2020-07-06"
        ),
        "lists" -> Json.obj(
          "testListName1" -> Json.obj(
            "listName" -> "testListName1",
            "listEntries" -> Json.arr(
              Json.obj(
                "entryKey" -> "entryValue"
              )
            )
          ),
          "testListName2" -> Json.obj(
            "listName" -> "testListName2",
            "listEntries" -> Json.arr(
              Json.obj(
                "entryKey" -> "entryValue"
              )
            )
          )
        )
      )

      val listNames = ReferenceDataPayload(data).listsNames

      listNames mustEqual Set(ListName("testListName1"), ListName("testListName2"))
    }
  }

  "getlist" - {
    "returns the list for a listName" in {

      val testListName2 = "testListName2"

      val listEntry = Json.obj(
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
              "listEntries" -> Json.arr(listEntry)
            )
          )
      )

      val listName = ListName(testListName2)

      val expectedList = SingleList(listName, MessageInformation(messageId, LocalDate.parse(snapshotDate)), Seq(listEntry))

      ReferenceDataPayload(data).getList(listName) mustEqual expectedList
    }
  }

}
