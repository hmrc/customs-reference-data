/*
 * Copyright 2020 HM Revenue & Customs
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

import java.time.LocalDateTime

import com.google.inject.Inject
import javax.inject.Singleton
import models.MessageInformation
import models.VersionId
import models.VersionInformation
import play.api.libs.json._
import reactivemongo.api.commands.WriteResult
import reactivemongo.play.json.ImplicitBSONHandlers.JsObjectDocumentWriter
import services.VersionIdProducer

import scala.concurrent.ExecutionContext
import scala.concurrent.Future

@Singleton
class VersionRepository @Inject() (versionCollection: VersionCollection, versionIdProducer: VersionIdProducer)(implicit
  ec: ExecutionContext
) {

  def save(messageInformation: MessageInformation): Future[VersionId] = {
    val versionId: VersionId                   = versionIdProducer()
    val time: LocalDateTime                    = LocalDateTime.now()
    val versionInformation: VersionInformation = VersionInformation(messageInformation, versionId, time)

    versionCollection().flatMap {
      _.insert(false)
        .one(Json.toJsObject(versionInformation))
        .flatMap {
          wr =>
            WriteResult
              .lastError(wr)
              .fold(Future.successful(versionId))(_ => Future.failed(new Exception("Failed to save version information")))
        }
    }
  }
}
