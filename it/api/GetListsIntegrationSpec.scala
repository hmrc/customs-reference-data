package api

import base.ItSpecBase
import models.ApiDataSource
import models.ListName
import models.VersionId
import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import play.api.Application
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.JsObject
import play.api.libs.json.Json
import play.api.libs.ws.WSClient
import play.api.libs.ws.WSResponse
import repositories.ListRepository
import repositories.VersionRepository
import uk.gov.hmrc.mongo.test.MongoSupport

import java.io.InputStream
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

    dropDatabase()

    val versionId: VersionId = VersionId("test-version-id")

    versionRepo.save(versionId, defaultMessageInformation, ApiDataSource.RefDataFeed, Seq(defaultListName)).futureValue

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
        ws.url(postUrl).post(Json.parse(request))

        Thread.sleep(200) // wait for the request to go in

        val myResponse: WSResponse = ws.url(getListUrl()).get().futureValue

        (Json.parse(myResponse.body) \ "meta" \ "version").as[String] mustBe versionId.versionId
        (Json.parse(myResponse.body) \ "data").as[Seq[JsObject]] mustBe defaultData
      }
    }
  }

}
