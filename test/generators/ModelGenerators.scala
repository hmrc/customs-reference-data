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

import models._
import org.scalacheck.Arbitrary
import org.scalacheck.Arbitrary.arbitrary
import play.api.libs.json.JsArray

trait ModelGenerators extends JavaTimeGenerators {

  implicit val arbitraryListName: Arbitrary[ListName] =
    Arbitrary {
      for {
        name <- arbitrary[String]
      } yield ListName(name)
    }

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
      } yield ReferenceDataList(listName, metaData, JsArray.empty)
    }

}
