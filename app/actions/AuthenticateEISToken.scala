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

package actions

import com.google.inject.ImplementedBy
import com.google.inject.Inject
import com.google.inject.Singleton
import config.AppConfig
import models.UnauthorisedError
import play.api.Logging
import play.api.http.Status.UNAUTHORIZED
import play.api.libs.json.Json
import play.api.mvc.Results.Status
import play.api.mvc._

import scala.concurrent.ExecutionContext
import scala.concurrent.Future

@ImplementedBy(classOf[AuthenticateEISTokenImpl])
trait AuthenticateEISToken extends ActionFilter[Request] with ActionBuilder[Request, AnyContent]

@Singleton
class AuthenticateEISTokenImpl @Inject() (appConfig: AppConfig, parsers: BodyParsers.Default)(implicit val executionContext: ExecutionContext)
    extends AuthenticateEISToken
    with Logging {

  private val incomingAuthConfig = appConfig.incomingAuth
  private val tokenPattern       = "^Bearer (.+)$".r

  override def filter[A](request: Request[A]): Future[Option[Result]] = {
    val result =
      if (incomingAuthConfig.enabled)
        (for {
          bearerToken <- request.headers.get("Authorization")
          tokenMatch <- {
            logger.debug(s"Bearer token: $bearerToken")
            tokenPattern.findFirstMatchIn(bearerToken)
          }
          token = tokenMatch.group(1)
          if incomingAuthConfig.acceptedTokens.contains(token)
        } yield token) match {
          case Some(_) => None
          case None    => Some(createUnauthorisedResponse(request.headers))
        }
      else
        None
    Future.successful(result)
  }

  private def createUnauthorisedResponse(headers: Headers): Result = {
    val message = "Supplied Bearer token is invalid"
    logger.warn(message)
    Status(UNAUTHORIZED)(Json.toJson(UnauthorisedError(message)))
      .withHeaders(headers.headers.filter(_._1.toLowerCase == "x-correlation-id"): _*)
  }

  override def parser: BodyParser[AnyContent] = parsers
}
