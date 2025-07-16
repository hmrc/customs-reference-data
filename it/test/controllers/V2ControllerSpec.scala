/*
 * Copyright 2024 HM Revenue & Customs
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

package controllers

import base.ItSpecBase
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.ws.WSClient
import uk.gov.hmrc.mongo.test.MongoSupport
import org.mongodb.scala._

import java.io.File

trait V2ControllerSpec extends ItSpecBase with GuiceOneServerPerSuite with BeforeAndAfterEach with MongoSupport {

  val wsClient: WSClient = app.injector.instanceOf[WSClient]
  val baseUrl: String    = s"http://localhost:$port"

  override def fakeApplication(): Application =
    GuiceApplicationBuilder()
      .configure("metrics.enabled" -> false)
      .build()

  def file(fileName: String) = new File(getClass.getResource(fileName).toURI)

  protected def getTestDatabase: MongoDatabase =
    mongoClient.getDatabase("customs-reference-data-it")

  protected def countDocuments: Long =
    getTestDatabase
      .getCollection("v2-reference-data-lists-new")
      .countDocuments()
      .toFuture()
      .futureValue

  protected def dropTestDatabase(): Unit =
    getTestDatabase
      .drop()
      .toFuture()
      .futureValue

  override def beforeEach(): Unit = {
    super.beforeEach()
    dropTestDatabase()
    countDocuments mustEqual 0
  }
}
