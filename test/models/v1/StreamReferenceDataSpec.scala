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

package models.v1

import akka.actor.ActorSystem
import akka.stream.scaladsl.Source
import akka.stream.testkit.scaladsl.TestSink
import akka.util.ByteString
import base.SpecBase
import generators.ModelArbitraryInstances
import models.MetaData
import org.scalatest.BeforeAndAfterAll
import org.scalatest.OptionValues
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks
import play.api.libs.json.JsObject
import play.api.libs.json.Json

class StreamReferenceDataSpec extends SpecBase with ScalaCheckDrivenPropertyChecks with ModelArbitraryInstances with OptionValues with BeforeAndAfterAll {

  implicit lazy val actorSystem: ActorSystem = ActorSystem()

  override def afterAll(): Unit =
    actorSystem.terminate()

  "StreamReferenceData" - {

    "must transform stream and turn it into a ByteString" in {

      val name = arbitraryListName.arbitrary.sample.value
      val meta = arbitraryMetaData.arbitrary.sample.value

      val testFlow = StreamReferenceData(name, meta).nestInJson[JsObject]

      val source = Source(1 to 5).map(_ => Json.obj("index" -> "value"))

      val streamOutput: Seq[ByteString] = source
        .via(testFlow)
        .runWith(TestSink.probe[ByteString])
        .request(11)
        .expectNextN(11)

      val result = Json.parse(streamOutput.map(_.utf8String).mkString)
      val url    = controllers.consumption.v1.routes.ListRetrievalController.get(name).url

      val href = (result \ "_links" \ "self" \ "href").as[String]
      url must include(href)
      href mustNot include("v1.0/")
      href mustNot include("v2.0/")

      (result \ "meta").as[MetaData] mustBe meta
      (result \ "id").as[String] mustBe name.listName
      (result \ "data").as[Seq[JsObject]] mustBe Seq.fill(5)(Json.obj("index" -> "value"))

    }
  }
}
