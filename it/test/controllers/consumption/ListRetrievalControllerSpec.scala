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

package controllers.consumption

import controllers.V2ControllerSpec
import models.ApiDataSource.ColDataFeed
import models._
import play.api.http.Status._
import play.api.libs.json.JsObject
import play.api.libs.json.Json
import repositories.ListRepository
import repositories.VersionRepository

import java.time.Instant
import java.time.LocalDate

class ListRetrievalControllerSpec extends V2ControllerSpec {

  private val versionRepository = app.injector.instanceOf[VersionRepository]
  private val listRepository    = app.injector.instanceOf[ListRepository]

  private val headers = Seq(
    "Accept" -> "application/vnd.hmrc.2.0+json"
  )

  private val snapshotDate = LocalDate.of(2020, 1, 1)

  private val listName           = "CustomsOffices"
  private val messageInformation = MessageInformation("", snapshotDate)
  private val versionId          = VersionId("1")

  private val instant = Instant.ofEpochMilli(1705062384089L)

  private def customsOffice(json: String): GenericListItem =
    GenericListItem(
      listName = ListName(listName),
      messageInformation = messageInformation,
      versionId = versionId,
      data = Json.parse(json).as[JsObject],
      createdOn = instant
    )

  private val customsOffice1 = customsOffice(
    """
      |{
      |  "id" : "GB000001",
      |  "name" : "Customs office 1"
      |}
      |""".stripMargin
  )

  private val customsOffice2 = customsOffice(
    """
      |{
      |  "id" : "GB000002",
      |  "name" : "Customs office 2"
      |}
      |""".stripMargin
  )

  private val customsOffice3 = customsOffice(
    """
      |{
      |  "id" : "GB000003",
      |  "name" : "Customs office 3"
      |}
      |""".stripMargin
  )

  override def beforeEach(): Unit = {
    super.beforeEach()

    versionRepository.save(versionId, messageInformation, ColDataFeed, Seq(ListName(listName)), instant).futureValue
    val list = GenericList(ListName(listName), Seq(customsOffice1, customsOffice2, customsOffice3))
    listRepository.insertList(list).futureValue

    countDocuments mustBe 3
  }

  "v2 list retrieval endpoint" - {
    "when given no values in query param" - {
      val url = s"$baseUrl/customs-reference-data/lists/$listName"

      "must respond with 200 status" in {
        val response =
          wsClient
            .url(url)
            .withHttpHeaders(headers*)
            .get()
            .futureValue

        response.status mustBe OK

        val expectedJson = Json.parse("""
            |{
            |   "_links": {
            |     "self": {
            |       "href": "/customs-reference-data/lists/CustomsOffices"
            |     }
            |   },
            |   "meta": {
            |     "version" : "1",
            |     "snapshotDate" : "2020-01-01"
            |   },
            |   "id": "CustomsOffices",
            |   "data": [
            |     {
            |       "id" : "GB000001",
            |       "name" : "Customs office 1"
            |     },
            |     {
            |       "id" : "GB000002",
            |       "name" : "Customs office 2"
            |     },
            |     {
            |       "id" : "GB000003",
            |       "name" : "Customs office 3"
            |     }
            |   ]
            |}
            |""".stripMargin)

        Json.parse(response.body) mustBe expectedJson
      }
    }

    "when given single value in query param" - {
      val url = s"$baseUrl/customs-reference-data/lists/$listName?data.id=GB000001"

      "must respond with 200 status" in {
        val response =
          wsClient
            .url(url)
            .withHttpHeaders(headers*)
            .get()
            .futureValue

        response.status mustBe OK

        val expectedJson = Json.parse("""
            |{
            |   "_links": {
            |     "self": {
            |       "href": "/customs-reference-data/lists/CustomsOffices?data.id=GB000001"
            |     }
            |   },
            |   "meta": {
            |     "version" : "1",
            |     "snapshotDate" : "2020-01-01"
            |   },
            |   "id": "CustomsOffices",
            |   "data": [
            |     {
            |       "id" : "GB000001",
            |       "name" : "Customs office 1"
            |     }
            |   ]
            |}
            |""".stripMargin)

        Json.parse(response.body) mustBe expectedJson
      }
    }

    "when given multiple values in query param" - {
      val url = s"$baseUrl/customs-reference-data/lists/$listName?data.id=GB000001&data.id=GB000002"

      "must respond with 200 status" in {
        val response =
          wsClient
            .url(url)
            .withHttpHeaders(headers*)
            .get()
            .futureValue

        response.status mustBe OK

        val expectedJson = Json.parse("""
            |{
            |   "_links": {
            |     "self": {
            |       "href": "/customs-reference-data/lists/CustomsOffices?data.id=GB000001&data.id=GB000002"
            |     }
            |   },
            |   "meta": {
            |     "version" : "1",
            |     "snapshotDate" : "2020-01-01"
            |   },
            |   "id": "CustomsOffices",
            |   "data": [
            |     {
            |       "id" : "GB000001",
            |       "name" : "Customs office 1"
            |     },
            |     {
            |       "id" : "GB000002",
            |       "name" : "Customs office 2"
            |     }
            |   ]
            |}
            |""".stripMargin)

        Json.parse(response.body) mustBe expectedJson
      }
    }
  }
}
