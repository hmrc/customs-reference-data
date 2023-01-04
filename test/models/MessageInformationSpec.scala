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
import generators.JavaTimeGenerators
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.libs.json.JsSuccess
import play.api.libs.json.Json

import java.time.LocalDate

class MessageInformationSpec extends SpecBase with ScalaCheckPropertyChecks with JavaTimeGenerators {

  "MessageInformation" - {

    "must read from json" in {
      forAll(arbitrary[String], arbitrary[LocalDate]) {
        (messageId, snapshotDate) =>
          Json
            .obj(
              "messageID" -> messageId,
              "snapshotDate" -> Json.obj(
                s"$$date" -> Json.obj(
                  s"$$numberLong" -> snapshotDate.toEpochMilli
                )
              )
            )
            .validate[MessageInformation] mustBe JsSuccess(MessageInformation(messageId, snapshotDate))
      }
    }

    "must write to json" in {
      forAll(arbitrary[String], arbitrary[LocalDate]) {
        (messageId, snapshotDate) =>
          Json.toJson(MessageInformation(messageId, snapshotDate)) mustBe
            Json
              .obj(
                "messageID" -> messageId,
                "snapshotDate" -> Json.obj(
                  s"$$date" -> Json.obj(
                    s"$$numberLong" -> snapshotDate.toEpochMilli
                  )
                )
              )
      }
    }
  }
}
