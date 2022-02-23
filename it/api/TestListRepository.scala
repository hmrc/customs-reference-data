package api

import akka.NotUsed
import akka.stream.scaladsl.Source
import models.GenericListItem
import models.ListName
import models.VersionId
import models.VersionedListName
import play.api.libs.json.JsObject
import repositories.ListRepository
import repositories.ListRepositoryWriteResult
import uk.gov.hmrc.mongo.MongoComponent

import javax.inject.Inject
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class TestListRepository @Inject() (mongoComponent: MongoComponent) extends ListRepository(mongoComponent) {
  override def getListByName(listNameDetails: VersionedListName): Future[Source[JsObject, NotUsed]] = super.getListByName(listNameDetails)

  override def getListNames(version: VersionId): Future[Seq[ListName]] = super.getListNames(version)

  override def insertList(list: Seq[GenericListItem]): Future[ListRepositoryWriteResult] = {
    Thread.sleep(1500) // simulate a delay in inserting data
    super.insertList(list)
  }
}
