/*
 * Copyright 2025 HM Revenue & Customs
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
import models.Phase
import models.Phase.*
import models.request.VersionedRequest
import play.api.mvc.*
import play.api.mvc.Results.BadRequest
import sttp.model.HeaderNames.Accept

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

@ImplementedBy(classOf[VersionedActionImpl])
trait VersionedAction extends ActionRefiner[Request, VersionedRequest] with ActionBuilder[VersionedRequest, AnyContent]

class VersionedActionImpl @Inject() (override val parser: BodyParsers.Default)(implicit val executionContext: ExecutionContext) extends VersionedAction {

  override protected def refine[A](request: Request[A]): Future[Either[Result, VersionedRequest[A]]] = {
    val pattern = """application/vnd.hmrc.(.*)\+json""".r

    Future.successful {
      request.headers.get(Accept) match {
        case Some(pattern(version)) =>
          Phase(version) match {
            case Some(phase) => Right(VersionedRequest(request, phase))
            case None        => Left(BadRequest(s"Accept header contains an invalid version '$version'"))
          }
        case _ =>
          Right(VersionedRequest(request, Phase5))
      }
    }
  }
}
