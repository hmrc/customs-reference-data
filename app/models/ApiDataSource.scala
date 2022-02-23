/*
 * Copyright 2022 HM Revenue & Customs
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

import play.api.libs.json.Json
import play.api.libs.json.Reads
import play.api.libs.json.Writes
import repositories.Query

sealed trait ApiDataSource {
  def asString: String
}

object ApiDataSource {

  case object RefDataFeed extends ApiDataSource {
    override def asString: String = "RefDataFeed"
  }

  case object ColDataFeed extends ApiDataSource {
    override def asString: String = "ColDataFeed"
  }

  val fromString: PartialFunction[String, ApiDataSource] = {
    case "RefDataFeed" => RefDataFeed
    case "ColDataFeed" => ColDataFeed
  }

  implicit val jsonWrites: Writes[ApiDataSource] =
    implicitly[Writes[String]]
      .contramap(_.toString)

  implicit val jsonReads: Reads[ApiDataSource] =
    implicitly[Reads[String]]
      .map(fromString.lift)
      .map(_.get)

  implicit val query: Query[ApiDataSource] =
    Query.fromWrites(ads => Json.obj("source" -> ads))

}
