/*
 * Copyright 2021 HM Revenue & Customs
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
import models._
import play.api.libs.json.JsObject
import play.api.libs.json.JsValue
import repositories.DefaultListRepository.FailedWrite
import repositories.DefaultListRepository.PartialWriteFailure
import repositories.DefaultListRepository.SuccessfulWrite
import repositories.ListRepository
import repositories.VersionIdProducer
import repositories.VersionRepository

import scala.concurrent.ExecutionContext
import scala.concurrent.Future

@ImplementedBy(classOf[ReferenceDataServiceImpl])
trait ReferenceDataService {

  def insert(feed: ApiDataSource, payload: ReferenceDataPayload): Future[Option[ErrorDetails]]

  def validate(jsonSchemaProvider: JsonSchemaProvider, body: JsValue): Either[ErrorDetails, JsObject]

}

private[ingestion] class ReferenceDataServiceImpl @Inject() (
  repository: ListRepository,
  versionRepository: VersionRepository,
  schemaValidationService: SchemaValidationService,
  versionIdProducer: VersionIdProducer
)(implicit ec: ExecutionContext)
    extends ReferenceDataService {

  def insert(feed: ApiDataSource, payload: ReferenceDataPayload): Future[Option[ErrorDetails]] = {
    val versionId = versionIdProducer()

    Future
      .sequence(
        payload
          .toIterable(versionId)
          .map(repository.insertList)
      )
      .flatMap(
        writeResult =>
          versionRepository
            .save(versionId, payload.messageInformation, feed, payload.listNames)
            .map(
              _ =>
                writeResult
                  .foldLeft[Option[Seq[ListName]]](None) {
                    case (errors, SuccessfulWrite)                  => errors
                    case (errors, PartialWriteFailure(listName, _)) => errors.orElse(Some(Seq())).map(_ :+ listName)
                    case (errors, FailedWrite(listName))            => errors.orElse(Some(Seq())).map(_ :+ listName)
                  }
                  .map(x => WriteError(x.map(_.listName).mkString("Failed to insert the following lists: ", ", ", "")))
            )
      )
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
