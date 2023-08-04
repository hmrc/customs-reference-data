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

import com.google.inject.ImplementedBy
import com.google.inject.Inject
import models._
import play.api.Logging
import play.api.libs.json.JsObject
import play.api.libs.json.JsValue
import repositories._
import repositories.v2.ListRepository
import repositories.v2.VersionRepository
import services.consumption.TimeService
import services.ingestion.SchemaValidationService

import scala.concurrent.ExecutionContext
import scala.concurrent.Future

@ImplementedBy(classOf[ReferenceDataServiceImpl])
trait ReferenceDataService {
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
    extends ReferenceDataService
    with Logging {

  def insert(feed: ApiDataSource, payload: ReferenceDataPayload): Future[Option[ErrorDetails]] = {
    val versionId: VersionId = versionIdProducer()
    val now                  = timeService.now()

    for {
      writeResult  <- Future.sequence(payload.toIterable(versionId, now).map(listRepository.insertList))
      insertResult <- versionRepository.save(versionId, payload.messageInformation, feed, payload.listNames, now)
      _            <- cleanUp(payload, versionId, insertResult, writeResult.toList)
    } yield writeResult
      .foldLeft[Option[Seq[ListName]]](None) {
        case (errors, SuccessfulWrite)       => errors
        case (errors, FailedWrite(listName)) => errors.orElse(Some(Seq())).map(_ :+ listName)
      }
      .map(x => WriteError(x.map(_.listName).mkString("[services.ingestion.v2.ReferenceDataServiceImpl]: Failed to insert the following lists: ", ", ", "")))
  }

  private def cleanUp(
    list: ReferenceDataPayload,
    versionId: VersionId,
    insertResult: Boolean,
    writeResult: List[ListRepositoryWriteResult]
  ): Future[Boolean] = {
    // Can not delete the old version if any of the writes failed!
    val writeFailure: Boolean = writeResult.exists(r => r.isInstanceOf[FailedWrite])

    if (insertResult && !writeFailure) {
      logger.info(s"Deleting ${list.listNames.toString} data with a version id less than ${versionId.versionId}")
      for {
        x <- listRepository.deleteOldImports(list, versionId)
        y <- versionRepository.deleteOldImports(versionId)
      } yield (x == SuccessfulDelete, y == SuccessfulVersionDelete) match {
        case (true, false) =>
          logger.warn(s"Delete version failed for data with a version id less than : ${versionId.versionId}")
          false
        case (false, true) =>
          logger.warn(s"Delete list failed for data with a version id less than : ${versionId.versionId}")
          false
        case _ => true
      }
    } else {
      logger.warn(s"Insert failed for : ${versionId.versionId} for lists: ${list.listNames.toString}")
      Future.successful(false)
    }
  }

  def validate(jsonSchemaProvider: JsonSchemaProvider, body: JsValue): Either[ErrorDetails, JsObject] =
    schemaValidationService.validate(jsonSchemaProvider, body)
}
