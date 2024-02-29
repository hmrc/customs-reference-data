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

package api

import base.ItSpecBase
import models.ApiDataSource
import models.ListName
import models.VersionId
import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import play.api.Application
import play.api.http.Status.ACCEPTED
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.JsObject
import play.api.libs.json.Json
import play.api.libs.ws.WSClient
import play.api.libs.ws.WSResponse
import repositories.v1.ListRepository
import repositories.v1.VersionRepository
import uk.gov.hmrc.mongo.test.MongoSupport

import java.io.InputStream
import java.time.Instant
import scala.concurrent.ExecutionContext.Implicits.global
import scala.io.Source

class GetListsIntegrationSpec extends ItSpecBase with ConsumptionHelper with GuiceOneServerPerSuite with MongoSupport {

  implicit override lazy val app: Application = GuiceApplicationBuilder()
    .configure()
    .overrides(bind[ListRepository].to[TestListRepository].eagerly())
    .build()

  class Setup() {
    lazy val listRepo: ListRepository       = app.injector.instanceOf[ListRepository]
    lazy val versionRepo: VersionRepository = app.injector.instanceOf[VersionRepository]
    lazy val ws: WSClient                   = app.injector.instanceOf[WSClient]

    lazy val stream: InputStream = app.environment
      .resourceAsStream("data/request.json")
      .get

    lazy val request: String = Source.fromInputStream(stream).mkString

    val postUrl                                          = s"http://localhost:$port/customs-reference-data/reference-data-lists"
    def getListUrl(listName: ListName = defaultListName) = s"http://localhost:$port/customs-reference-data/lists/${listName.listName}"

    listRepo.collection.drop().toFuture().futureValue
    versionRepo.collection.drop().toFuture().futureValue

    val versionId: VersionId = VersionId("test-version-id")
    val now                  = Instant.now()

    versionRepo.save(versionId, defaultMessageInformation, ApiDataSource.RefDataFeed, Seq(defaultListName), now).futureValue

    listRepo.insertList(basicList(versionId)).futureValue

    versionRepo.getLatest(defaultListName).futureValue.map(_.messageInformation) mustBe Some(defaultMessageInformation)
  }

  "ListRetrievalController" - {
    "When no data ingestion is in progress" - {
      "should return a list from the current version" in new Setup {
        val myResponse: WSResponse = ws.url(getListUrl()).get().futureValue

        (Json.parse(myResponse.body) \ "meta" \ "version").as[String] mustBe versionId.versionId
        (Json.parse(myResponse.body) \ "data").as[Seq[JsObject]] mustBe defaultData
      }
    }
    "When get ListName is called while data ingestion is in progress" - {
      "should return a list from the previous version" in new Setup {
        ws.url(postUrl).post(Json.parse(request)) map {
          r =>
            r.status mustBe ACCEPTED

            val myResponse: WSResponse = ws.url(getListUrl()).get().futureValue

            (Json.parse(myResponse.body) \ "meta" \ "version").as[String] mustBe versionId.versionId
            (Json.parse(myResponse.body) \ "data").as[Seq[JsObject]] mustBe defaultData
        }
      }
    }
  }

}
