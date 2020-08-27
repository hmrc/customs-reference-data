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

package generators

import java.time.LocalDate

import models.ReferenceDataPayload
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import play.api.libs.json.JsObject
import play.api.libs.json.JsString
import play.api.libs.json.Json

trait ModelGenerators {
  self: BaseGenerators with JavaTimeGenerators =>

  val genSimpleJsString: Gen[JsString] = arbitrary[String].map(JsString)

  val genSimpleJsObject: Gen[JsObject] =
    for {
      key   <- arbitrary[String]
      value <- genSimpleJsString
    } yield Json.obj(key -> value)

  def genReferenceList(numberOfLists: Int = 5, dataItemsGen: Option[Gen[JsObject]] = None): Gen[JsObject] =
    for {
      listNames <- arbitrary[String]
      listItems <- Gen.listOfN(numberOfLists, dataItemsGen.getOrElse(genSimpleJsObject))
    } yield Json.obj(
      listNames -> Json.obj(
        "listName"    -> listNames,
        "listEntries" -> listItems
      )
    )

  def genReferenceDataPayload(numberOfLists: Int = 5, numberOfListItems: Int = 5, dataItemsGen: Option[Gen[JsObject]] = None): Gen[ReferenceDataPayload] =
    for {
      messageId    <- arbitrary[String]
      snapshotDate <- arbitrary[LocalDate]
      lists        <- Gen.listOfN(numberOfLists, genReferenceList(numberOfListItems, dataItemsGen))
    } yield {
      val json = Json.obj(
        "messageInformation" -> Json.obj(
          "messageID"    -> messageId,
          "snapshotDate" -> snapshotDate
        ),
        "lists" -> lists.foldLeft(Json.obj())(_ ++ _)
      )

      ReferenceDataPayload(json)
    }

}

object ModelGenerators extends ModelGenerators with BaseGenerators with JavaTimeGenerators
