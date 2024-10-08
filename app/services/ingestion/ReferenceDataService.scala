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

package services.ingestion

import com.google.inject.ImplementedBy
import com.google.inject.Inject
import models._
import play.api.Logging
import play.api.libs.json.JsObject
import play.api.libs.json.JsValue
import repositories.ListRepository
import repositories.VersionRepository
import repositories.FailedWrite
import repositories.ListRepositoryWriteResult
import repositories.SuccessfulWrite
import repositories.VersionIdProducer
import services.TimeService

import scala.concurrent.ExecutionContext
import scala.concurrent.Future

@ImplementedBy(classOf[ReferenceDataServiceImpl])
trait ReferenceDataService extends Logging {
  def insert(feed: ApiDataSource, payload: ReferenceDataPayload): Future[Option[ErrorDetails]]
  def validate(jsonSchemaProvider: JsonSchemaProvider, body: JsValue): Either[ErrorDetails, JsObject]
}

private[ingestion] class ReferenceDataServiceImpl @Inject() (
  listRepository: ListRepository,
  versionRepository: VersionRepository,
  schemaValidationService: SchemaValidationService,
  versionIdProducer: VersionIdProducer,
  timeService: TimeService
)(implicit ec: ExecutionContext)
    extends ReferenceDataService {

  def insert(feed: ApiDataSource, payload: ReferenceDataPayload): Future[Option[ErrorDetails]] = {
    val versionId = versionIdProducer()
    val now       = timeService.currentInstant()

    for {
      writeResult <- Future.sequence(payload.toIterable(versionId, now).map(listRepository.insertList))
      _           <- versionRepository.save(versionId, payload.messageInformation, feed, payload.listNames, now)
    } yield writeResult
      .foldLeft[Option[Seq[ListRepositoryWriteResult]]](None) {
        case (errors, write: SuccessfulWrite) =>
          logger.info(write.toString)
          errors
        case (errors, write: FailedWrite) =>
          logger.info(write.toString)
          errors.orElse(Some(Seq())).map(_ :+ write)
      }
      .map {
        x =>
          WriteError(x.map(_.listName.listName).mkString("[services.ingestion.ReferenceDataServiceImpl]: Failed to insert the following lists: ", ", ", ""))
      }
  }

  def validate(jsonSchemaProvider: JsonSchemaProvider, body: JsValue): Either[ErrorDetails, JsObject] =
    schemaValidationService.validate(jsonSchemaProvider, body)
}
