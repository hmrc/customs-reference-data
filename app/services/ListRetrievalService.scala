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

package services

import java.time.LocalDate

import javax.inject.Inject
import models._
import play.api.libs.json.JsObject
import play.api.libs.json.JsString
import repositories.ListRepository

import scala.concurrent.ExecutionContext
import scala.concurrent.Future

class ListRetrievalService @Inject() (listRepository: ListRepository)(implicit ec: ExecutionContext) {

  private def getVersion = MetaData("version", LocalDate.of(2020, 11, 5))

  def getList(listName: ListName): Future[Option[ReferenceDataList]] =
    listRepository.getListByName(listName, getVersion).map {
      referenceDataList =>
        if (referenceDataList.nonEmpty) Some(ReferenceDataList(listName, getVersion, referenceDataList)) else None
    }

  def getResourceLinks(metaData: Option[MetaData] = None): Future[Option[ResourceLinks]] =
    listRepository.getAllLists.map {
      list =>
        if (list.nonEmpty) Some(ResourceLinks(_links = buildLinks(list), metaData = metaData)) else None
    }

  private def buildLinks(list: List[GenericListItem]): Map[String, JsObject] = {

    val buildUri: String => String =
      uri => s"/customs-reference-data/$uri"

    val resourceLinks: Seq[Map[String, JsObject]] = list.zipWithIndex.map {
      case (data, index) => Map(s"list${index + 1}" -> JsObject(Seq("href" -> JsString(buildUri(data.listName.listName)))))
    }

    Map("self" -> JsObject(Seq("href" -> JsString(buildUri("lists"))))) ++
      resourceLinks.flatten
  }

}
