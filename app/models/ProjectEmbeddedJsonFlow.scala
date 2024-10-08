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

import org.apache.pekko.NotUsed
import org.apache.pekko.stream.ActorAttributes
import org.apache.pekko.stream.Attributes
import org.apache.pekko.stream.Supervision
import org.apache.pekko.stream.scaladsl.Flow
import play.api.Logging
import play.api.libs.json.JsObject
import play.api.libs.json.JsResultException

class ProjectEmbeddedJsonFlow(listName: ListName) extends Logging {

  private val supervisionStrategy: Attributes = ActorAttributes.supervisionStrategy {
    case x: JsResultException if x.errors.map(_._2).exists(_.exists(_.message.contains("""'data' is undefined on object"""))) =>
      logger.error(s"""Error when transforming ${listName.listName}. Expected data item to have "data" field""")
      Supervision.Stop
    case x: JsResultException =>
      logger.error(s"Unexpected error when transforming ${listName.listName}", x)
      Supervision.Stop
    case _ => Supervision.Stop
  }

  def project: Flow[JsObject, JsObject, NotUsed] =
    Flow[JsObject]
      .map {
        jsObject =>
          (jsObject \ "data")
            .as[JsObject]
            .unescapeXml
      }
      .withAttributes(supervisionStrategy)

}

object ProjectEmbeddedJsonFlow {
  def apply(listName: ListName): ProjectEmbeddedJsonFlow = new ProjectEmbeddedJsonFlow(listName)
}
