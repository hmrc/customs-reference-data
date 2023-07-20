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

package utils

import play.api.libs.json.Json.JsValueWrapper
import play.api.libs.json.JsArray
import play.api.libs.json.JsValue
import play.api.libs.json.Json

import java.io.File
import java.io.FileOutputStream
import java.nio.charset.StandardCharsets
import scala.xml.Elem
import scala.xml.XML

object XmlToJsonConverter {

  /**
    *  Create a file in conf/resources called input.xml containing the XML from Europa to be converted to JSON then run:
    *  sbt "runMain utils.XmlToJsonConverter"
    */
  def main(args: Array[String]): Unit = {
    val xml  = readXml()
    val json = convertXmlToJson(xml)
    writeJson(json)
  }

  private def readXml(): Elem = {
    val inputStream = getClass.getResourceAsStream("/resources/input.xml")
    XML.load(inputStream)
  }

  /**
    * Amend XML paths or add fields as appropriate
    */
  private def convertXmlToJson(xml: Elem): JsValue =
    (xml \\ "RDEntry").foldLeft(JsArray()) {
      (acc, entry) =>
        val code        = (entry \ "dataItem").text
        val description = (entry \\ "description").find(_.attributes("lang").map(_.text).contains("en")).map(_.text)

        val fields: Seq[(String, JsValueWrapper)] = Seq(
          "code"        -> Some(code),
          "description" -> description
        ).flatMap {
          case (key, Some(value)) => Some((key, value))
          case _                  => None
        }

        JsArray(acc.value :+ Json.obj(fields: _*))
    }

  private def writeJson(json: JsValue): Unit = {
    val fos = new FileOutputStream(new File("conf/resources/output.json"))
    fos.write(Json.prettyPrint(json).getBytes(StandardCharsets.UTF_8))
  }

}
