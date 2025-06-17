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

import play.api.mvc.QueryStringBindable

case class FilterParams(parameters: Seq[(String, Seq[String])]) {

  def toList: Seq[(String, String)] =
    parameters.flatMap {
      case (key, values) =>
        values.map {
          value => key -> value
        }
    }
}

object FilterParams {

  def apply(): FilterParams = new FilterParams(Nil)

  implicit def queryStringBindable: QueryStringBindable[FilterParams] =
    new QueryStringBindable[FilterParams] {

      override def bind(key: String, params: Map[String, Seq[String]]): Option[Either[String, FilterParams]] =
        params.toSeq match {
          case Nil => None
          case x   => Some(Right(FilterParams(x)))
        }

      override def unbind(key: String, filters: FilterParams): String =
        filters.parameters
          .flatMap {
            case (key, values) =>
              values.map(
                value => s"$key=$value"
              )
          }
          .mkString("&")
    }
}
