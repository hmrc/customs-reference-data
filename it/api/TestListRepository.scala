package api

import akka.stream.scaladsl.Source
import models.{GenericListItem, ListName, VersionId, VersionedListName}
import play.api.libs.json.JsObject
import repositories.{DefaultListRepository, ListRepository, ListRepositoryWriteResult}

import javax.inject.Inject
import scala.concurrent.Future

class TestListRepository @Inject()(defaultListRepository: DefaultListRepository) extends ListRepository {
  override def getListByName(listNameDetails: VersionedListName): Future[Source[JsObject, Future[_]]] = defaultListRepository.getListByName(listNameDetails)

  override def getListByName(listNameDetails: VersionedListName): Future[Seq[JsObject]] = defaultListRepository.getListByName(listNameDetails)

  override def getListNames(version: VersionId): Future[Seq[ListName]] = defaultListRepository.getListNames(version)

  override def insertList(list: Seq[GenericListItem]): Future[ListRepositoryWriteResult] = {
    Thread.sleep(1500) // simulate a delay in inserting data
    defaultListRepository.insertList(list)
  }
}
