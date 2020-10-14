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

package services.ingestion

import com.google.inject.ImplementedBy
import com.google.inject.Inject
import models.ApiDataSource
import models.ErrorDetails
import models.JsonSchemaProvider
import models.OtherError
import models.ReferenceDataPayload
import play.api.libs.json.JsObject
import play.api.libs.json.JsValue
import repositories.ListRepository.FailedWrite
import repositories.ListRepository.PartialWriteFailure
import repositories.ListRepository.SuccessfulWrite
import repositories.ListRepository
import repositories.VersionRepository
import services.ingestion.ReferenceDataService.DataProcessingResult
import services.ingestion.ReferenceDataService.DataProcessingResult.DataProcessingFailed
import services.ingestion.ReferenceDataService.DataProcessingResult.DataProcessingSuccessful

import scala.concurrent.ExecutionContext
import scala.concurrent.Future

@ImplementedBy(classOf[ReferenceDataServiceImpl])
trait ReferenceDataService {

  def insert(feed: ApiDataSource, payload: ReferenceDataPayload): Future[Either[ErrorDetails, Unit]]

  def validate(jsonSchemaProvider: JsonSchemaProvider, body: JsValue): Either[ErrorDetails, JsObject]

}

private[ingestion] class ReferenceDataServiceImpl @Inject() (
  repository: ListRepository,
  versionRepository: VersionRepository,
  schemaValidationService: SchemaValidationService
)(implicit ec: ExecutionContext)
    extends ReferenceDataService {

  def insert(feed: ApiDataSource, payload: ReferenceDataPayload): Future[Either[ErrorDetails, Unit]] =
    versionRepository.save(payload.messageInformation, feed, payload.listNames).flatMap {
      versionId =>
        Future
          .sequence(
            payload
              .toIterable(versionId)
              .map(repository.insertList)
          )
          .map(_.foldLeft[Either[ErrorDetails, Unit]](Right(())) {
            case (_, SuccessfulWrite) => Right(())
            case (_, error)           => Left(OtherError(error.toString))
          })
    }

  def validate(jsonSchemaProvider: JsonSchemaProvider, body: JsValue): Either[ErrorDetails, JsObject] =
    schemaValidationService.validate(jsonSchemaProvider, body)
}

object ReferenceDataService {

  sealed trait DataProcessingResult

  object DataProcessingResult {
    case object DataProcessingSuccessful extends DataProcessingResult
    case object DataProcessingFailed     extends DataProcessingResult
  }

}
