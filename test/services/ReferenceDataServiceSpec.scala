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

import base.SpecBase
import generators.ModelGenerators.genReferenceDataPayload
import repositories.ListRepository
import org.mockito.Mockito._
import org.mockito.ArgumentMatchers.any
import repositories.ListRepository.PartialWriteFailure
import repositories.ListRepository.SuccessfulWrite
import services.ReferenceDataService.DataProcessingResult._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ReferenceDataServiceSpec extends SpecBase {

  "addNew" - {
    "reports the processing as successful if all lists are successfully saved" in {
      val payload = genReferenceDataPayload(numberOfLists = 2).sample.value

      val repository = mock[ListRepository]
      when(repository.insertList(any())).thenReturn(Future.successful(SuccessfulWrite))

      val service = new ReferenceDataService(repository)

      service.insert(payload).futureValue mustBe DataProcessingSuccessful

      verify(repository, times(2)).insertList(any())
    }

    "reports the processing as failed if any lists are not saved" in {
      val payload = genReferenceDataPayload(numberOfLists = 2).sample.value

      val repository = mock[ListRepository]
      when(repository.insertList(any()))
        .thenReturn(Future.successful(SuccessfulWrite))
        .thenReturn(Future.successful(PartialWriteFailure(Seq.empty)))

      val service = new ReferenceDataService(repository)

      service.insert(payload).futureValue mustBe DataProcessingFailed

      verify(repository, times(2)).insertList(any())
    }

  }
}
