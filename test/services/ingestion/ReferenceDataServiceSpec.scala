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
import org.scalatest.TestData
import org.scalatestplus.play.guice.GuiceOneAppPerTest
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.Application
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import repositories._
import services.consumption.TimeService

import java.time.LocalDateTime
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ReferenceDataServiceSpec extends SpecBase with ScalaCheckDrivenPropertyChecks with GuiceOneAppPerTest with ScalaCheckPropertyChecks {

  private val mockValidationService: SchemaValidationService = mock[SchemaValidationService]
  private val mockTimeService: TimeService                   = mock[TimeService]

  private val now: LocalDateTime = LocalDateTime.now()

  when(mockTimeService.now()).thenReturn(now)

  override def newAppForTest(testData: TestData): Application =
    new GuiceApplicationBuilder()
      .overrides(bind[SchemaValidationService].toInstance(mockValidationService))
      .build()

  "insert" - {
    "reports the processing as successful if all lists are successfully saved" in {
      val numberOfLists = 2
      forAll(genReferenceDataListsPayload(numberOfLists), arbitrary[ApiDataSource]) {
        (payload, apiDataSource) =>
          val listRepository    = mock[NewListRepository]
          val versionIdProducer = mock[VersionIdProducer]

          val versionId = VersionId("1")

          when(versionIdProducer.apply()).thenReturn(versionId)
          when(listRepository.insertList(any())).thenReturn(Future.successful(SuccessfulWrite))

          val versionRepository = mock[VersionRepository]
          val validationService = mock[SchemaValidationService]

          when(versionRepository.save(eqTo(versionId), any(), any(), any(), any())).thenReturn(Future.successful(true))

          val service = new ReferenceDataServiceImpl(listRepository, versionRepository, validationService, versionIdProducer, mockTimeService)

          service.insert(apiDataSource, payload).futureValue mustBe None

          verify(listRepository, times(numberOfLists)).insertList(any())
          verify(versionRepository, times(1)).save(eqTo(versionId), any(), any(), eqTo(payload.listNames), eqTo(now))
      }
    }

    "reports the processing as a having failures when there is a FailedWrite" in {
      val numberOfLists = 2
      forAll(genReferenceDataListsPayload(numberOfLists), arbitrary[ApiDataSource]) {
        (payload, apiDataSource) =>
          val listRepository    = mock[NewListRepository]
          val versionIdProducer = mock[VersionIdProducer]

          val versionId = VersionId("1")

          when(versionIdProducer.apply()).thenReturn(versionId)

          val failedListName = payload.toIterable(versionId, mockTimeService.now()).toList(1).head.listName

          when(listRepository.insertList(any()))
            .thenReturn(Future.successful(SuccessfulWrite))
            .thenReturn(Future.successful(FailedWrite(failedListName)))

          val versionRepository = mock[VersionRepository]
          val validationService = mock[SchemaValidationService]

          when(versionRepository.save(eqTo(versionId), any(), any(), any(), any())).thenReturn(Future.successful(true))

          val service = new ReferenceDataServiceImpl(listRepository, versionRepository, validationService, versionIdProducer, mockTimeService)

          val expectedError = WriteError(
            s"Failed to insert the following lists: ${failedListName.listName}"
          )

          service.insert(apiDataSource, payload).futureValue.value mustBe expectedError

          verify(listRepository, times(numberOfLists)).insertList(any())
      }
    }

    "reports the processing as a having failures when there all failure" in {
      val numberOfLists = 3
      forAll(genReferenceDataListsPayload(numberOfLists), arbitrary[ApiDataSource]) {
        (payload, apiDataSource) =>
          val listRepository    = mock[NewListRepository]
          val versionIdProducer = mock[VersionIdProducer]

          val versionId = VersionId("1")

          when(versionIdProducer.apply()).thenReturn(versionId)

          val listOfListOfItems = payload.toIterable(versionId, mockTimeService.now()).toList
          val failedListName1   = listOfListOfItems.head.head.listName
          val failedListName2   = listOfListOfItems(1).head.listName
          val failedListName3   = listOfListOfItems(2).head.listName

          when(listRepository.insertList(any()))
            .thenReturn(Future.successful(FailedWrite(failedListName1)))
            .thenReturn(Future.successful(FailedWrite(failedListName2)))
            .thenReturn(Future.successful(FailedWrite(failedListName3)))

          val versionRepository = mock[VersionRepository]
          val validationService = mock[SchemaValidationService]

          when(versionRepository.save(eqTo(versionId), any(), any(), any(), any())).thenReturn(Future.successful(true))

          val service = new ReferenceDataServiceImpl(listRepository, versionRepository, validationService, versionIdProducer, mockTimeService)

          val expectedError = WriteError(
            s"Failed to insert the following lists: ${failedListName1.listName}, ${failedListName2.listName}, ${failedListName3.listName}"
          )

          service.insert(apiDataSource, payload).futureValue.value mustBe expectedError

          verify(listRepository, times(numberOfLists)).insertList(any())
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

      result mustBe testJson
    }

    "must return error when a json validation error occurs" in {

      when(mockValidationService.validate(any(), any())).thenReturn(Left(OtherError("Json failed")))

      val service        = app.injector.instanceOf[ReferenceDataService]
      val testJsonSchema = app.injector.instanceOf[TestJsonSchema]

      val result = service.validate(testJsonSchema, testJson).left.value

      result mustBe OtherError("Json failed")
    }
  }
}
