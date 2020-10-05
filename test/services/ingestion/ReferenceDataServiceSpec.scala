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

import base.SpecBase
import generators.ModelGenerators.genReferenceDataListsPayload
import models.OtherError
import models.VersionId
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.{eq => eqTo}
import org.mockito.Mockito._
import org.scalatest.OptionValues
import org.scalatest.TestData
import org.scalatestplus.play.guice.GuiceOneAppPerTest
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.Application
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import repositories.ListRepository
import repositories.VersionRepository
import repositories.ListRepository.PartialWriteFailure
import repositories.ListRepository.SuccessfulWrite
import services.ingestion.ReferenceDataService.DataProcessingResult.DataProcessingFailed
import services.ingestion.ReferenceDataService.DataProcessingResult.DataProcessingSuccessful

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ReferenceDataServiceSpec extends SpecBase with ScalaCheckDrivenPropertyChecks with GuiceOneAppPerTest with ScalaCheckPropertyChecks with OptionValues {

  val mockVersionRepository: VersionRepository       = mock[VersionRepository]
  val mockValidationService: SchemaValidationService = mock[SchemaValidationService]
  val mockListRepository: ListRepository             = mock[ListRepository]

  override def newAppForTest(testData: TestData): Application =
    new GuiceApplicationBuilder()
      .overrides(bind[VersionRepository].toInstance(mockVersionRepository))
      .overrides(bind[SchemaValidationService].toInstance(mockValidationService))
      .overrides(bind[ListRepository].toInstance(mockListRepository))
      .build()

  "insert" - {
    "reports the processing as successful if all lists are successfully saved" in {
      forAll(genReferenceDataListsPayload(numberOfLists = 2)) {
        payload =>
          val repository = mock[ListRepository]
          when(repository.insertList(any())).thenReturn(Future.successful(SuccessfulWrite))

          val versionId         = VersionId("1")
          val versionRepository = mock[VersionRepository]
          val validationService = mock[SchemaValidationService]

          when(versionRepository.save(any(), any())).thenReturn(Future.successful(versionId))

          val service = new ReferenceDataServiceImpl(repository, versionRepository, validationService)

          service.insert(payload).futureValue mustBe DataProcessingSuccessful

          verify(repository, times(2)).insertList(any())
          verify(versionRepository, times(1)).save(any(), eqTo(payload.listNames.toSet))
      }
    }

    "reports the processing as failed if any lists are not saved" in {
      forAll(genReferenceDataListsPayload(numberOfLists = 2)) {
        payload =>
          val repository = mock[ListRepository]
          when(repository.insertList(any()))
            .thenReturn(Future.successful(SuccessfulWrite))
            .thenReturn(Future.successful(PartialWriteFailure(Seq.empty)))

          val versionId         = VersionId("1")
          val versionRepository = mock[VersionRepository]
          val validationService = mock[SchemaValidationService]

          when(versionRepository.save(any(), any())).thenReturn(Future.successful(versionId))

          val service = new ReferenceDataServiceImpl(repository, versionRepository, validationService)

          service.insert(payload).futureValue mustBe DataProcessingFailed

          verify(repository, times(2)).insertList(any())
      }
    }

  }

  "validateAndDecompress" - {

    val testJson       = Json.obj("foo" -> "bar")
    val compressedJson = compress(testJson.toString.getBytes)

    "must return compressed and validated Array[Byte] on success" in {

      when(mockValidationService.validate(any(), any())).thenReturn(Right(testJson))

      val service        = app.injector.instanceOf[ReferenceDataService]
      val testJsonSchema = app.injector.instanceOf[TestJsonSchema]

      val result = service.validateAndDecompress(testJsonSchema, compressedJson).right.get

      result mustBe testJson
    }

    "must return error when given body not in Gzip format" in {

      val invalidArrayByte = testJson.toString.getBytes

      val service        = app.injector.instanceOf[ReferenceDataService]
      val testJsonSchema = app.injector.instanceOf[TestJsonSchema]

      val result = service.validateAndDecompress(testJsonSchema, invalidArrayByte).left.get

      result mustBe OtherError("Not in GZIP format")
    }

    "must return error when a json validation error occurs" in {

      when(mockValidationService.validate(any(), any())).thenReturn(Left(OtherError("Json failed")))

      val service        = app.injector.instanceOf[ReferenceDataService]
      val testJsonSchema = app.injector.instanceOf[TestJsonSchema]

      val result = service.validateAndDecompress(testJsonSchema, compressedJson).left.get

      result mustBe OtherError("Json failed")
    }
  }
}
