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

import cats.data.OptionT
import cats.implicits._
import javax.inject.Inject
import models._
import repositories.ListRepository
import repositories.VersionRepository

import scala.concurrent.ExecutionContext
import scala.concurrent.Future

class ListRetrievalService @Inject() (listRepository: ListRepository, versionRepository: VersionRepository)(implicit ec: ExecutionContext) {

  private def getVersion = MetaData("version", LocalDate.of(2020, 11, 5))

  def getList(listName: ListName): Future[Option[ReferenceDataList]] =
    listRepository.getListByName(listName, getVersion).map {
      referenceDataList =>
        if (referenceDataList.nonEmpty) Some(ReferenceDataList(listName, getVersion, referenceDataList)) else None
    }

  def getResourceLinks(): Future[Option[ResourceLinks]] = {

    val getResourceLinks = for {
      versionInformation <- OptionT(versionRepository.getLatest)
      genericItemList    <- OptionT.liftF(listRepository.getAllLists(versionInformation.versionId))
      resourceLinks <- OptionT.fromOption[Future] {
        if (genericItemList.nonEmpty) {
          val metaData = MetaData.apply(versionInformation.versionId.versionId, versionInformation.messageInformation.snapshotDate)
          Some(ResourceLinks.apply(genericItemList.map(_.listName), metaData))
        } else
          None
      }
    } yield resourceLinks

    getResourceLinks.value
  }
}
