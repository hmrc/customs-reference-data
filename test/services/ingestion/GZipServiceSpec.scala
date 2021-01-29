/*
 * Copyright 2021 HM Revenue & Customs
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

package services.ingestion

import base.SpecBase
import models.OtherError
import org.scalatest.EitherValues
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks

class GZipServiceSpec extends SpecBase with ScalaCheckDrivenPropertyChecks with EitherValues {

  "decompressArrayByeToJson" - {

    "must" - {

      "return a decompressed array[Byte] from GZip format" in {

        val testString = "Test ArrayByte"

        val compressedValue = compress(testString.getBytes)
        val result          = GZipService.decompressArrayByte(compressedValue).right.value

        val decompressedResult = result.map(_.toChar).mkString

        decompressedResult mustBe testString
      }

      "return an error when given uncompressed array[Byte]" in {

        val testString = "Test ArrayByte"

        val uncompressedArrayByte = testString.getBytes
        val result                = GZipService.decompressArrayByte(uncompressedArrayByte).left.value

        result mustBe an[OtherError]
      }
    }
  }

}
