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
import generators.ModelArbitraryInstances._
import generators.ModelGenerators.genReferenceDataListsPayload
import models.ApiDataSource
import models.GenericListItem
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
import repositories.ListRepository.PartialWriteFailure
import repositories.ListRepository.SuccessfulWrite
import repositories.ListRepository
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
          val repository = mock[ListRepository]
          when(repository.insertList(any())).thenReturn(Future.successful(SuccessfulWrite))

          val versionId         = VersionId("1")
          val versionRepository = mock[VersionRepository]
          val validationService = mock[SchemaValidationService]

          when(versionRepository.save(any(), any(), any())).thenReturn(Future.successful(versionId))

          val service = new ReferenceDataServiceImpl(repository, versionRepository, validationService)

          service.insert(apiDataSource, payload).futureValue.right.get mustBe (())

          verify(repository, times(2)).insertList(any())
          verify(versionRepository, times(1)).save(any(), any(), eqTo(payload.listNames))
      }
    }

    "reports the processing as failed if any lists are not saved" in {
      forAll(genReferenceDataListsPayload(numberOfLists = 4), arbitrary[ApiDataSource]) {
        (payload, apiDataSource) =>
          val repository = mock[ListRepository]
          val versionId  = VersionId("1")

          val genericListItems1: Seq[GenericListItem] = payload.toIterable(versionId).toList(1)
          val genericListItems2: Seq[GenericListItem] = payload.toIterable(versionId).toList(3)

          when(repository.insertList(any()))
            .thenReturn(Future.successful(SuccessfulWrite))
            .thenReturn(Future.successful(PartialWriteFailure(genericListItems1)))
            .thenReturn(Future.successful(SuccessfulWrite))
            .thenReturn(Future.successful(PartialWriteFailure(genericListItems2)))

          val versionRepository = mock[VersionRepository]
          val validationService = mock[SchemaValidationService]

          when(versionRepository.save(any(), any(), any())).thenReturn(Future.successful(versionId))

          val service = new ReferenceDataServiceImpl(repository, versionRepository, validationService)

          service.insert(apiDataSource, payload).futureValue.left.get mustBe WriteError(
            s"Failed to insert the following lists: ${genericListItems1.head.listName}, ${genericListItems2.head.listName}"
          )

          verify(repository, times(2)).insertList(any())
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
