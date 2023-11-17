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

package generators

import models.MessageInformation
import models.ReferenceDataListsPayload
import org.scalacheck.Gen
import play.api.libs.json.JsObject
import play.api.libs.json.JsString
import play.api.libs.json.Json

import java.time.Instant

trait ModelGenerators {
  self: BaseGenerators with JavaTimeGenerators =>

  val genSimpleJsObject: Gen[JsObject] =
    for {
      key   <- nonEmptyString
      value <- nonEmptyString.map(JsString)
    } yield Json.obj(key -> value)

  def genReferenceList(numberOfLists: Int = 5, dataItemsGen: Option[Gen[JsObject]] = None, listNameGen: Option[Gen[String]] = None): Gen[JsObject] =
    for {
      listNames <- listNameGen.getOrElse(stringsWithMaxLength(10))
      listItems <- Gen.listOfN(numberOfLists, dataItemsGen.getOrElse(genSimpleJsObject))
    } yield Json.obj(
      listNames -> Json.obj(
        "listName"    -> listNames,
        "listEntries" -> listItems
      )
    )

  /**
    * Generator that sample full reference data push as JSON
    *
    * @param numberOfLists The number of lists for the sample reference data json
    * @param numberOfListItems The number of list items to populate the list's listEntries with
    * @param messageInformation The metadata for the reference data. This allows the caller to override the default random values and specify their own
    * @return A [[play.api.libs.json.JsObject]] that represents a full reference data push
    */
  def genReferenceDataListsJson(
    numberOfLists: Int = 5,
    numberOfListItems: Int = 5,
    messageInformation: Option[Gen[MessageInformation]] = None,
    dataItemsGen: Option[Gen[JsObject]] = None
  ): Gen[JsObject] = {
    import generators.ModelArbitraryInstances.arbitraryMessageInformation

    require(numberOfLists >= 1, "Number of lists should be greater than 1")

    // This is used to ensure that there are no collisions in the listNames so that we generate the specified number of lists.
    val suffix: Iterator[String] = Iterator.from(0, 1).map(_.toString)

    val jsObjGen2: Gen[JsObject] = {
      val listNameGen: Gen[String] = stringsWithMaxLength(10).map(_ ++ suffix.next())
      genReferenceList(numberOfListItems, dataItemsGen, listNameGen = Some(listNameGen))
    }

    for {
      messageInformation <- messageInformation.getOrElse(arbitraryMessageInformation.arbitrary)
      lists              <- Gen.listOfN(numberOfLists, jsObjGen2)
      listsObject = lists.foldLeft(Json.obj())(_ ++ _)
    } yield Json.obj(
      "messageInformation" -> Json.obj(
        "messageID"    -> messageInformation.messageId,
        "snapshotDate" -> messageInformation.snapshotDate
      ),
      "lists" -> listsObject
    )
  }

  def genReferenceDataListsPayload(
    numberOfLists: Int = 5,
    numberOfListItems: Int = 5,
    dataItemsGen: Option[Gen[JsObject]] = None
  ): Gen[ReferenceDataListsPayload] =
    genReferenceDataListsJson(numberOfLists, numberOfListItems, dataItemsGen = dataItemsGen)
      .map(ReferenceDataListsPayload(_))

  def genCustomsOfficeListsPayload(
    numberOfLists: Int = 5,
    numberOfListItems: Int = 5,
    dataItemsGen: Option[Gen[JsObject]] = None
  ): Gen[ReferenceDataListsPayload] =
    genReferenceDataListsJson(numberOfLists, numberOfListItems, dataItemsGen = dataItemsGen)
      .map(ReferenceDataListsPayload(_))

  val genInstant: Gen[Instant] = Gen
    .chooseNum(0L, 10000L)
    .map(Instant.ofEpochMilli)

}

object ModelGenerators extends ModelGenerators with BaseGenerators with JavaTimeGenerators
