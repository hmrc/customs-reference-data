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

import cats.data.EitherT
import com.google.inject.{ImplementedBy, Inject}
import models.*
import play.api.Logging
import repositories.*
import services.TimeService
import uk.gov.hmrc.mongo.MongoComponent
import uk.gov.hmrc.mongo.transaction.{TransactionConfiguration, Transactions}

import java.time.Instant
import scala.concurrent.{ExecutionContext, Future}
import scala.util.control.NonFatal

@ImplementedBy(classOf[ReferenceDataServiceImpl])
trait ReferenceDataService extends Logging {
  def insert(feed: ApiDataSource, payload: ReferenceDataPayload): Future[Either[ErrorDetails, Unit]]
}

private[ingestion] class ReferenceDataServiceImpl @Inject() (
  listRepository: ListRepository,
  versionRepository: VersionRepository,
  versionIdProducer: VersionIdProducer,
  timeService: TimeService,
  val mongoComponent: MongoComponent
)(implicit ec: ExecutionContext)
    extends ReferenceDataService
    with Transactions {

  implicit private val tc: TransactionConfiguration = TransactionConfiguration.strict

  def insert(feed: ApiDataSource, payload: ReferenceDataPayload): Future[Either[ErrorDetails, Unit]] = {
    val versionId = versionIdProducer()
    val now       = timeService.currentInstant()

    versionRepository.getExpiredVersions(now, feed).flatMap {
      versionIds =>
        withSessionAndTransaction(
          _ =>
            (
              for {
                _           <- EitherT(versionRepository.save(versionId, payload.messageInformation, feed, payload.listNames, now))
                writeResult <- EitherT(insert(payload, versionId, now))
                _           <- EitherT(listRepository.remove(versionIds))
                _           <- EitherT(versionRepository.remove(versionIds))
              } yield writeResult
            ).value.map {
              case Left(value) =>
                throw ErrorDetailsException(value)
              case Right(value) =>
                Right(value)
            }
        ).recover {
          case e: ErrorDetailsException => Left(e.errorDetails)
          case NonFatal(e)              => Left(MongoError(e.getMessage))
        }
    }
  }

  private def insert(payload: ReferenceDataPayload, versionId: VersionId, now: Instant): Future[Either[ErrorDetails, Unit]] =
    Future
      .sequence(payload.toIterable(versionId, now).map(listRepository.insertList))
      .map {
        _.foldLeft(Seq.empty[ListRepositoryWriteResult]) {
          case (errors, write: SuccessfulWrite) =>
            logger.info(write.toString)
            errors
          case (errors, write: FailedWrite) =>
            logger.warn(write.toString)
            errors :+ write
        }
      }
      .map {
        case Nil =>
          Right(())
        case writeResults =>
          val message = writeResults
            .map(_.listName.listName)
            .mkString("[services.ingestion.ReferenceDataServiceImpl]: Failed to insert the following lists: ", ", ", "")

          Left(MongoError(message))
      }
}
