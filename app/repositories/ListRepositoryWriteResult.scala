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

package repositories

import models.GenericList
import models.ListName

sealed trait ListRepositoryWriteResult {
  val listName: ListName
  val numberOfListEntries: Int
}

case class SuccessfulWrite(listName: ListName, numberOfListEntries: Int) extends ListRepositoryWriteResult {

  override def toString: String = s"Successfully saved $numberOfListEntries entries to $listName"
}

object SuccessfulWrite {

  def apply(list: GenericList): SuccessfulWrite =
    new SuccessfulWrite(list.name, list.entries.length)
}

case class FailedWrite(listName: ListName, numberOfListEntries: Int) extends ListRepositoryWriteResult {

  override def toString: String = s"Failed to save $numberOfListEntries entries to $listName"
}

object FailedWrite {

  def apply(list: GenericList): FailedWrite =
    new FailedWrite(list.name, list.entries.length)
}
