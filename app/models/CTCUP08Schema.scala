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

package models

import javax.inject.Inject
import javax.inject.Singleton
import org.leadpony.justify.api.JsonValidationService
import play.api.Environment

@Singleton
class CTCUP08Schema @Inject() (env: Environment, jsonValidationService: JsonValidationService)
    extends SimpleJsonSchemaProvider(env, jsonValidationService)("schemas/CTCUP08.schema.json")
