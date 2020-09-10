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
import java.time.LocalDateTime

import models._
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Arbitrary
import org.scalacheck.Gen
import play.api.libs.json.JsObject

trait ModelArbitraryInstances extends JavaTimeGenerators {

  implicit def arbitraryResourceLinks: Arbitrary[ResourceLinks] =
    Arbitrary {
      for {
        linkKey  <- arbitrary[String]
        link     <- arbitrarySimpleJsObject.arbitrary
        links    <- Gen.option(Map(linkKey -> link))
        metaData <- Gen.option(arbitrary(arbitraryMetaData))
      } yield ResourceLinks(_links = links, metaData = metaData)
    }

  implicit val arbitraryListName: Arbitrary[ListName] =
    Arbitrary(arbitrary[String].map(ListName(_)))

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
      //TODO: This needs updating when we understand what data will be
    }

  implicit val arbitraryMessageInformation: Arbitrary[MessageInformation] =
    Arbitrary {
      for {
        messageId    <- arbitrary[String]
        snapshotDate <- arbitrary[LocalDate]
      } yield MessageInformation(messageId, snapshotDate)
    }

  implicit def arbitrarySimpleJsObject: Arbitrary[JsObject] = Arbitrary(ModelGenerators.genSimpleJsObject)

  implicit def arbitraryGenericListItem(implicit arbJsObject: Arbitrary[JsObject], arbVersionId: Arbitrary[VersionId]): Arbitrary[GenericListItem] =
    Arbitrary {
      for {
        listName           <- arbitrary[ListName]
        messageInformation <- arbitrary[MessageInformation]
        versionId          <- arbVersionId.arbitrary
        data               <- arbJsObject.arbitrary
      } yield GenericListItem(listName, messageInformation, versionId, data)
    }

  implicit val arbitraryVersionId: Arbitrary[VersionId] =
    Arbitrary(arbitrary[String].map(VersionId(_)))

  implicit def arbitraryVersionInformation(implicit ldt: Arbitrary[LocalDateTime]): Arbitrary[VersionInformation] =
    Arbitrary {
      for {
        mi  <- arbitrary[MessageInformation]
        v   <- arbitrary[VersionId]
        ldt <- ldt.arbitrary
      } yield VersionInformation(mi, v, ldt)
    }
}

object ModelArbitraryInstances extends ModelArbitraryInstances
