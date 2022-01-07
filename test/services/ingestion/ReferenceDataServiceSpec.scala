/*
 * Copyright 2022 HM Revenue & Customs
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
import generators.ModelArbitraryInstances._
import generators.ModelGenerators.genReferenceDataListsPayload
import models.ApiDataSource
import models.OtherError
import models.VersionId
import models.WriteError
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.{eq => eqTo}
import org.mockito.Mockito._
import org.scalacheck.Arbitrary.arbitrary
import org.scalatest.OptionValues
import org.scalatest.TestData
import org.scalatestplus.play.guice.GuiceOneAppPerTest
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.Application
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import repositories.FailedWrite
import repositories.PartialWriteFailure
import repositories.SuccessfulWrite
import repositories.ListRepository
import repositories.VersionIdProducer
import repositories.VersionRepository

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
      forAll(genReferenceDataListsPayload(numberOfLists = 2), arbitrary[ApiDataSource]) {
        (payload, apiDataSource) =>
          val repository        = mock[ListRepository]
          val versionIdProducer = mock[VersionIdProducer]

          val versionId = VersionId("1")

          when(versionIdProducer.apply()).thenReturn(versionId)
          when(repository.insertList(any())).thenReturn(Future.successful(SuccessfulWrite))

          val versionRepository = mock[VersionRepository]
          val validationService = mock[SchemaValidationService]

          when(versionRepository.save(eqTo(versionId), any(), any(), any())).thenReturn(Future.successful(versionId))

          val service = new ReferenceDataServiceImpl(repository, versionRepository, validationService, versionIdProducer)

          service.insert(apiDataSource, payload).futureValue mustBe None

          verify(repository, times(2)).insertList(any())
          verify(versionRepository, times(1)).save(eqTo(versionId), any(), any(), eqTo(payload.listNames))
      }
    }

    "reports the processing as a having failures when there is a partial failure" in {
      forAll(genReferenceDataListsPayload(numberOfLists = 2), arbitrary[ApiDataSource]) {
        (payload, apiDataSource) =>
          val repository        = mock[ListRepository]
          val versionIdProducer = mock[VersionIdProducer]

          val versionId = VersionId("1")

          when(versionIdProducer.apply()).thenReturn(versionId)

          val failedListName = payload.toIterable(versionId).toList(1).head.listName

          when(repository.insertList(any()))
            .thenReturn(Future.successful(SuccessfulWrite))
            .thenReturn(Future.successful(PartialWriteFailure(failedListName, Seq(1))))

          val versionRepository = mock[VersionRepository]
          val validationService = mock[SchemaValidationService]

          when(versionRepository.save(eqTo(versionId), any(), any(), any())).thenReturn(Future.successful(versionId))

          val service = new ReferenceDataServiceImpl(repository, versionRepository, validationService, versionIdProducer)

          val expectedError = WriteError(
            s"Failed to insert the following lists: ${failedListName.listName}"
          )

          service.insert(apiDataSource, payload).futureValue.value mustBe expectedError
          verify(repository, times(2)).insertList(any())
      }
    }

    "reports the processing as a having failures when there is a FailedWrite" in {
      forAll(genReferenceDataListsPayload(numberOfLists = 2), arbitrary[ApiDataSource]) {
        (payload, apiDataSource) =>
          val repository        = mock[ListRepository]
          val versionIdProducer = mock[VersionIdProducer]

          val versionId = VersionId("1")

          when(versionIdProducer.apply()).thenReturn(versionId)

          val failedListName = payload.toIterable(versionId).toList(1).head.listName

          when(repository.insertList(any()))
            .thenReturn(Future.successful(SuccessfulWrite))
            .thenReturn(Future.successful(FailedWrite(failedListName)))

          val versionRepository = mock[VersionRepository]
          val validationService = mock[SchemaValidationService]

          when(versionRepository.save(eqTo(versionId), any(), any(), any())).thenReturn(Future.successful(versionId))

          val service = new ReferenceDataServiceImpl(repository, versionRepository, validationService, versionIdProducer)

          val expectedError = WriteError(
            s"Failed to insert the following lists: ${failedListName.listName}"
          )

          service.insert(apiDataSource, payload).futureValue.value mustBe expectedError
          verify(repository, times(2)).insertList(any())
      }
    }

    "reports the processing as a having failures when there all failure" in {
      forAll(genReferenceDataListsPayload(numberOfLists = 3), arbitrary[ApiDataSource]) {
        (payload, apiDataSource) =>
          val repository        = mock[ListRepository]
          val versionIdProducer = mock[VersionIdProducer]

          val versionId = VersionId("1")

          when(versionIdProducer.apply()).thenReturn(versionId)

          val listOfListOfItems = payload.toIterable(versionId).toList
          val failedListName1   = listOfListOfItems.head.head.listName
          val failedListName2   = listOfListOfItems(1).head.listName
          val failedListName3   = listOfListOfItems(2).head.listName

          when(repository.insertList(any()))
            .thenReturn(Future.successful(PartialWriteFailure(failedListName1, Seq(1))))
            .thenReturn(Future.successful(FailedWrite(failedListName2)))
            .thenReturn(Future.successful(PartialWriteFailure(failedListName3, Seq(2, 3))))

          val versionRepository = mock[VersionRepository]
          val validationService = mock[SchemaValidationService]

          when(versionRepository.save(eqTo(versionId), any(), any(), any())).thenReturn(Future.successful(versionId))

          val service = new ReferenceDataServiceImpl(repository, versionRepository, validationService, versionIdProducer)

          val expectedError = WriteError(
            s"Failed to insert the following lists: ${failedListName1.listName}, ${failedListName2.listName}, ${failedListName3.listName}"
          )

          service.insert(apiDataSource, payload).futureValue.value mustBe expectedError
          verify(repository, times(3)).insertList(any())
      }
    }
  }

  "validate" - {

    val testJson = Json.obj("foo" -> "bar")

    "must return JsObject on successful validation" in {

      when(mockValidationService.validate(any(), any())).thenReturn(Right(testJson))

      val service        = app.injector.instanceOf[ReferenceDataService]
      val testJsonSchema = app.injector.instanceOf[TestJsonSchema]

      val result = service.validate(testJsonSchema, testJson).right.get

      result mustBe testJson
    }

    "must return error when a json validation error occurs" in {

      when(mockValidationService.validate(any(), any())).thenReturn(Left(OtherError("Json failed")))

      val service        = app.injector.instanceOf[ReferenceDataService]
      val testJsonSchema = app.injector.instanceOf[TestJsonSchema]

      val result = service.validate(testJsonSchema, testJson).left.get

      result mustBe OtherError("Json failed")
    }
  }
}
