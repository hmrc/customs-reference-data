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

import models.ApiDataSource._
import models.v2.CodeList
import play.api.libs.json._
import services.TimeService
import services.UUIDService

import javax.inject.Inject
import scala.xml.NodeSeq

sealed trait XmlToJsonConverter {

  val uuidService: UUIDService

  val timeService: TimeService

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
                val values = codeList.json(entry)
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
      "messageID"    -> uuidService.randomUUID(),
      "snapshotDate" -> timeService.currentDate()
    )

    Json.obj(
      "messageInformation" -> messageInformation,
      "lists"              -> lists
    )
  }
}

object XmlToJsonConverter {

  class ReferenceDataListXmlToJsonConverter @Inject() (
    override val uuidService: UUIDService,
    override val timeService: TimeService
  ) extends XmlToJsonConverter {
    override def predicate(codeList: CodeList): Boolean = codeList.source == RefDataFeed
  }

  class CustomsOfficeListXmlToJsonConverter @Inject() (
    override val uuidService: UUIDService,
    override val timeService: TimeService
  ) extends XmlToJsonConverter {
    override def predicate(codeList: CodeList): Boolean = codeList.source == ColDataFeed
  }
}
