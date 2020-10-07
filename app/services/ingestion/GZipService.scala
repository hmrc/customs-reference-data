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

package services.ingestion

import java.io.ByteArrayInputStream
import java.util.zip.GZIPInputStream

import models.OtherError

import scala.util.Failure
import scala.util.Success
import scala.util.Try

private[ingestion] object GZipService {

  def decompressArrayByte(arrayByte: Array[Byte]): Either[OtherError, Array[Byte]] =
    Try {
      val inputStream = new GZIPInputStream(new ByteArrayInputStream(arrayByte))
      scala.io.Source.fromInputStream(inputStream).mkString.getBytes
    } match {
      case Success(value)     => Right(value)
      case Failure(exception) => Left(OtherError(exception.getMessage))
    }
}
