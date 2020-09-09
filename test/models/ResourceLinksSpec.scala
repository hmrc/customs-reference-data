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
import generators.ModelArbitraryInstances
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks
import play.api.libs.json.JsObject
import play.api.libs.json.JsString
import play.api.libs.json.Json

class ResourceLinksSpec extends SpecBase with ModelArbitraryInstances with ScalaCheckDrivenPropertyChecks {

  "ResourceLinks" - {

    "should" - {

      "deserialise and serialise" in {

        forAll(arbitrary[ResourceLinks]) {
          resourceLinks =>
            val metaData = resourceLinks.metaData match {
              case Some(meta) =>
                Json.obj(
                  "metaData" -> Json.obj(
                    "version"      -> meta.version,
                    "snapshotDate" -> meta.snapshotDate.toString
                  )
                )
              case None => Json.obj()
            }

            val links = resourceLinks._links match {
              case Some(link) =>
                Json.obj(
                  "_links" -> Json.obj(
                    link.head._1 -> Json.toJson(link.head._2)
                  )
                )
              case None => Json.obj()
            }

            val data = links ++ metaData

            data.as[ResourceLinks] mustBe resourceLinks

            Json.toJson(resourceLinks) mustBe data
        }

      }

      "handle empty object" in {

        val resourceLinks = ResourceLinks(
          _links = None,
          metaData = None
        )

        val data = Json.obj()

        data.as[ResourceLinks] mustBe resourceLinks

        Json.toJson(resourceLinks) mustBe data
      }

      "handle complete object" in {

        val data = Json.obj(
          "_links" ->
            Json.obj(
              "self"  -> Json.obj("href" -> "/customs-reference-data/lists"),
              "list1" -> Json.obj("href" -> "/customs-reference-data/list1"),
              "list2" -> Json.obj("href" -> "/customs-reference-data/list2")
            ),
          "metaData" -> Json.obj(
            "version"      -> "12345",
            "snapshotDate" -> "2020-07-06"
          )
        )

        val links = Map(
          "self"  -> JsObject(Seq("href" -> JsString("/customs-reference-data/lists"))),
          "list1" -> JsObject(Seq("href" -> JsString("/customs-reference-data/list1"))),
          "list2" -> JsObject(Seq("href" -> JsString("/customs-reference-data/list2")))
        )

        val metaData = MetaData(
          version = "12345",
          snapshotDate = LocalDate.of(2020, 7, 6)
        )

        val resourceLinks = ResourceLinks(
          _links = Some(links),
          metaData = Some(
            metaData
          )
        )

        data.as[ResourceLinks] mustBe resourceLinks

        Json.toJson(resourceLinks) mustBe data

      }

    }

  }

}
