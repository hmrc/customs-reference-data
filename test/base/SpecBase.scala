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

package base

import org.mongodb.scala.bson.BsonDocument
import org.mongodb.scala.model.IndexModel
import org.scalatest.concurrent.IntegrationPatience
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatest.EitherValues
import org.scalatest.OptionValues
import org.scalatestplus.mockito.MockitoSugar
import play.api.inject.guice.GuiceApplicationBuilder

import java.io.ByteArrayOutputStream
import java.time.LocalDate
import java.time.ZoneOffset
import java.util.Base64
import java.util.concurrent.TimeUnit
import java.util.zip.GZIPOutputStream

trait SpecBase extends AnyFreeSpec with Matchers with OptionValues with EitherValues with ScalaFutures with IntegrationPatience with MockitoSugar {

  type AppFunction = GuiceApplicationBuilder => GuiceApplicationBuilder

  val baseApplicationBuilder: AppFunction = identity

  def compress(input: Array[Byte]): Array[Byte] = {
    val bos  = new ByteArrayOutputStream(input.length)
    val gzip = new GZIPOutputStream(bos)
    gzip.write(input)
    gzip.close()
    val compressed = bos.toByteArray
    bos.close()
    compressed
  }

  def encode(input: Array[Byte]): Array[Byte] = Base64.getEncoder.encode(input)

  implicit class RichLocalDate(date: LocalDate) {

    def toEpochMilli: String =
      date.atStartOfDay.atZone(ZoneOffset.UTC).toInstant.toEpochMilli.toString
  }

  implicit class RichIndexModel(indexModel: IndexModel) {

    def tupled(): (String, BsonDocument, Option[Long]) = {
      val options = indexModel.getOptions
      (
        options.getName,
        indexModel.getKeys.toBsonDocument,
        options.getExpireAfter(TimeUnit.SECONDS) match {
          case null => None
          case x    => Some(x)
        }
      )
    }
  }
}
