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

package controllers.ingestion.v2.testOnly

import models.ApiDataSource._
import play.api.libs.json.Json.JsValueWrapper
import play.api.libs.json._

import java.time.LocalDate
import java.util.UUID
import scala.xml.NodeSeq

sealed trait XmlToJsonConverter {

  def flatten(fields: Seq[(String, Option[JsValueWrapper])]): Seq[(String, JsValueWrapper)] =
    fields.flatMap {
      case (key, Some(value)) => Some((key, value))
      case _                  => None
    }

  def predicate(codeList: CodeList): Boolean

  def convert(xml: NodeSeq): JsValue = {
    val lists = (xml \\ "RDEntity").foldLeft[JsObject](Json.obj()) {
      (acc, entry) =>
        val listName = (entry \ "@name").text
        val codeList = CodeList(listName)
        codeList match {
          case Some(codeList) if predicate(codeList) =>
            val listEntries = (entry \\ "RDEntry").foldLeft(JsArray()) {
              (acc, entry) =>
                val values = codeList
                  .fields(entry)
                  .map {
                    _.flatMap {
                      case (key, Some(value)) => Some((key, value))
                      case _                  => None
                    }
                  }
                  .map {
                    fields => Json.obj(fields: _*)
                  }
                JsArray(acc.value ++ values)
            }

            val list = Json.obj(
              "listName"    -> listName,
              "listEntries" -> listEntries
            )

            acc + (codeList.name -> list)
          case _ =>
            acc
        }
    }

    val messageInformation = Json.obj(
      "messageID"    -> UUID.randomUUID(),
      "snapshotDate" -> LocalDate.now()
    )

    Json.obj(
      "messageInformation" -> messageInformation,
      "lists"              -> lists
    )
  }
}

object XmlToJsonConverter {

  class ReferenceDataListXmlToJsonConverter extends XmlToJsonConverter {
    override def predicate(codeList: CodeList): Boolean = codeList.source == RefDataFeed
  }

  class CustomsOfficeListXmlToJsonConverter extends XmlToJsonConverter {
    override def predicate(codeList: CodeList): Boolean = codeList.source == ColDataFeed
  }
}
