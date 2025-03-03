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

import com.google.inject.{ImplementedBy, Inject, Singleton}
import play.api.Logging
import play.api.http.HeaderNames.*
import play.api.mvc.*

import scala.concurrent.{ExecutionContext, Future}

@ImplementedBy(classOf[LogHeadersImpl])
trait LogHeaders extends ActionFilter[Request] with ActionBuilder[Request, AnyContent]

@Singleton
class LogHeadersImpl @Inject() (
  parsers: BodyParsers.Default
)(implicit val executionContext: ExecutionContext)
    extends LogHeaders
    with Logging {

  override def filter[A](request: Request[A]): Future[Option[Result]] = {
    val headersToLog = Seq(
      ACCEPT,
      ACCEPT_ENCODING,
      CONTENT_ENCODING,
      CONTENT_TYPE,
      CONTENT_LENGTH,
      TRANSFER_ENCODING
    )

    val headers = request.headers.headers
      .filter {
        case (header, _) => headersToLog.contains(header)
      }
      .mkString(", ")
    logger.info(s"Headers: $headers")
    Future.successful(None)
  }

  override def parser: BodyParser[AnyContent] = parsers
}
