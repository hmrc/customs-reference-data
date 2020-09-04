/*
 * Copyright 2019 HM Revenue & Customs
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

package repositories

import com.typesafe.config.ConfigFactory
import org.scalatest._
import play.api.Application
import play.api.Configuration
import reactivemongo.api.MongoConnection.ParsedURI
import reactivemongo.api._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object MongoSuite extends OptionValues {

  private lazy val config = Configuration(
    ConfigFactory.load(
      System.getProperty(
        "config.resource"
      )
    )
  )

  lazy val connection: Future[(ParsedURI, MongoConnection)] =
    for {
      parsedUri   <- MongoConnection.fromString(config.get[String]("mongodb.uri"))
      connnection <- AsyncDriver().connect(parsedUri)
    } yield (parsedUri, connnection)
}

trait MongoSuite extends BeforeAndAfterAll {
  self: TestSuite =>

  def started(app: Application): Future[Unit] =
    Future
      .sequence(
        Seq(
          app.injector.instanceOf[ListCollectionIndexManager].started,
          app.injector.instanceOf[VersionCollectionIndexManager].started
        )
      )
      .map(_ => ())

  def database: Future[DefaultDB] =
    MongoSuite.connection.flatMap {
      case (uri, connection) =>
        connection.database(uri.db.get)
    }

}
