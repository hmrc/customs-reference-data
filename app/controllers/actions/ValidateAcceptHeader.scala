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

package controllers.actions

import com.google.inject.ImplementedBy
import com.google.inject.Inject
import com.google.inject.Singleton
import play.api.Logging
import play.api.mvc.*
import play.api.mvc.Results.BadRequest

import scala.concurrent.ExecutionContext
import scala.concurrent.Future

@ImplementedBy(classOf[ValidateAcceptHeaderImpl])
trait ValidateAcceptHeader extends ActionFilter[Request] with ActionBuilder[Request, AnyContent]

@Singleton
class ValidateAcceptHeaderImpl @Inject() (override val parser: BodyParsers.Default)(implicit val executionContext: ExecutionContext)
    extends ValidateAcceptHeader
    with Logging {

  private val pattern = "application/vnd\\.hmrc\\.2\\.0\\+(gzip|json)".r

  override def filter[A](request: Request[A]): Future[Option[Result]] =
    Future.successful {
      request.headers.get("Accept") match {
        case Some(value) if pattern.matches(value) => None
        case _                                     => Some(BadRequest)
      }
    }
}
