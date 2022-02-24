package api

import akka.NotUsed
import akka.stream.scaladsl.Source
import config.AppConfig
import models.GenericListItem
import models.ListName
import models.VersionId
import play.api.libs.json.JsObject
import repositories.ListRepository
import repositories.ListRepositoryWriteResult
import uk.gov.hmrc.mongo.MongoComponent

import javax.inject.Inject
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class TestListRepository @Inject() (
  mongoComponent: MongoComponent,
  config: AppConfig
) extends ListRepository(mongoComponent, config) {

  override def getListByName(listName: ListName, versionId: VersionId): Source[JsObject, NotUsed] =
    super.getListByName(listName, versionId)

  override def insertList(list: Seq[GenericListItem]): Future[ListRepositoryWriteResult] = {
    Thread.sleep(1500) // simulate a delay in inserting data
    super.insertList(list)
  }
}
