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

import models._
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Arbitrary
import org.scalacheck.Gen
import play.api.libs.json.JsObject

import java.time.LocalDate
import java.time.LocalDateTime

trait ModelArbitraryInstances extends JavaTimeGenerators {

  val maxListSize = 10

  implicit def arbitraryResourceLinks: Arbitrary[ResourceLinks] =
    Arbitrary {
      for {
        size      <- Gen.chooseNum(1, maxListSize)
        listNames <- Gen.listOfN(size, arbitrary[ListName])
      } yield ResourceLinks(listNames)
    }

  implicit val arbitraryListName: Arbitrary[ListName] =
    Arbitrary(BaseGenerators.stringsWithMaxLength(50).map(ListName(_)))

  implicit val arbitraryMetaData: Arbitrary[MetaData] =
    Arbitrary {
      for {
        version  <- arbitrary[String]
        snapShot <- arbitrary[LocalDate]
      } yield MetaData(version, snapShot)
    }

  implicit val arbitraryReferenceDataList: Arbitrary[ReferenceDataList] =
    Arbitrary {
      for {
        listName <- arbitrary[ListName]
        metaData <- arbitrary[MetaData]
      } yield ReferenceDataList(listName, metaData, Nil)
    }

  implicit val arbitraryMessageInformation: Arbitrary[MessageInformation] =
    Arbitrary {
      for {
        messageId    <- arbitrary[String]
        snapshotDate <- arbitrary[LocalDate]
      } yield MessageInformation(messageId, snapshotDate)
    }

  implicit def arbitrarySimpleJsObject: Arbitrary[JsObject] = Arbitrary(ModelGenerators.genSimpleJsObject)

  implicit def arbitraryNewGenericListItem(implicit
    arbJsObject: Arbitrary[JsObject],
    arbVersionId: Arbitrary[VersionId]
  ): Arbitrary[GenericListItem] =
    Arbitrary {
      for {
        listName           <- arbitrary[ListName]
        messageInformation <- arbitrary[MessageInformation]
        versionId          <- arbVersionId.arbitrary
        data               <- arbJsObject.arbitrary
        createdOn          <- arbitrary[LocalDateTime]
      } yield GenericListItem(listName, messageInformation, versionId, data, createdOn)
    }

  implicit val arbitraryVersionId: Arbitrary[VersionId] =
    Arbitrary(arbitrary[String].map(VersionId(_)))

  implicit val arbitraryApiDataSource: Arbitrary[ApiDataSource] =
    Arbitrary(Gen.oneOf(ApiDataSource.RefDataFeed, ApiDataSource.ColDataFeed))

  implicit def arbitraryVersionInformation(implicit ldt: Arbitrary[LocalDateTime]): Arbitrary[VersionInformation] =
    Arbitrary {
      for {
        mi  <- arbitrary[MessageInformation]
        v   <- arbitrary[VersionId]
        ldt <- ldt.arbitrary
        api <- arbitrary[ApiDataSource]
        vf  <- arbitrary[ListName]
      } yield VersionInformation(mi, v, ldt, api, Seq(vf))
    }

  implicit def arbitraryReferenceDataPayload: Arbitrary[ReferenceDataPayload] =
    Arbitrary(Gen.oneOf(ModelGenerators.genReferenceDataListsPayload(), ModelGenerators.genCustomsOfficeListsPayload()))
}

object ModelArbitraryInstances extends ModelArbitraryInstances
