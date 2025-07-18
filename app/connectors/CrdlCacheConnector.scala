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

package connectors

import config.AppConfig
import models.FilterParams
import org.apache.pekko.stream.Materializer
import org.apache.pekko.stream.scaladsl.Source
import org.apache.pekko.util.ByteString
import uk.gov.hmrc.http.client.{HttpClientV2, given}
import uk.gov.hmrc.http.{HeaderCarrier, StringContextOps}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class CrdlCacheConnector @Inject() (config: AppConfig, http: HttpClientV2)(implicit ec: ExecutionContext, mat: Materializer) {

  def get(codeList: String, filterParams: FilterParams)(implicit hc: HeaderCarrier): Future[Source[ByteString, ?]] = {
    val url = url"${config.crdlCacheUrl}/lists/$codeList?${filterParams.toList}"
    http.get(url).stream[Source[ByteString, ?]]
  }

}
