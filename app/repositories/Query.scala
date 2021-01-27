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

package repositories

import play.api.libs.json.JsObject
import play.api.libs.json.OWrites

trait Query[A] {
  def toJson(a: A): JsObject
}

object Query {

  def apply[A: Query]: Query[A] = implicitly[Query[A]]

  def fromWrites[A: OWrites]: Query[A] = implicitly[OWrites[A]].writes _

  implicit class QueryOps[A](a: A) {
    def query(implicit qa: Query[A]): JsObject = Query[A].toJson(a)
  }

}
