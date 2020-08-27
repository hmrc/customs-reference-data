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

package services

import javax.inject.Inject
import models.ReferenceDataPayload
import repositories.ListRepository
import repositories.ListRepository.FailedWrite
import repositories.ListRepository.PartialWriteFailure
import repositories.ListRepository.SuccessfulWrite
import services.ReferenceDataService.DataProcessingResult

import scala.concurrent.ExecutionContext
import scala.concurrent.Future

class ReferenceDataService @Inject() (repository: ListRepository)(implicit ec: ExecutionContext) {
  import services.ReferenceDataService.DataProcessingResult._

  def addNew(payload: ReferenceDataPayload): Future[DataProcessingResult] =
    Future
      .sequence(
        payload.listsNames
          .map(payload.getList)
          .map(x => repository.insertList(x.toGenericListItem))
      )
      .map(_.foldLeft[DataProcessingResult](DataProcessingSuccessful) {
        case (_, SuccessfulWrite)        => DataProcessingSuccessful
        case (_, _: PartialWriteFailure) => DataProcessingFailed
        case (_, _: FailedWrite)         => DataProcessingFailed
      })

}

object ReferenceDataService {

  sealed trait DataProcessingResult

  object DataProcessingResult {
    case object DataProcessingSuccessful extends DataProcessingResult
    case object DataProcessingFailed     extends DataProcessingResult
  }

}
