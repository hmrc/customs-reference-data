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

package services.consumption.testOnly

import play.api.Configuration

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
private[testOnly] class ResourceConfig @Inject() (config: Configuration) {

  val customsOfficeP5: String =
    config.get[String]("resourceFiles.customsOfficesP5")

  val countryCodesCommonTransit: String =
    config.get[String]("resourceFiles.countryCodesCommonTransit")

  val countryCodesCTC: String =
    config.get[String]("resourceFiles.countryCodesCTC")

  val countryCodesFullList: String =
    config.get[String]("resourceFiles.countryCodesFullList")

  val countryCodesCommunity: String =
    config.get[String]("resourceFiles.countryCodesCommunity")

  val countryCodesForAddress: String =
    config.get[String]("resourceFiles.countryCodesForAddress")

  val countryCustomsOfficeSecurityAgreementArea: String =
    config.get[String]("resourceFiles.countryCustomsOfficeSecurityAgreementArea")

  val countryAddressPostcodeBased: String =
    config.get[String]("resourceFiles.countryAddressPostcodeBased")

  val countryWithoutZip: String =
    config.get[String]("resourceFiles.countryWithoutZip")

  val unLocode: String =
    config.get[String]("resourceFiles.unLocodeExtended")

  val nationality: String =
    config.get[String]("resourceFiles.nationality")

  val previousDocumentType: String =
    config.get[String]("resourceFiles.previousDocumentType")

  val supportingDocumentType: String =
    config.get[String]("resourceFiles.supportingDocumentType")

  val transportDocumentType: String =
    config.get[String]("resourceFiles.transportDocumentType")

  val kindOfPackage: String =
    config.get[String]("resourceFiles.kindOfPackage")

  val kindOfPackageBulk: String =
    config.get[String]("resourceFiles.kindOfPackageBulk")

  val kindOfPackageUnpacked: String =
    config.get[String]("resourceFiles.kindOfPackageUnpacked")

  val additionalReference: String =
    config.get[String]("resourceFiles.additionalReference")

  val additionalInformation: String =
    config.get[String]("resourceFiles.additionalInformation")

  val metric: String =
    config.get[String]("resourceFiles.metric")

  val currencyCode: String =
    config.get[String]("resourceFiles.currencyCode")
}
