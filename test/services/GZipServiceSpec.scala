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

import java.io.ByteArrayOutputStream
import java.util.zip.GZIPOutputStream

import base.SpecBase
import models.ResponseErrorMessage
import org.scalatest.EitherValues
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks
import play.api.libs.json.Json

class GZipServiceSpec extends SpecBase with ScalaCheckDrivenPropertyChecks with EitherValues {

  "decompressArrayByeToJson" - {

    "must" - {

      "return a decompressed Json when given array[Byte] in GZip format" in {

        val validJson =
          """
            |{
            |   "messageInformation": {
            |     "messageID": "74bd0784-8dc9-4eba-a435-9914ace26995",
            |     "snapshotDate": "2020-07-06"
            | }
            |}
            |""".stripMargin

        val compressedJson = compress(validJson.getBytes)
        val result         = GZipService.decompressArrayByteToJson(compressedJson).right.value

        result mustBe Json.parse(validJson)
      }

      "return an error when given invalid Json" in {

        val invalidJson = "Invalid"

        val compressedJson = compress(invalidJson.getBytes)
        val result         = GZipService.decompressArrayByteToJson(compressedJson).left.value

        result mustBe an[ResponseErrorMessage]
      }

      "return an error when given uncompressed array[Byte]" in {

        val validJson =
          """
            |{
            |   "messageInformation": {
            |     "messageID": "74bd0784-8dc9-4eba-a435-9914ace26995",
            |     "snapshotDate": "2020-07-06"
            | }
            |}
            |""".stripMargin

        val uncompressedArrayByte = validJson.getBytes
        val result                = GZipService.decompressArrayByteToJson(uncompressedArrayByte).left.value

        result mustBe an[ResponseErrorMessage]
      }
    }
  }

  def compress(input: Array[Byte]): Array[Byte] = {
    val bos  = new ByteArrayOutputStream(input.length)
    val gzip = new GZIPOutputStream(bos)
    gzip.write(input)
    gzip.close()
    val compressed = bos.toByteArray
    bos.close()
    compressed
  }

}
