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

package services.consumption

import akka.stream.scaladsl.Source
import akka.util.ByteString
import cats.data.OptionT
import cats.implicits._
import javax.inject.Inject
import models._
import play.api.libs.json.JsObject
import play.api.libs.json.Json
import reactivemongo.akkastream.State
import repositories.ListRepository
import repositories.VersionRepository

import scala.concurrent.ExecutionContext
import scala.concurrent.Future

class ListRetrievalService @Inject() (listRepository: ListRepository, versionRepository: VersionRepository)(implicit ec: ExecutionContext) {

  def streamList(listName: ListName): Future[Option[Source[JsObject, Future[State]]]] =
    (
      for {
        versionInformation <- OptionT(versionRepository.getLatest(listName))
        versionedListName = VersionedListName(listName, versionInformation.versionId)
        referenceDataList <- OptionT.liftF(listRepository.getListByNameSource(versionedListName))
      } yield referenceDataList
    ).value

  def jsonFormat(listName: ListName, metaData: MetaData): String =
    s"""
       |{
       |   "_links": {
       |     "self": {
       |       "href": "customs-reference-data/lists/${listName.listName}"
       |     }
       |   },
       |   "id": "${listName.listName}",
       |   "data": [
       |""".stripMargin

  def sourceTransform(listName: ListName): Future[Option[Source[ByteString, Future[State]]]] =
    (
      for {
        referenceDataList <- OptionT(streamList(listName))
        getMeta           <- OptionT(getMetaData(listName))
        jsonFormatted = jsonFormat(listName, getMeta)
      } yield referenceDataList
        .map(r => ByteString(Json.stringify(r)))
        .intersperse(ByteString(jsonFormatted), ByteString(","), ByteString("]}"))
    ).value

  def getMetaData(listName: ListName): Future[Option[MetaData]] =
    versionRepository.getLatest(listName).map(_.map(MetaData(_)))

  def getList(listName: ListName): Future[Option[ReferenceDataList]] =
    (for {
      versionInformation <- OptionT(versionRepository.getLatest(listName))
      versionedListName = VersionedListName(listName, versionInformation.versionId)
      referenceDataList <- OptionT.liftF(listRepository.getListByName(versionedListName))
      if referenceDataList.nonEmpty
    } yield {
      val metaData: MetaData = MetaData(versionInformation)
      ReferenceDataList(listName, metaData, referenceDataList)
    }).value

  def getResourceLinks(): Future[Option[ResourceLinks]] =
    versionRepository.getLatestListNames().map {
      listNames =>
        if (listNames.nonEmpty)
          Some(ResourceLinks(listNames))
        else
          None
    }
}
