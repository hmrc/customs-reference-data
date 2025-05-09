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

import base.SpecBase
import generators.ModelArbitraryInstances.*
import generators.ModelGenerators.genReferenceDataListsPayload
import models.*
import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito.*
import org.scalacheck.Arbitrary.arbitrary
import org.scalatest.{BeforeAndAfterEach, TestData}
import org.scalatestplus.play.guice.GuiceOneAppPerTest
import org.scalatestplus.scalacheck.{ScalaCheckDrivenPropertyChecks, ScalaCheckPropertyChecks}
import play.api.Application
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import repositories.*
import services.TimeService

import java.time.Instant
import scala.concurrent.Future

class ReferenceDataServiceSpec
    extends SpecBase
    with ScalaCheckDrivenPropertyChecks
    with GuiceOneAppPerTest
    with ScalaCheckPropertyChecks
    with BeforeAndAfterEach {

  private val mockValidationService: SchemaValidationService = mock[SchemaValidationService]
  private val mockTimeService: TimeService                   = mock[TimeService]
  private val mockVersionRepository                          = mock[VersionRepository]
  private val mockListRepository                             = mock[ListRepository]
  private val mockVersionIdProducer                          = mock[VersionIdProducer]

  private val now: Instant = Instant.now()

  override def newAppForTest(testData: TestData): Application =
    new GuiceApplicationBuilder()
      .overrides(
        bind[SchemaValidationService].toInstance(mockValidationService),
        bind[TimeService].toInstance(mockTimeService),
        bind[VersionRepository].toInstance(mockVersionRepository),
        bind[ListRepository].toInstance(mockListRepository),
        bind[VersionIdProducer].toInstance(mockVersionIdProducer)
      )
      .build()

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockValidationService)
    reset(mockTimeService)
    reset(mockVersionRepository)
    reset(mockListRepository)
    reset(mockVersionIdProducer)

    when(mockTimeService.currentInstant()).thenReturn(now)
  }

  "insert" - {
    "reports the processing as successful if all lists are successfully saved" in {
      val numberOfLists = 2
      forAll(genReferenceDataListsPayload(numberOfLists), arbitrary[ApiDataSource]) {
        (payload, apiDataSource) =>
          beforeEach()

          val versionId1        = VersionId("1")
          val versionId2        = VersionId("2")
          val versionId3        = VersionId("3")
          val expiredVersionIds = Seq(versionId1, versionId2)

          when(mockVersionIdProducer.apply()).thenReturn(versionId3)
          when(mockVersionRepository.save(eqTo(versionId3), any(), any(), any(), any())).thenReturn(Future.successful(Right(())))
          when(mockListRepository.insertList(any())).thenReturn(Future.successful(SuccessfulWrite(ListName("foo"), 1)))
          when(mockVersionRepository.getExpiredVersions(any())).thenReturn(Future.successful(expiredVersionIds))
          when(mockListRepository.remove(eqTo(expiredVersionIds))).thenReturn(Future.successful(Right(())))
          when(mockVersionRepository.remove(eqTo(expiredVersionIds))).thenReturn(Future.successful(Right(())))

          val service = app.injector.instanceOf[ReferenceDataService]

          service.insert(apiDataSource, payload).futureValue.value mustEqual ()

          verify(mockListRepository, times(numberOfLists)).insertList(any())
          verify(mockVersionRepository, times(1)).save(eqTo(versionId3), any(), any(), eqTo(payload.listNames), eqTo(now))
      }
    }

    "reports the processing as having failures when there is a failure" in {
      val numberOfLists = 2
      forAll(genReferenceDataListsPayload(numberOfLists), arbitrary[ApiDataSource]) {
        (payload, apiDataSource) =>
          beforeEach()

          val versionId = VersionId("1")

          val failedListName = payload.toIterable(versionId, mockTimeService.currentInstant()).toList(1).entries.head.listName

          when(mockVersionIdProducer.apply()).thenReturn(versionId)
          when(mockVersionRepository.save(eqTo(versionId), any(), any(), any(), any())).thenReturn(Future.successful(Right(())))

          when(mockListRepository.insertList(any()))
            .thenReturn(Future.successful(SuccessfulWrite(ListName("foo"), 1)))
            .thenReturn(Future.successful(FailedWrite(failedListName, 1)))

          val service = app.injector.instanceOf[ReferenceDataService]

          val expectedError = WriteError(
            s"[services.ingestion.ReferenceDataServiceImpl]: Failed to insert the following lists: ${failedListName.listName}"
          )

          service.insert(apiDataSource, payload).futureValue.left.value mustEqual expectedError

          verify(mockListRepository, times(numberOfLists)).insertList(any())
      }
    }

    "reports the processing as having failures when they all fail" in {
      val numberOfLists = 3
      forAll(genReferenceDataListsPayload(numberOfLists), arbitrary[ApiDataSource]) {
        (payload, apiDataSource) =>
          beforeEach()

          val versionId = VersionId("1")

          val listOfListOfItems = payload.toIterable(versionId, mockTimeService.currentInstant()).toList
          val failedListName1   = listOfListOfItems.head.entries.head.listName
          val failedListName2   = listOfListOfItems(1).entries.head.listName
          val failedListName3   = listOfListOfItems(2).entries.head.listName

          when(mockVersionIdProducer.apply()).thenReturn(versionId)
          when(mockVersionRepository.save(eqTo(versionId), any(), any(), any(), any())).thenReturn(Future.successful(Right(())))

          when(mockListRepository.insertList(any()))
            .thenReturn(Future.successful(FailedWrite(failedListName1, 1)))
            .thenReturn(Future.successful(FailedWrite(failedListName2, 1)))
            .thenReturn(Future.successful(FailedWrite(failedListName3, 1)))

          val service = app.injector.instanceOf[ReferenceDataService]

          val expectedError = WriteError(
            s"[services.ingestion.ReferenceDataServiceImpl]: Failed to insert the following lists: ${failedListName1.listName}, ${failedListName2.listName}, ${failedListName3.listName}"
          )

          service.insert(apiDataSource, payload).futureValue.left.value mustEqual expectedError

          verify(mockListRepository, times(numberOfLists)).insertList(any())
      }
    }

    "reports the processing as having failures when failure to save to version repository" in {
      val numberOfLists = 2
      forAll(genReferenceDataListsPayload(numberOfLists), arbitrary[ApiDataSource]) {
        (payload, apiDataSource) =>
          beforeEach()

          val versionId = VersionId("1")

          val error = WriteError("foo")

          when(mockVersionIdProducer.apply()).thenReturn(versionId)
          when(mockVersionRepository.save(eqTo(versionId), any(), any(), any(), any())).thenReturn(Future.successful(Left(error)))

          val service = app.injector.instanceOf[ReferenceDataService]

          service.insert(apiDataSource, payload).futureValue.left.value mustEqual error
      }
    }

    "reports the processing as having failures when exception thrown saving to version repository" in {
      val numberOfLists = 2
      forAll(genReferenceDataListsPayload(numberOfLists), arbitrary[ApiDataSource]) {
        (payload, apiDataSource) =>
          beforeEach()

          val versionId = VersionId("1")

          val message = "foo"

          when(mockVersionIdProducer.apply()).thenReturn(versionId)
          when(mockVersionRepository.save(eqTo(versionId), any(), any(), any(), any())).thenReturn(Future.failed(new Throwable(message)))

          val service = app.injector.instanceOf[ReferenceDataService]

          service.insert(apiDataSource, payload).futureValue.left.value mustEqual WriteError(message)
      }
    }

    "reports the processing as a having failures when failure to remove from list repository" in {
      val numberOfLists = 2
      forAll(genReferenceDataListsPayload(numberOfLists), arbitrary[ApiDataSource]) {
        (payload, apiDataSource) =>
          beforeEach()

          val versionId1        = VersionId("1")
          val versionId2        = VersionId("2")
          val versionId3        = VersionId("3")
          val expiredVersionIds = Seq(versionId1, versionId2)

          val error = DeleteError("foo")

          when(mockVersionIdProducer.apply()).thenReturn(versionId3)
          when(mockVersionRepository.save(eqTo(versionId3), any(), any(), any(), any())).thenReturn(Future.successful(Right(())))
          when(mockListRepository.insertList(any())).thenReturn(Future.successful(SuccessfulWrite(ListName("foo"), 1)))
          when(mockVersionRepository.getExpiredVersions(any())).thenReturn(Future.successful(expiredVersionIds))
          when(mockListRepository.remove(eqTo(expiredVersionIds))).thenReturn(Future.successful(Left(error)))

          val service = app.injector.instanceOf[ReferenceDataService]

          service.insert(apiDataSource, payload).futureValue.left.value mustEqual error
      }
    }

    "reports the processing as a having failures when exception thrown removing from list repository" in {
      val numberOfLists = 2
      forAll(genReferenceDataListsPayload(numberOfLists), arbitrary[ApiDataSource]) {
        (payload, apiDataSource) =>
          beforeEach()

          val versionId1        = VersionId("1")
          val versionId2        = VersionId("2")
          val versionId3        = VersionId("3")
          val expiredVersionIds = Seq(versionId1, versionId2)

          val message = "foo"

          when(mockVersionIdProducer.apply()).thenReturn(versionId3)
          when(mockVersionRepository.save(eqTo(versionId3), any(), any(), any(), any())).thenReturn(Future.successful(Right(())))
          when(mockListRepository.insertList(any())).thenReturn(Future.successful(SuccessfulWrite(ListName("foo"), 1)))
          when(mockVersionRepository.getExpiredVersions(any())).thenReturn(Future.successful(expiredVersionIds))
          when(mockListRepository.remove(eqTo(expiredVersionIds))).thenReturn(Future.failed(new Throwable(message)))

          val service = app.injector.instanceOf[ReferenceDataService]

          service.insert(apiDataSource, payload).futureValue.left.value mustEqual WriteError(message)
      }
    }

    "reports the processing as a having failures when failure to remove from version repository" in {
      val numberOfLists = 2
      forAll(genReferenceDataListsPayload(numberOfLists), arbitrary[ApiDataSource]) {
        (payload, apiDataSource) =>
          beforeEach()

          val versionId1        = VersionId("1")
          val versionId2        = VersionId("2")
          val versionId3        = VersionId("3")
          val expiredVersionIds = Seq(versionId1, versionId2)

          val error = DeleteError("foo")

          when(mockVersionIdProducer.apply()).thenReturn(versionId3)
          when(mockVersionRepository.save(eqTo(versionId3), any(), any(), any(), any())).thenReturn(Future.successful(Right(())))
          when(mockListRepository.insertList(any())).thenReturn(Future.successful(SuccessfulWrite(ListName("foo"), 1)))
          when(mockVersionRepository.getExpiredVersions(any())).thenReturn(Future.successful(expiredVersionIds))
          when(mockListRepository.remove(eqTo(expiredVersionIds))).thenReturn(Future.successful(Right(())))
          when(mockVersionRepository.remove(eqTo(expiredVersionIds))).thenReturn(Future.successful(Left(error)))

          val service = app.injector.instanceOf[ReferenceDataService]

          service.insert(apiDataSource, payload).futureValue.left.value mustEqual error
      }
    }

    "reports the processing as a having failures when exception thrown removing from version repository" in {
      val numberOfLists = 2
      forAll(genReferenceDataListsPayload(numberOfLists), arbitrary[ApiDataSource]) {
        (payload, apiDataSource) =>
          beforeEach()

          val versionId1        = VersionId("1")
          val versionId2        = VersionId("2")
          val versionId3        = VersionId("3")
          val expiredVersionIds = Seq(versionId1, versionId2)

          val message = "foo"

          when(mockVersionIdProducer.apply()).thenReturn(versionId3)
          when(mockVersionRepository.save(eqTo(versionId3), any(), any(), any(), any())).thenReturn(Future.successful(Right(())))
          when(mockListRepository.insertList(any())).thenReturn(Future.successful(SuccessfulWrite(ListName("foo"), 1)))
          when(mockVersionRepository.getExpiredVersions(any())).thenReturn(Future.successful(expiredVersionIds))
          when(mockListRepository.remove(eqTo(expiredVersionIds))).thenReturn(Future.successful(Right(())))
          when(mockVersionRepository.remove(eqTo(expiredVersionIds))).thenReturn(Future.failed(new Throwable(message)))

          val service = app.injector.instanceOf[ReferenceDataService]

          service.insert(apiDataSource, payload).futureValue.left.value mustEqual WriteError(message)
      }
    }
  }

  "validate" - {

    val testJson = Json.obj("foo" -> "bar")

    "must return JsObject on successful validation" in {

      when(mockValidationService.validate(any(), any())).thenReturn(Right(testJson))

      val service        = app.injector.instanceOf[ReferenceDataService]
      val testJsonSchema = app.injector.instanceOf[TestJsonSchema]

      val result = service.validate(testJsonSchema, testJson).value

      result mustEqual testJson
    }

    "must return error when a json validation error occurs" in {

      when(mockValidationService.validate(any(), any())).thenReturn(Left(OtherError("Json failed")))

      val service        = app.injector.instanceOf[ReferenceDataService]
      val testJsonSchema = app.injector.instanceOf[TestJsonSchema]

      val result = service.validate(testJsonSchema, testJson).left.value

      result mustEqual OtherError("Json failed")
    }
  }
}
