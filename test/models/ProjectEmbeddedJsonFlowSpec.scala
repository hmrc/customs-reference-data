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
import org.apache.pekko.actor.ActorSystem
import org.apache.pekko.stream.scaladsl.Source
import org.apache.pekko.stream.testkit.scaladsl.TestSink
import play.api.libs.json.JsObject
import play.api.libs.json.Json

import scala.collection.immutable.{Seq => ImmSeq}

class ProjectEmbeddedJsonFlowSpec extends SpecBase {

  implicit lazy val actorSystem: ActorSystem = ActorSystem()

  "ProjectEmbeddedJson" - {
    "returns the embedded json object in the data field" - {
      "when ordinary data" in {
        val listName = ListName("asdf")

        val source = Source.repeat(
          Json.obj(
            "field1" -> "value1",
            "data" -> Json.obj(
              "a" -> "A"
            )
          )
        )

        val expectedValues: ImmSeq[JsObject] = ImmSeq.fill(11) {
          Json.obj(
            "a" -> "A"
          )
        }

        source
          .via(ProjectEmbeddedJsonFlow(listName).project)
          .runWith(TestSink.probe[JsObject])
          .request(11)
          .expectNextN(expectedValues)
      }

      "when data contains raw HTML" in {
        val listName = ListName("asdf")

        val source = Source.repeat(
          Json.obj(
            "data" -> Json.obj(
              "code"        -> "3",
              "description" -> "ENS &amp; EXS"
            )
          )
        )

        val expectedValues: ImmSeq[JsObject] = ImmSeq.fill(11) {
          Json.obj(
            "code"        -> "3",
            "description" -> "ENS & EXS"
          )
        }

        source
          .via(ProjectEmbeddedJsonFlow(listName).project)
          .runWith(TestSink.probe[JsObject])
          .request(11)
          .expectNextN(expectedValues)
      }
    }

    "terminates the stream the element from the if it is missing a data field with embedded object" in {
      val listName = ListName("asdf")

      val source =
        Source.fromIterator(
          () =>
            Seq(
              Json.obj("field1" -> "value1")
            ).iterator
        )

      source
        .via(ProjectEmbeddedJsonFlow(listName).project)
        .runWith(TestSink.probe[JsObject])
        .request(1)
        .expectError()
    }

    "terminates the stream if the element cannot be parsed as a JsObject" in {
      val listName = ListName("asdf")

      val source =
        Source.fromIterator(
          () =>
            Seq(
              Json.obj("field1" -> "value1", "data" -> 1)
            ).iterator
        )

      source
        .via(ProjectEmbeddedJsonFlow(listName).project)
        .runWith(TestSink.probe[JsObject])
        .request(1)
        .expectError()
    }
  }
}
