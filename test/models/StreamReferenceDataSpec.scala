/*
 * Copyright 2021 HM Revenue & Customs
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

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.Materializer
import akka.stream.scaladsl.Flow
import akka.stream.scaladsl.Source
import akka.stream.testkit.scaladsl.TestSink
import akka.util.ByteString
import base.SpecBase
import generators.ModelArbitraryInstances
import org.scalatest.OptionValues
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks
import play.api.libs.json.JsObject
import play.api.libs.json.JsValue
import play.api.libs.json.Json

class StreamReferenceDataSpec extends SpecBase with ScalaCheckDrivenPropertyChecks with ModelArbitraryInstances with OptionValues {

  "StreamReferenceData" - {

    "must transform stream and turn it into a ByteString" in {

      implicit lazy val actorSystem: ActorSystem = ActorSystem()
      implicit lazy val mat: Materializer        = ActorMaterializer()

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

      (result \ "_links" \ "self" \ "href").as[String] mustBe controllers.consumption.routes.ListRetrievalController.get(name).url
      (result \ "meta").as[MetaData] mustBe meta
      (result \ "id").as[String] mustBe name.listName
      (result \ "data").as[Seq[JsObject]] mustBe Seq.fill(5)(Json.obj("index" -> "value"))
    }
  }
}
