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

import base.SpecBase
import cats.data.EitherT
import generators.ModelArbitraryInstances._
import generators.ModelGenerators.genReferenceDataListsPayload
import models.ApiDataSource
import models.ErrorDetails
import models.OtherError
import models.VersionId
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
import repositories.SuccessState
import repositories.VersionIdProducer
import repositories.v2.ListRepository
import repositories.v2.VersionRepository
import services.consumption.TimeService
import services.ingestion.SchemaValidationService
import services.ingestion.TestJsonSchema

import java.time.Instant
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ReferenceDataServiceSpec extends SpecBase with ScalaCheckDrivenPropertyChecks with GuiceOneAppPerTest with ScalaCheckPropertyChecks {

  private val mockValidationService: SchemaValidationService = mock[SchemaValidationService]
  private val mockTimeService: TimeService                   = mock[TimeService]

  private val now: Instant = Instant.now()

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
          val versionId = VersionId("1")

          val listRepository: ListRepository             = mock[ListRepository]
          val versionIdProducer: VersionIdProducer       = mock[VersionIdProducer]
          val versionRepository: VersionRepository       = mock[VersionRepository]
          val validationService: SchemaValidationService = mock[SchemaValidationService]

          when(versionIdProducer.apply()).thenReturn(versionId)

          when(listRepository.insertList(any()))
            .thenReturn(EitherT.rightT[Future, ErrorDetails](SuccessState))

          when(listRepository.deleteList(any(), any()))
            .thenReturn(EitherT.rightT[Future, ErrorDetails](SuccessState))

          when(versionRepository.deleteListVersion(any(), any()))
            .thenReturn(EitherT.rightT[Future, ErrorDetails](SuccessState))

          when(versionRepository.save(eqTo(versionId), any(), any(), any(), any()))
            .thenReturn(EitherT.rightT[Future, ErrorDetails](SuccessState))

          val service = new ReferenceDataServiceImpl(listRepository, versionRepository, validationService, versionIdProducer, mockTimeService)

          service.insert(apiDataSource, payload).value.futureValue mustBe Right(List(SuccessState, SuccessState))

          verify(listRepository, times(numberOfLists)).insertList(any())
          verify(listRepository, times(10)).deleteList(any(), any())
          verify(versionRepository, times(numberOfLists)).deleteListVersion(any(), any())
          verify(versionRepository, times(numberOfLists)).save(eqTo(versionId), any(), any(), any(), eqTo(now))
      }
    }

    "reports the processing as a having failures when there is a FailedWrite" in {
      val numberOfLists = 2
      forAll(genReferenceDataListsPayload(numberOfLists), arbitrary[ApiDataSource]) {
        (payload, apiDataSource) =>
          val listRepository    = mock[ListRepository]
          val versionIdProducer = mock[VersionIdProducer]

          val versionId = VersionId("1")

          when(versionIdProducer.apply()).thenReturn(versionId)

          when(listRepository.insertList(any()))
            .thenReturn(EitherT.leftT[Future, SuccessState.type](OtherError("error")))
            .thenReturn(EitherT.rightT[Future, ErrorDetails](SuccessState))

          val versionRepository = mock[VersionRepository]
          val validationService = mock[SchemaValidationService]

          val service = new ReferenceDataServiceImpl(listRepository, versionRepository, validationService, versionIdProducer, mockTimeService)

          service.insert(apiDataSource, payload).value.futureValue mustBe Left(OtherError("error"))

          verify(listRepository, times(1)).insertList(any())
          verify(listRepository, times(0)).deleteList(any(), any())
          verify(listRepository, times(0)).deleteList(any(), any())
          verify(versionRepository, times(0)).deleteListVersion(any(), any())
      }
    }

    "reports the processing as a having failures when there all failure" in {
      val numberOfLists = 2
      forAll(genReferenceDataListsPayload(numberOfLists), arbitrary[ApiDataSource]) {
        (payload, apiDataSource) =>
          val listRepository    = mock[ListRepository]
          val versionIdProducer = mock[VersionIdProducer]

          val versionId = VersionId("1")

          when(versionIdProducer.apply()).thenReturn(versionId)

          when(listRepository.insertList(any()))
            .thenReturn(EitherT.leftT[Future, SuccessState.type](OtherError("error1")))
            .thenReturn(EitherT.leftT[Future, SuccessState.type](OtherError("error2")))
            .thenReturn(EitherT.leftT[Future, SuccessState.type](OtherError("error3")))
            .thenReturn(EitherT.leftT[Future, SuccessState.type](OtherError("error4")))

          val versionRepository = mock[VersionRepository]
          val validationService = mock[SchemaValidationService]

          val service = new ReferenceDataServiceImpl(listRepository, versionRepository, validationService, versionIdProducer, mockTimeService)

          service.insert(apiDataSource, payload).value.futureValue mustBe Left(OtherError("error1"))

          verify(listRepository, times(1)).insertList(any())
          verify(listRepository, times(0)).deleteList(any(), any())
          verify(listRepository, times(0)).deleteList(any(), any())
          verify(versionRepository, times(0)).deleteListVersion(any(), any())
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
