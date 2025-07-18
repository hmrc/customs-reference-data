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

package config

import play.api.Configuration

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppConfig @Inject() (config: Configuration, servicesConfig: MyServicesConfig) {

  lazy val replaceIndexes: Boolean          = config.get[Boolean]("mongodb.replaceIndexes")
  lazy val ttl: Int                         = config.get[Int]("mongodb.timeToLiveInSeconds")
  lazy val incomingAuth: IncomingAuthConfig = config.get[IncomingAuthConfig]("incomingRequestAuth")

  lazy val crdlCacheUrl: String = servicesConfig.fullServiceUrl("crdl-cache")
}
