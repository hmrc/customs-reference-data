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

package services.ingestion.v2

import cats.data.EitherT
import com.google.inject.{ImplementedBy, Inject}
import models._
import play.api.Logging
import play.api.libs.json.{JsObject, JsValue}
import repositories._
import repositories.v2.{ListRepository, VersionRepository}
import services.consumption.TimeService
import services.ingestion.SchemaValidationService

import java.time.Instant
import scala.concurrent.{ExecutionContext, Future}

@ImplementedBy(classOf[ReferenceDataServiceImpl])
trait ReferenceDataService {
  def insert(feed: ApiDataSource, payload: ReferenceDataPayload): EitherT[Future, ErrorDetails, List[SuccessState.type]]
  def validate(jsonSchemaProvider: JsonSchemaProvider, body: JsValue): Either[ErrorDetails, JsObject]
}

private[ingestion] class ReferenceDataServiceImpl @Inject() (
  listRepository: ListRepository,
  versionRepository: VersionRepository,
  schemaValidationService: SchemaValidationService,
  versionIdProducer: VersionIdProducer,
  timeService: TimeService
)(implicit ec: ExecutionContext)
    extends ReferenceDataService
    with Logging {

  def insert(feed: ApiDataSource, payload: ReferenceDataPayload): EitherT[Future, ErrorDetails, List[SuccessState.type]] = {
    val versionId: VersionId = versionIdProducer()
    val now: Instant         = timeService.now()

    import cats.syntax.all._

    for {
      writeResult  <- payload.toIterable(versionId, now).toList.traverse(listRepository.insertList)
      _            <- versionRepository.save(versionId, payload.messageInformation, feed, payload.listNames, now)
      _            <- listRepository.deleteOldImports(payload, now)
      _            <- versionRepository.deleteOldImports(now, versionId)
    } yield writeResult
  }

  def validate(jsonSchemaProvider: JsonSchemaProvider, body: JsValue): Either[ErrorDetails, JsObject] =
    schemaValidationService.validate(jsonSchemaProvider, body)
}
