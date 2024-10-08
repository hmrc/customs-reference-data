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

package services.consumption

import models._
import org.apache.pekko.NotUsed
import org.apache.pekko.stream.scaladsl.Source
import play.api.libs.json.JsObject
import repositories.ListRepository
import repositories.VersionRepository

import javax.inject.Inject
import scala.concurrent.ExecutionContext
import scala.concurrent.Future

class ListRetrievalService @Inject() (
  listRepository: ListRepository,
  versionRepository: VersionRepository
)(implicit ec: ExecutionContext) {

  def getStreamedList(listName: ListName, versionId: VersionId, filter: Option[FilterParams]): Source[JsObject, NotUsed] =
    listRepository.getListByName(listName, versionId, filter).via(ProjectEmbeddedJsonFlow(listName).project)

  def getLatestVersion(listName: ListName): Future[Option[VersionInformation]] =
    versionRepository.getLatest(listName)

  def getResourceLinks: Future[Option[ResourceLinks]] =
    versionRepository.getLatestListNames.map {
      listNames =>
        if (listNames.nonEmpty)
          Some(ResourceLinks(listNames))
        else
          None
    }
}
