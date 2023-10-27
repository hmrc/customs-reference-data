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

package play

import play.api.Configuration
import play.api.http.HeaderNames.ACCEPT
import play.api.mvc.RequestHeader

import javax.inject.Inject

class RequestHeaderUtils @Inject() (config: Configuration) {

  private val headerRegex = "application/vnd\\.hmrc\\.(.*)\\+(gzip|json)".r

  private lazy val unversionedContexts = config
    .getOptional[Seq[String]]("versioning.unversionedContexts")
    .getOrElse(Seq.empty[String])

  def isRequestUnversioned(request: RequestHeader): Boolean =
    unversionedContexts.exists(request.uri.startsWith(_))

  def getVersionedRequest(originalRequest: RequestHeader): RequestHeader = {
    val version = getVersion(originalRequest)

    originalRequest.withTarget(
      originalRequest.target
        .withUriString(versionedUri(originalRequest.uri, version))
        .withPath(versionedUri(originalRequest.path, version))
    )
  }

  // If no version default to v1.0 (NCTS P4)
  private def getVersion(originalRequest: RequestHeader): String =
    originalRequest.headers
      .get(ACCEPT)
      .flatMap(headerRegex.findFirstMatchIn(_))
      .map(_.group(1))
      .getOrElse("1.0")

  private def versionedUri(urlPath: String, version: String): String =
    urlPath match {
      case "/" => s"/v$version"
      case _   => s"/v$version$urlPath"
    }
}
