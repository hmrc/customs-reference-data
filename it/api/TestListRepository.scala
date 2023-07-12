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

import akka.NotUsed
import akka.stream.scaladsl.Source
import config.AppConfig
import models.ListName
import models.GenericListItem
import models.VersionId
import play.api.libs.json.JsObject
import repositories.ListRepositoryWriteResult
import repositories.v1.ListRepository
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
