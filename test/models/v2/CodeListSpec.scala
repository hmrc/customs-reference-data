/*
 * Copyright 2024 HM Revenue & Customs
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

package models.v2

import base.SpecBase
import models.ApiDataSource._
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.libs.json.JsArray
import play.api.libs.json.Json

class CodeListSpec extends SpecBase with ScalaCheckPropertyChecks {

  "CodeList" - {
    "when valid code list" - {
      "AdditionalInformation" in {
        val xml =
          <ns3:RDEntry>
            <ns4:RDEntryStatus>
              <ns5:state>valid</ns5:state>
              <ns5:activeFrom>2024-06-20</ns5:activeFrom>
            </ns4:RDEntryStatus>
            <ns4:dataItem name="AdditionalInformationCode">00200</ns4:dataItem>
            <ns4:LsdList>
              <ns7:description lang="en">Several occurrences of documents and parties</ns7:description>
            </ns4:LsdList>
          </ns3:RDEntry>

        val result = CodeList("AdditionalInformation").value

        result.name mustBe "AdditionalInformation"

        result.source mustBe RefDataFeed

        result.json(xml) mustBe Seq(
          Json.obj(
            "state"       -> "valid",
            "activeFrom"  -> "2024-06-20",
            "code"        -> "00200",
            "description" -> "Several occurrences of documents and parties"
          )
        )
      }

      "AdditionalReference" in {
        val xml =
          <ns3:RDEntry>
            <ns4:RDEntryStatus>
              <ns5:state>valid</ns5:state>
              <ns5:activeFrom>2024-01-18</ns5:activeFrom>
            </ns4:RDEntryStatus>
            <ns4:dataItem name="DocumentType">C651</ns4:dataItem>
            <ns4:LsdList>
              <ns7:description lang="en">Electronic administrative document (e-AD), as referred to in Article 3(1) of Reg. (EC) No 684/2009</ns7:description>
            </ns4:LsdList>
          </ns3:RDEntry>

        val result = CodeList("AdditionalReference").value

        result.name mustBe "AdditionalReference"

        result.source mustBe RefDataFeed

        result.json(xml) mustBe Seq(
          Json.obj(
            "state"        -> "valid",
            "activeFrom"   -> "2024-01-18",
            "documentType" -> "C651",
            "description"  -> "Electronic administrative document (e-AD), as referred to in Article 3(1) of Reg. (EC) No 684/2009"
          )
        )
      }

      "AdditionalSupplyChainActorRoleCode" in {
        val xml =
          <ns3:RDEntry>
            <ns4:RDEntryStatus>
              <ns5:state>valid</ns5:state>
              <ns5:activeFrom>2024-07-09</ns5:activeFrom>
            </ns4:RDEntryStatus>
            <ns4:dataItem name="Role">CS</ns4:dataItem>
            <ns4:LsdList>
              <ns7:description lang="en">Consolidator</ns7:description>
            </ns4:LsdList>
          </ns3:RDEntry>

        val result = CodeList("AdditionalSupplyChainActorRoleCode").value

        result.name mustBe "AdditionalSupplyChainActorRoleCode"

        result.source mustBe RefDataFeed

        result.json(xml) mustBe Seq(
          Json.obj(
            "state"       -> "valid",
            "activeFrom"  -> "2024-07-09",
            "role"        -> "CS",
            "description" -> "Consolidator"
          )
        )
      }

      "AuthorisationTypeDeparture" in {
        val xml =
          <ns3:RDEntry>
            <ns4:RDEntryStatus>
              <ns5:state>valid</ns5:state>
              <ns5:activeFrom>2024-07-09</ns5:activeFrom>
            </ns4:RDEntryStatus>
            <ns4:dataItem name="AuthorisationType">C521</ns4:dataItem>
            <ns4:LsdList>
              <ns7:description lang="en">ACR - Authorisation for the status of...</ns7:description>
            </ns4:LsdList>
          </ns3:RDEntry>

        val result = CodeList("AuthorisationTypeDeparture").value

        result.name mustBe "AuthorisationTypeDeparture"

        result.source mustBe RefDataFeed

        result.json(xml) mustBe Seq(
          Json.obj(
            "state"       -> "valid",
            "activeFrom"  -> "2024-07-09",
            "code"        -> "C521",
            "description" -> "ACR - Authorisation for the status of..."
          )
        )
      }

      "AuthorisationTypeDestination" in {
        val xml =
          <ns3:RDEntry>
            <ns4:RDEntryStatus>
              <ns5:state>valid</ns5:state>
              <ns5:activeFrom>2024-07-09</ns5:activeFrom>
            </ns4:RDEntryStatus>
            <ns4:dataItem name="AuthorisationType">C520</ns4:dataItem>
            <ns4:LsdList>
              <ns7:description lang="en">ACT - Authorisation for the status of...</ns7:description>
            </ns4:LsdList>
          </ns3:RDEntry>

        val result = CodeList("AuthorisationTypeDestination").value

        result.name mustBe "AuthorisationTypeDestination"

        result.source mustBe RefDataFeed

        result.json(xml) mustBe Seq(
          Json.obj(
            "state"       -> "valid",
            "activeFrom"  -> "2024-07-09",
            "code"        -> "C520",
            "description" -> "ACT - Authorisation for the status of..."
          )
        )
      }

      "BusinessRejectionTypeDepExp" in {
        val xml =
          <ns3:RDEntry>
            <ns4:RDEntryStatus>
              <ns5:state>valid</ns5:state>
              <ns5:activeFrom>2024-06-20</ns5:activeFrom>
            </ns4:RDEntryStatus>
            <ns4:dataItem name="BusinessRejectionTypeDepExpCode">013</ns4:dataItem>
            <ns4:LsdList>
              <ns7:description lang="en">Amendment rejection</ns7:description>
            </ns4:LsdList>
          </ns3:RDEntry>

        val result = CodeList("BusinessRejectionTypeDepExp").value

        result.name mustBe "BusinessRejectionTypeDepExp"

        result.source mustBe RefDataFeed

        result.json(xml) mustBe Seq(
          Json.obj(
            "state"       -> "valid",
            "activeFrom"  -> "2024-06-20",
            "code"        -> "013",
            "description" -> "Amendment rejection"
          )
        )
      }

      "CUSCode" in {
        val xml =
          <ns3:RDEntry>
            <ns4:RDEntryStatus>
              <ns5:state>valid</ns5:state>
              <ns5:activeFrom>2021-02-22</ns5:activeFrom>
            </ns4:RDEntryStatus>
            <ns4:dataItem name="CUSCode">0010001-6</ns4:dataItem>
            <ns4:LsdList>
              <ns7:description lang="en">https://ec.europa.eu/taxation_customs/dds2/ecics/chemicalsubstance_details.jsp?Lang=en &amp; Cus=0010001-6</ns7:description>
            </ns4:LsdList>
          </ns3:RDEntry>

        val result = CodeList("CUSCode").value

        result.name mustBe "CUSCode"

        result.source mustBe RefDataFeed

        result.json(xml) mustBe Seq(
          Json.obj(
            "state"      -> "valid",
            "activeFrom" -> "2021-02-22",
            "code"       -> "0010001-6"
          )
        )
      }

      "ControlType" in {
        val xml =
          <ns3:RDEntry>
            <ns4:RDEntryStatus>
              <ns5:state>valid</ns5:state>
              <ns5:activeFrom>2024-07-09</ns5:activeFrom>
            </ns4:RDEntryStatus>
            <ns4:dataItem name="Code">10</ns4:dataItem>
            <ns4:LsdList>
              <ns7:description lang="en">Documentary controls</ns7:description>
            </ns4:LsdList>
          </ns3:RDEntry>

        val result = CodeList("ControlType").value

        result.name mustBe "ControlType"

        result.source mustBe RefDataFeed

        result.json(xml) mustBe Seq(
          Json.obj(
            "state"       -> "valid",
            "activeFrom"  -> "2024-07-09",
            "code"        -> "10",
            "description" -> "Documentary controls"
          )
        )
      }

      "CountryAddressPostcodeBased" in {
        val xml =
          <ns3:RDEntry>
            <ns4:RDEntryStatus>
              <ns5:state>valid</ns5:state>
              <ns5:activeFrom>2024-02-21</ns5:activeFrom>
            </ns4:RDEntryStatus>
            <ns4:dataItem name="CountryCode">IE</ns4:dataItem>
            <ns4:LsdList>
              <ns7:description lang="en">Ireland</ns7:description>
            </ns4:LsdList>
          </ns3:RDEntry>

        val result = CodeList("CountryAddressPostcodeBased").value

        result.name mustBe "CountryAddressPostcodeBased"

        result.source mustBe RefDataFeed

        result.json(xml) mustBe Seq(
          Json.obj(
            "state"       -> "valid",
            "activeFrom"  -> "2024-02-21",
            "code"        -> "IE",
            "description" -> "Ireland"
          )
        )
      }

      "CountryAddressPostcodeOnly" in {
        val xml =
          <ns3:RDEntry>
            <ns4:RDEntryStatus>
              <ns5:state>valid</ns5:state>
              <ns5:activeFrom>2024-02-21</ns5:activeFrom>
            </ns4:RDEntryStatus>
            <ns4:dataItem name="CountryCode">IE</ns4:dataItem>
            <ns4:LsdList>
              <ns7:description lang="en">Ireland</ns7:description>
            </ns4:LsdList>
          </ns3:RDEntry>

        val result = CodeList("CountryAddressPostcodeOnly").value

        result.name mustBe "CountryAddressPostcodeOnly"

        result.source mustBe RefDataFeed

        result.json(xml) mustBe Seq(
          Json.obj(
            "state"       -> "valid",
            "activeFrom"  -> "2024-02-21",
            "code"        -> "IE",
            "description" -> "Ireland"
          )
        )
      }

      "CountryCodesCTC" in {
        val xml =
          <ns3:RDEntry>
            <ns4:RDEntryStatus>
              <ns5:state>valid</ns5:state>
              <ns5:activeFrom>2024-02-21</ns5:activeFrom>
            </ns4:RDEntryStatus>
            <ns4:dataItem name="CountryCode">CH</ns4:dataItem>
            <ns4:LsdList>
              <ns7:description lang="en">Switzerland</ns7:description>
            </ns4:LsdList>
          </ns3:RDEntry>

        val result = CodeList("CountryCodesCTC").value

        result.name mustBe "CountryCodesCTC"

        result.source mustBe RefDataFeed

        result.json(xml) mustBe Seq(
          Json.obj(
            "state"       -> "valid",
            "activeFrom"  -> "2024-02-21",
            "code"        -> "CH",
            "description" -> "Switzerland"
          )
        )
      }

      "CountryCodesCommonTransit" in {
        val xml =
          <ns3:RDEntry>
            <ns4:RDEntryStatus>
              <ns5:state>valid</ns5:state>
              <ns5:activeFrom>2024-02-21</ns5:activeFrom>
            </ns4:RDEntryStatus>
            <ns4:dataItem name="CountryCode">AD</ns4:dataItem>
            <ns4:dataItem name="TccEntryDate">19000101</ns4:dataItem>
            <ns4:dataItem name="NctsEntryDate">19000101</ns4:dataItem>
            <ns4:dataItem name="GeoNomenclatureCode">043</ns4:dataItem>
            <ns4:dataItem name="CountryRegimeCode">TOC</ns4:dataItem>
            <ns4:LsdList>
              <ns7:description lang="en">Andorra</ns7:description>
            </ns4:LsdList>
          </ns3:RDEntry>

        val result = CodeList("CountryCodesCommonTransit").value

        result.name mustBe "CountryCodesCommonTransit"

        result.source mustBe RefDataFeed

        result.json(xml) mustBe Seq(
          Json.obj(
            "state"       -> "valid",
            "activeFrom"  -> "2024-02-21",
            "code"        -> "AD",
            "description" -> "Andorra"
          )
        )
      }

      "CountryCodesCommunity" in {
        val xml =
          <ns3:RDEntry>
            <ns4:RDEntryStatus>
              <ns5:state>valid</ns5:state>
              <ns5:activeFrom>2024-02-21</ns5:activeFrom>
            </ns4:RDEntryStatus>
            <ns4:dataItem name="CountryCode">AT</ns4:dataItem>
            <ns4:dataItem name="TccEntryDate">19000101</ns4:dataItem>
            <ns4:dataItem name="NctsEntryDate">19000101</ns4:dataItem>
            <ns4:dataItem name="GeoNomenclatureCode">038</ns4:dataItem>
            <ns4:dataItem name="CountryRegimeCode">EEC</ns4:dataItem>
            <ns4:LsdList>
              <ns7:description lang="en">Austria</ns7:description>
            </ns4:LsdList>
          </ns3:RDEntry>

        val result = CodeList("CountryCodesCommunity").value

        result.name mustBe "CountryCodesCommunity"

        result.source mustBe RefDataFeed

        result.json(xml) mustBe Seq(
          Json.obj(
            "state"       -> "valid",
            "activeFrom"  -> "2024-02-21",
            "code"        -> "AT",
            "description" -> "Austria"
          )
        )
      }

      "CountryCodesForAddress" in {
        val xml =
          <ns3:RDEntry>
            <ns4:RDEntryStatus>
              <ns5:state>valid</ns5:state>
              <ns5:activeFrom>2024-02-21</ns5:activeFrom>
            </ns4:RDEntryStatus>
            <ns4:dataItem name="CountryCode">AD</ns4:dataItem>
            <ns4:LsdList>
              <ns7:description lang="en">Andorra</ns7:description>
            </ns4:LsdList>
          </ns3:RDEntry>

        val result = CodeList("CountryCodesForAddress").value

        result.name mustBe "CountryCodesForAddress"

        result.source mustBe RefDataFeed

        result.json(xml) mustBe Seq(
          Json.obj(
            "state"       -> "valid",
            "activeFrom"  -> "2024-02-21",
            "code"        -> "AD",
            "description" -> "Andorra"
          )
        )
      }

      "CountryCodesFullList" in {
        val xml =
          <ns3:RDEntry>
            <ns4:RDEntryStatus>
              <ns5:state>valid</ns5:state>
              <ns5:activeFrom>2024-02-21</ns5:activeFrom>
            </ns4:RDEntryStatus>
            <ns4:dataItem name="CountryCode">AD</ns4:dataItem>
            <ns4:dataItem name="TccEntryDate">19000101</ns4:dataItem>
            <ns4:dataItem name="NctsEntryDate">19000101</ns4:dataItem>
            <ns4:dataItem name="GeoNomenclatureCode">043</ns4:dataItem>
            <ns4:dataItem name="CountryRegimeCode">TOC</ns4:dataItem>
            <ns4:LsdList>
              <ns7:description lang="en">Andorra</ns7:description>
            </ns4:LsdList>
          </ns3:RDEntry>

        val result = CodeList("CountryCodesFullList").value

        result.name mustBe "CountryCodesFullList"

        result.source mustBe RefDataFeed

        result.json(xml) mustBe Seq(
          Json.obj(
            "state"       -> "valid",
            "activeFrom"  -> "2024-02-21",
            "code"        -> "AD",
            "description" -> "Andorra"
          )
        )
      }

      "CountryCustomsSecurityAgreementArea" in {
        val xml =
          <ns3:RDEntry>
            <ns4:RDEntryStatus>
              <ns5:state>valid</ns5:state>
              <ns5:activeFrom>2024-02-21</ns5:activeFrom>
            </ns4:RDEntryStatus>
            <ns4:dataItem name="CountryCode">AD</ns4:dataItem>
            <ns4:LsdList>
              <ns7:description lang="en">Andorra</ns7:description>
            </ns4:LsdList>
          </ns3:RDEntry>

        val result = CodeList("CountryCustomsSecurityAgreementArea").value

        result.name mustBe "CountryCustomsSecurityAgreementArea"

        result.source mustBe RefDataFeed

        result.json(xml) mustBe Seq(
          Json.obj(
            "state"       -> "valid",
            "activeFrom"  -> "2024-02-21",
            "code"        -> "AD",
            "description" -> "Andorra"
          )
        )
      }

      "CountryWithoutZip" in {
        val xml =
          <ns3:RDEntry>
            <ns4:RDEntryStatus>
              <ns5:state>valid</ns5:state>
              <ns5:activeFrom>2023-06-07</ns5:activeFrom>
            </ns4:RDEntryStatus>
            <ns4:dataItem name="CountryCode">AD</ns4:dataItem>
          </ns3:RDEntry>

        val result = CodeList("CountryWithoutZip").value

        result.name mustBe "CountryWithoutZip"

        result.source mustBe RefDataFeed

        result.json(xml) mustBe Seq(
          Json.obj(
            "state"      -> "valid",
            "activeFrom" -> "2023-06-07",
            "code"       -> "AD"
          )
        )
      }

      "CurrencyCodes" in {
        val xml =
          <ns3:RDEntry>
            <ns4:RDEntryStatus>
              <ns5:state>valid</ns5:state>
              <ns5:activeFrom>2024-02-21</ns5:activeFrom>
            </ns4:RDEntryStatus>
            <ns4:dataItem name="Currency">BGN</ns4:dataItem>
            <ns4:dataItem name="RateValue">1.9558</ns4:dataItem>
            <ns4:LsdList>
              <ns7:description lang="en">Bulgarian lev</ns7:description>
            </ns4:LsdList>
          </ns3:RDEntry>

        val result = CodeList("CurrencyCodes").value

        result.name mustBe "CurrencyCodes"

        result.source mustBe RefDataFeed

        result.json(xml) mustBe Seq(
          Json.obj(
            "state"       -> "valid",
            "activeFrom"  -> "2024-02-21",
            "currency"    -> "BGN",
            "description" -> "Bulgarian lev"
          )
        )
      }

      "CustomsOffices" in {
        val xml =
          <ns1:RDEntry>
            <ns2:RDEntryStatus>
              <ns3:state>valid</ns3:state>
              <ns3:activeFrom>2020-03-23</ns3:activeFrom>
            </ns2:RDEntryStatus>
            <ns2:dataItem name="ReferenceNumber">AD000001</ns2:dataItem>
            <ns2:dataItem name="ReferenceNumberMainOffice">AD000003</ns2:dataItem>
            <ns2:dataItem name="ReferenceNumberHigherAuthority">AD000003</ns2:dataItem>
            <ns2:dataItem name="ReferenceNumberCompetentAuthorityOfEnquiry">AD000003</ns2:dataItem>
            <ns2:dataItem name="ReferenceNumberCompetentAuthorityOfRecovery">AD000003</ns2:dataItem>
            <ns2:dataItem name="CountryCode">AD</ns2:dataItem>
            <ns2:dataItem name="UnLocodeId">ALV</ns2:dataItem>
            <ns2:dataItem name="NctsEntryDate">20070614</ns2:dataItem>
            <ns2:dataItem name="NearestOffice">ES002541</ns2:dataItem>
            <ns2:dataItem name="PostalCode">AD600</ns2:dataItem>
            <ns2:dataItem name="PhoneNumber">+ (376) 84 1090</ns2:dataItem>
            <ns2:dataItem name="FaxNumber">+ (376) 841 898</ns2:dataItem>
            <ns2:dataItem name="GeoInfoCode">ES/AD</ns2:dataItem>
            <ns2:dataItem name="TraderDedicated">0</ns2:dataItem>
            <ns2:dataGroup name="CustomsOfficeLsd">
              <ns2:dataItem name="LanguageCode">EN</ns2:dataItem>
              <ns2:dataItem name="CustomsOfficeUsualName">CUSTOMS OFFICE SANT JULIÀ DE LÒRIA</ns2:dataItem>
              <ns2:dataItem name="StreetAndNumber">RIU RUNER BORDER</ns2:dataItem>
              <ns2:dataItem name="City">SANT JULIÀ DE LÒRIA</ns2:dataItem>
              <ns2:dataItem name="PrefixSuffixFlag">0</ns2:dataItem>
              <ns2:dataItem name="SpaceToAdd">0</ns2:dataItem>
            </ns2:dataGroup>
            <ns2:dataGroup name="CustomsOfficeTimetable">
              <ns2:dataItem name="SeasonCode">1</ns2:dataItem>
              <ns2:dataItem name="SeasonName">All Year</ns2:dataItem>
              <ns2:dataItem name="SeasonStartDate">20180101</ns2:dataItem>
              <ns2:dataItem name="SeasonEndDate">20991231</ns2:dataItem>
              <ns2:dataGroup name="CustomsOfficeTimetableLine">
                <ns2:dataItem name="DayInTheWeekBeginDay">1</ns2:dataItem>
                <ns2:dataItem name="OpeningHoursTimeFirstPeriodFrom">0800</ns2:dataItem>
                <ns2:dataItem name="OpeningHoursTimeFirstPeriodTo">2000</ns2:dataItem>
                <ns2:dataItem name="DayInTheWeekEndDay">5</ns2:dataItem>
                <ns2:dataGroup name="CustomsOfficeRoleTrafficCompetence">
                  <ns2:dataItem name="Role">AUT</ns2:dataItem>
                  <ns2:dataItem name="TrafficType">N/A</ns2:dataItem>
                </ns2:dataGroup>
                <ns2:dataGroup name="CustomsOfficeRoleTrafficCompetence">
                  <ns2:dataItem name="Role">DEP</ns2:dataItem>
                  <ns2:dataItem name="TrafficType">R</ns2:dataItem>
                </ns2:dataGroup>
                <ns2:dataGroup name="CustomsOfficeRoleTrafficCompetence">
                  <ns2:dataItem name="Role">DES</ns2:dataItem>
                  <ns2:dataItem name="TrafficType">R</ns2:dataItem>
                </ns2:dataGroup>
                <ns2:dataGroup name="CustomsOfficeRoleTrafficCompetence">
                  <ns2:dataItem name="Role">TRA</ns2:dataItem>
                  <ns2:dataItem name="TrafficType">R</ns2:dataItem>
                </ns2:dataGroup>
              </ns2:dataGroup>
              <ns2:dataGroup name="CustomsOfficeTimetableLine">
                <ns2:dataItem name="DayInTheWeekBeginDay">6</ns2:dataItem>
                <ns2:dataItem name="OpeningHoursTimeFirstPeriodFrom">0800</ns2:dataItem>
                <ns2:dataItem name="OpeningHoursTimeFirstPeriodTo">1200</ns2:dataItem>
                <ns2:dataItem name="DayInTheWeekEndDay">6</ns2:dataItem>
                <ns2:dataGroup name="CustomsOfficeRoleTrafficCompetence">
                  <ns2:dataItem name="Role">DEP</ns2:dataItem>
                  <ns2:dataItem name="TrafficType">R</ns2:dataItem>
                </ns2:dataGroup>
                <ns2:dataGroup name="CustomsOfficeRoleTrafficCompetence">
                  <ns2:dataItem name="Role">DES</ns2:dataItem>
                  <ns2:dataItem name="TrafficType">R</ns2:dataItem>
                </ns2:dataGroup>
                <ns2:dataGroup name="CustomsOfficeRoleTrafficCompetence">
                  <ns2:dataItem name="Role">TRA</ns2:dataItem>
                  <ns2:dataItem name="TrafficType">R</ns2:dataItem>
                </ns2:dataGroup>
              </ns2:dataGroup>
            </ns2:dataGroup>
            <ns2:dataGroup name="CustomsOfficeLsd">
              <ns2:dataItem name="LanguageCode">ES</ns2:dataItem>
              <ns2:dataItem name="CustomsOfficeUsualName">ADUANA DE ST. JULIÀ DE LÒRIA</ns2:dataItem>
              <ns2:dataItem name="StreetAndNumber">FRONTERA RIU RUNER</ns2:dataItem>
              <ns2:dataItem name="City">SANT JULIÀ DE LÒRIA</ns2:dataItem>
              <ns2:dataItem name="PrefixSuffixFlag">0</ns2:dataItem>
              <ns2:dataItem name="SpaceToAdd">0</ns2:dataItem>
            </ns2:dataGroup>
            <ns2:dataGroup name="CustomsOfficeLsd">
              <ns2:dataItem name="LanguageCode">FR</ns2:dataItem>
              <ns2:dataItem name="CustomsOfficeUsualName">BUREAU DE SANT JULIÀ DE LÒRIA</ns2:dataItem>
              <ns2:dataItem name="StreetAndNumber">FRONTIÈRE RIU RUNER</ns2:dataItem>
              <ns2:dataItem name="City">SANT JULIÀ DE LÒRIA</ns2:dataItem>
              <ns2:dataItem name="PrefixSuffixFlag">0</ns2:dataItem>
              <ns2:dataItem name="SpaceToAdd">0</ns2:dataItem>
            </ns2:dataGroup>
          </ns1:RDEntry>

        val result = CodeList("CustomsOffices").value

        result.name mustBe "customsOffices"

        result.source mustBe ColDataFeed

        result.json(xml) mustBe Seq(
          Json.obj(
            "state"        -> "valid",
            "activeFrom"   -> "2020-03-23",
            "languageCode" -> "EN",
            "name"         -> "CUSTOMS OFFICE SANT JULIÀ DE LÒRIA",
            "phoneNumber"  -> "+ (376) 84 1090",
            "id"           -> "AD000001",
            "countryId"    -> "AD",
            "roles" -> JsArray(
              Seq(
                Json.obj("role" -> "AUT"),
                Json.obj("role" -> "DEP"),
                Json.obj("role" -> "DES"),
                Json.obj("role" -> "TRA")
              )
            )
          ),
          Json.obj(
            "state"        -> "valid",
            "activeFrom"   -> "2020-03-23",
            "languageCode" -> "ES",
            "name"         -> "ADUANA DE ST. JULIÀ DE LÒRIA",
            "phoneNumber"  -> "+ (376) 84 1090",
            "id"           -> "AD000001",
            "countryId"    -> "AD",
            "roles" -> JsArray(
              Seq(
                Json.obj("role" -> "AUT"),
                Json.obj("role" -> "DEP"),
                Json.obj("role" -> "DES"),
                Json.obj("role" -> "TRA")
              )
            )
          ),
          Json.obj(
            "state"        -> "valid",
            "activeFrom"   -> "2020-03-23",
            "languageCode" -> "FR",
            "name"         -> "BUREAU DE SANT JULIÀ DE LÒRIA",
            "phoneNumber"  -> "+ (376) 84 1090",
            "id"           -> "AD000001",
            "countryId"    -> "AD",
            "roles" -> JsArray(
              Seq(
                Json.obj("role" -> "AUT"),
                Json.obj("role" -> "DEP"),
                Json.obj("role" -> "DES"),
                Json.obj("role" -> "TRA")
              )
            )
          )
        )
      }

      "DeclarationType" in {
        val xml =
          <ns3:RDEntry>
            <ns4:RDEntryStatus>
              <ns5:state>valid</ns5:state>
              <ns5:activeFrom>2024-06-20</ns5:activeFrom>
            </ns4:RDEntryStatus>
            <ns4:dataItem name="DeclarationTypeCode">T</ns4:dataItem>
            <ns4:LsdList>
              <ns7:description lang="en">Mixed consignments comprising...</ns7:description>
            </ns4:LsdList>
          </ns3:RDEntry>

        val result = CodeList("DeclarationType").value

        result.name mustBe "DeclarationType"

        result.source mustBe RefDataFeed

        result.json(xml) mustBe Seq(
          Json.obj(
            "state"       -> "valid",
            "activeFrom"  -> "2024-06-20",
            "code"        -> "T",
            "description" -> "Mixed consignments comprising..."
          )
        )
      }

      "DeclarationTypeAdditional" in {
        val xml =
          <ns3:RDEntry>
            <ns4:RDEntryStatus>
              <ns5:state>valid</ns5:state>
              <ns5:activeFrom>2024-07-09</ns5:activeFrom>
            </ns4:RDEntryStatus>
            <ns4:dataItem name="Code">A</ns4:dataItem>
            <ns4:LsdList>
              <ns7:description lang="en">for a standard customs declaration (under Article 162 of the Code)</ns7:description>
            </ns4:LsdList>
          </ns3:RDEntry>

        val result = CodeList("DeclarationTypeAdditional").value

        result.name mustBe "DeclarationTypeAdditional"

        result.source mustBe RefDataFeed

        result.json(xml) mustBe Seq(
          Json.obj(
            "state"       -> "valid",
            "activeFrom"  -> "2024-07-09",
            "code"        -> "A",
            "description" -> "for a standard customs declaration (under Article 162 of the Code)"
          )
        )
      }

      "DeclarationTypeItemLevel" in {
        val xml =
          <ns3:RDEntry>
            <ns4:RDEntryStatus>
              <ns5:state>valid</ns5:state>
              <ns5:activeFrom>2024-06-20</ns5:activeFrom>
            </ns4:RDEntryStatus>
            <ns4:dataItem name="DeclarationTypeCode">T1</ns4:dataItem>
            <ns4:LsdList>
              <ns7:description lang="en">Goods not having the customs status of Union goods, which are placed under the common transit procedure.</ns7:description>
            </ns4:LsdList>
          </ns3:RDEntry>

        val result = CodeList("DeclarationTypeItemLevel").value

        result.name mustBe "DeclarationTypeItemLevel"

        result.source mustBe RefDataFeed

        result.json(xml) mustBe Seq(
          Json.obj(
            "state"       -> "valid",
            "activeFrom"  -> "2024-06-20",
            "code"        -> "T1",
            "description" -> "Goods not having the customs status of Union goods, which are placed under the common transit procedure."
          )
        )
      }

      "DeclarationTypeSecurity" in {
        val xml =
          <ns3:RDEntry>
            <ns4:RDEntryStatus>
              <ns5:state>valid</ns5:state>
              <ns5:activeFrom>2024-03-18</ns5:activeFrom>
            </ns4:RDEntryStatus>
            <ns4:dataItem name="DeclarationTypeSecurityCode">0</ns4:dataItem>
            <ns4:LsdList>
              <ns7:description lang="en">Not used for safety and security purposes</ns7:description>
            </ns4:LsdList>
          </ns3:RDEntry>

        val result = CodeList("DeclarationTypeSecurity").value

        result.name mustBe "DeclarationTypeSecurity"

        result.source mustBe RefDataFeed

        result.json(xml) mustBe Seq(
          Json.obj(
            "state"       -> "valid",
            "activeFrom"  -> "2024-03-18",
            "code"        -> "0",
            "description" -> "Not used for safety and security purposes"
          )
        )
      }

      "DocumentTypeExcise" in {
        val xml =
          <ns3:RDEntry>
            <ns4:RDEntryStatus>
              <ns5:state>valid</ns5:state>
              <ns5:activeFrom>2024-03-18</ns5:activeFrom>
            </ns4:RDEntryStatus>
            <ns4:dataItem name="PreviousDocumentTypeCode">C651</ns4:dataItem>
            <ns4:LsdList>
              <ns7:description lang="en">AAD - Administrative Accompanying Document (EMCS)</ns7:description>
            </ns4:LsdList>
          </ns3:RDEntry>

        val result = CodeList("DocumentTypeExcise").value

        result.name mustBe "DocumentTypeExcise"

        result.source mustBe RefDataFeed

        result.json(xml) mustBe Seq(
          Json.obj(
            "state"       -> "valid",
            "activeFrom"  -> "2024-03-18",
            "code"        -> "C651",
            "description" -> "AAD - Administrative Accompanying Document (EMCS)"
          )
        )
      }

      "FunctionalErrorCodesIeCA" in {
        val xml =
          <ns3:RDEntry>
            <ns4:RDEntryStatus>
              <ns5:state>valid</ns5:state>
              <ns5:activeFrom>2024-03-18</ns5:activeFrom>
            </ns4:RDEntryStatus>
            <ns4:dataItem name="Code">12</ns4:dataItem>
            <ns4:dataItem name="Remark">Value of an element in a message is outside...</ns4:dataItem>
            <ns4:LsdList>
              <ns7:description lang="en">Codelist violation</ns7:description>
            </ns4:LsdList>
          </ns3:RDEntry>

        val result = CodeList("FunctionalErrorCodesIeCA").value

        result.name mustBe "FunctionalErrorCodesIeCA"

        result.source mustBe RefDataFeed

        result.json(xml) mustBe Seq(
          Json.obj(
            "state"       -> "valid",
            "activeFrom"  -> "2024-03-18",
            "code"        -> "12",
            "description" -> "Codelist violation"
          )
        )
      }

      "GuaranteeType" in {
        val xml =
          <ns3:RDEntry>
            <ns4:RDEntryStatus>
              <ns5:state>valid</ns5:state>
              <ns5:activeFrom>2024-07-09</ns5:activeFrom>
            </ns4:RDEntryStatus>
            <ns4:dataItem name="GuaranteeTypeCode">0</ns4:dataItem>
            <ns4:LsdList>
              <ns7:description lang="en">Guarantee waiver</ns7:description>
            </ns4:LsdList>
          </ns3:RDEntry>

        val result = CodeList("GuaranteeType").value

        result.name mustBe "GuaranteeType"

        result.source mustBe RefDataFeed

        result.json(xml) mustBe Seq(
          Json.obj(
            "state"       -> "valid",
            "activeFrom"  -> "2024-07-09",
            "code"        -> "0",
            "description" -> "Guarantee waiver"
          )
        )
      }

      "GuaranteeTypeWithGRN" in {
        val xml =
          <ns3:RDEntry>
            <ns4:RDEntryStatus>
              <ns5:state>valid</ns5:state>
              <ns5:activeFrom>2024-07-09</ns5:activeFrom>
            </ns4:RDEntryStatus>
            <ns4:dataItem name="GuaranteeTypeCode">0</ns4:dataItem>
            <ns4:LsdList>
              <ns7:description lang="en">Guarantee waiver</ns7:description>
            </ns4:LsdList>
          </ns3:RDEntry>

        val result = CodeList("GuaranteeTypeWithGRN").value

        result.name mustBe "GuaranteeTypeWithGRN"

        result.source mustBe RefDataFeed

        result.json(xml) mustBe Seq(
          Json.obj(
            "state"       -> "valid",
            "activeFrom"  -> "2024-07-09",
            "code"        -> "0",
            "description" -> "Guarantee waiver"
          )
        )
      }

      "GuaranteeTypeEUNonTIR" in {
        val xml =
          <ns3:RDEntry>
            <ns4:RDEntryStatus>
              <ns5:state>valid</ns5:state>
              <ns5:activeFrom>2024-07-09</ns5:activeFrom>
            </ns4:RDEntryStatus>
            <ns4:dataItem name="GuaranteeTypeCode">0</ns4:dataItem>
            <ns4:LsdList>
              <ns7:description lang="en">Guarantee waiver</ns7:description>
            </ns4:LsdList>
          </ns3:RDEntry>

        val result = CodeList("GuaranteeTypeEUNonTIR").value

        result.name mustBe "GuaranteeTypeEUNonTIR"

        result.source mustBe RefDataFeed

        result.json(xml) mustBe Seq(
          Json.obj(
            "state"       -> "valid",
            "activeFrom"  -> "2024-07-09",
            "code"        -> "0",
            "description" -> "Guarantee waiver"
          )
        )
      }

      "GuaranteeTypeCTC" in {
        val xml =
          <ns3:RDEntry>
            <ns4:RDEntryStatus>
              <ns5:state>valid</ns5:state>
              <ns5:activeFrom>2024-07-09</ns5:activeFrom>
            </ns4:RDEntryStatus>
            <ns4:dataItem name="GuaranteeTypeCode">0</ns4:dataItem>
            <ns4:LsdList>
              <ns7:description lang="en">Guarantee waiver</ns7:description>
            </ns4:LsdList>
          </ns3:RDEntry>

        val result = CodeList("GuaranteeTypeCTC").value

        result.name mustBe "GuaranteeTypeCTC"

        result.source mustBe RefDataFeed

        result.json(xml) mustBe Seq(
          Json.obj(
            "state"       -> "valid",
            "activeFrom"  -> "2024-07-09",
            "code"        -> "0",
            "description" -> "Guarantee waiver"
          )
        )
      }

      "GuaranteeTypeWithReference" in {
        val xml =
          <ns3:RDEntry>
            <ns4:RDEntryStatus>
              <ns5:state>valid</ns5:state>
              <ns5:activeFrom>2024-07-09</ns5:activeFrom>
            </ns4:RDEntryStatus>
            <ns4:dataItem name="GuaranteeTypeCode">0</ns4:dataItem>
            <ns4:LsdList>
              <ns7:description lang="en">Guarantee waiver</ns7:description>
            </ns4:LsdList>
          </ns3:RDEntry>

        val result = CodeList("GuaranteeTypeWithReference").value

        result.name mustBe "GuaranteeTypeWithReference"

        result.source mustBe RefDataFeed

        result.json(xml) mustBe Seq(
          Json.obj(
            "state"       -> "valid",
            "activeFrom"  -> "2024-07-09",
            "code"        -> "0",
            "description" -> "Guarantee waiver"
          )
        )
      }

      "HScode" in {
        val xml =
          <ns3:RDEntry>
            <ns4:RDEntryStatus>
              <ns5:state>valid</ns5:state>
              <ns5:activeFrom>2023-05-20</ns5:activeFrom>
            </ns4:RDEntryStatus>
            <ns4:dataItem name="Code">010121</ns4:dataItem>
            <ns4:LsdList>
              <ns7:description lang="en">See http://ec.europa.eu/taxation_customs/dds2/taric/measures.jsp?Lang=en &amp; Taric=010121</ns7:description>
            </ns4:LsdList>
          </ns3:RDEntry>

        val result = CodeList("HScode").value

        result.name mustBe "HScode"

        result.source mustBe RefDataFeed

        result.json(xml) mustBe Seq(
          Json.obj(
            "state"      -> "valid",
            "activeFrom" -> "2023-05-20",
            "code"       -> "010121"
          )
        )
      }

      "IncidentCode" in {
        val xml =
          <ns3:RDEntry>
            <ns4:RDEntryStatus>
              <ns5:state>valid</ns5:state>
              <ns5:activeFrom>2024-05-21</ns5:activeFrom>
            </ns4:RDEntryStatus>
            <ns4:dataItem name="Code">1</ns4:dataItem>
            <ns4:LsdList>
              <ns7:description lang="en">The carrier is obliged to deviate from...</ns7:description>
            </ns4:LsdList>
          </ns3:RDEntry>

        val result = CodeList("IncidentCode").value

        result.name mustBe "IncidentCode"

        result.source mustBe RefDataFeed

        result.json(xml) mustBe Seq(
          Json.obj(
            "state"       -> "valid",
            "activeFrom"  -> "2024-05-21",
            "code"        -> "1",
            "description" -> "The carrier is obliged to deviate from..."
          )
        )
      }

      "InvalidGuaranteeReason" in {
        val xml =
          <ns3:RDEntry>
            <ns4:RDEntryStatus>
              <ns5:state>valid</ns5:state>
              <ns5:activeFrom>2024-06-20</ns5:activeFrom>
            </ns4:RDEntryStatus>
            <ns4:dataItem name="InvalidGuaranteeReasonCode">G01</ns4:dataItem>
            <ns4:LsdList>
              <ns7:description lang="en">Guarantee does not exist</ns7:description>
            </ns4:LsdList>
          </ns3:RDEntry>

        val result = CodeList("InvalidGuaranteeReason").value

        result.name mustBe "InvalidGuaranteeReason"

        result.source mustBe RefDataFeed

        result.json(xml) mustBe Seq(
          Json.obj(
            "state"       -> "valid",
            "activeFrom"  -> "2024-06-20",
            "code"        -> "G01",
            "description" -> "Guarantee does not exist"
          )
        )
      }

      "KindOfPackages" in {
        val xml =
          <ns3:RDEntry>
            <ns4:RDEntryStatus>
              <ns5:state>valid</ns5:state>
              <ns5:activeFrom>2024-02-21</ns5:activeFrom>
            </ns4:RDEntryStatus>
            <ns4:dataItem name="KindOfPackages">1A</ns4:dataItem>
            <ns4:LsdList>
              <ns7:description lang="en">Drum, steel</ns7:description>
            </ns4:LsdList>
          </ns3:RDEntry>

        val result = CodeList("KindOfPackages").value

        result.name mustBe "KindOfPackages"

        result.source mustBe RefDataFeed

        result.json(xml) mustBe Seq(
          Json.obj(
            "state"       -> "valid",
            "activeFrom"  -> "2024-02-21",
            "code"        -> "1A",
            "description" -> "Drum, steel"
          )
        )
      }

      "KindOfPackagesBulk" in {
        val xml =
          <ns3:RDEntry>
            <ns4:RDEntryStatus>
              <ns5:state>valid</ns5:state>
              <ns5:activeFrom>2024-02-21</ns5:activeFrom>
            </ns4:RDEntryStatus>
            <ns4:dataItem name="KindOfPackages">VG</ns4:dataItem>
            <ns4:LsdList>
              <ns7:description lang="en">Bulk, gas (at 1031 mbar and 15°C)</ns7:description>
            </ns4:LsdList>
          </ns3:RDEntry>

        val result = CodeList("KindOfPackagesBulk").value

        result.name mustBe "KindOfPackagesBulk"

        result.source mustBe RefDataFeed

        result.json(xml) mustBe Seq(
          Json.obj(
            "state"       -> "valid",
            "activeFrom"  -> "2024-02-21",
            "code"        -> "VG",
            "description" -> "Bulk, gas (at 1031 mbar and 15°C)"
          )
        )
      }

      "KindOfPackagesUnpacked" in {
        val xml =
          <ns3:RDEntry>
            <ns4:RDEntryStatus>
              <ns5:state>valid</ns5:state>
              <ns5:activeFrom>2024-02-21</ns5:activeFrom>
            </ns4:RDEntryStatus>
            <ns4:dataItem name="KindOfPackages">NE</ns4:dataItem>
            <ns4:LsdList>
              <ns7:description lang="en">Unpacked or unpackaged</ns7:description>
            </ns4:LsdList>
          </ns3:RDEntry>

        val result = CodeList("KindOfPackagesUnpacked").value

        result.name mustBe "KindOfPackagesUnpacked"

        result.source mustBe RefDataFeed

        result.json(xml) mustBe Seq(
          Json.obj(
            "state"       -> "valid",
            "activeFrom"  -> "2024-02-21",
            "code"        -> "NE",
            "description" -> "Unpacked or unpackaged"
          )
        )
      }

      "Nationality" in {
        val xml =
          <ns3:RDEntry>
            <ns4:RDEntryStatus>
              <ns5:state>valid</ns5:state>
              <ns5:activeFrom>2024-02-21</ns5:activeFrom>
            </ns4:RDEntryStatus>
            <ns4:dataItem name="CountryCode">AD</ns4:dataItem>
            <ns4:LsdList>
              <ns7:description lang="en">Andorra</ns7:description>
            </ns4:LsdList>
          </ns3:RDEntry>

        val result = CodeList("Nationality").value

        result.name mustBe "Nationality"

        result.source mustBe RefDataFeed

        result.json(xml) mustBe Seq(
          Json.obj(
            "state"       -> "valid",
            "activeFrom"  -> "2024-02-21",
            "code"        -> "AD",
            "description" -> "Andorra"
          )
        )
      }

      "PreviousDocumentExportType" in {
        val xml =
          <ns3:RDEntry>
            <ns4:RDEntryStatus>
              <ns5:state>valid</ns5:state>
              <ns5:activeFrom>2024-03-18</ns5:activeFrom>
            </ns4:RDEntryStatus>
            <ns4:dataItem name="PreviousDocumentTypeCode">N830</ns4:dataItem>
            <ns4:LsdList>
              <ns7:description lang="en">Goods declaration for exportation</ns7:description>
            </ns4:LsdList>
          </ns3:RDEntry>

        val result = CodeList("PreviousDocumentExportType").value

        result.name mustBe "PreviousDocumentExportType"

        result.source mustBe RefDataFeed

        result.json(xml) mustBe Seq(
          Json.obj(
            "state"       -> "valid",
            "activeFrom"  -> "2024-03-18",
            "code"        -> "N830",
            "description" -> "Goods declaration for exportation"
          )
        )
      }

      "PreviousDocumentType" in {
        val xml =
          <ns3:RDEntry>
            <ns4:RDEntryStatus>
              <ns5:state>valid</ns5:state>
              <ns5:activeFrom>2024-03-18</ns5:activeFrom>
            </ns4:RDEntryStatus>
            <ns4:dataItem name="PreviousDocumentTypeCode">C512</ns4:dataItem>
            <ns4:LsdList>
              <ns7:description lang="en">SDE - Authorisation to use simplified...</ns7:description>
            </ns4:LsdList>
          </ns3:RDEntry>

        val result = CodeList("PreviousDocumentType").value

        result.name mustBe "PreviousDocumentType"

        result.source mustBe RefDataFeed

        result.json(xml) mustBe Seq(
          Json.obj(
            "state"       -> "valid",
            "activeFrom"  -> "2024-03-18",
            "code"        -> "C512",
            "description" -> "SDE - Authorisation to use simplified..."
          )
        )
      }

      "PreviousDocumentUnionGoods" in {
        val xml =
          <ns3:RDEntry>
            <ns4:RDEntryStatus>
              <ns5:state>valid</ns5:state>
              <ns5:activeFrom>2024-03-18</ns5:activeFrom>
            </ns4:RDEntryStatus>
            <ns4:dataItem name="PreviousDocumentTypeCode">C612</ns4:dataItem>
            <ns4:LsdList>
              <ns7:description lang="en">Internal Community transit Declaration — Article 227 of the Code</ns7:description>
            </ns4:LsdList>
          </ns3:RDEntry>

        val result = CodeList("PreviousDocumentUnionGoods").value

        result.name mustBe "PreviousDocumentUnionGoods"

        result.source mustBe RefDataFeed

        result.json(xml) mustBe Seq(
          Json.obj(
            "state"       -> "valid",
            "activeFrom"  -> "2024-03-18",
            "code"        -> "C612",
            "description" -> "Internal Community transit Declaration — Article 227 of the Code"
          )
        )
      }

      "QualifierOfIdentificationIncident" in {
        val xml =
          <ns3:RDEntry>
            <ns4:RDEntryStatus>
              <ns5:state>valid</ns5:state>
              <ns5:activeFrom>2024-03-18</ns5:activeFrom>
            </ns4:RDEntryStatus>
            <ns4:dataItem name="QualifierOfTheIdentification">U</ns4:dataItem>
            <ns4:LsdList>
              <ns7:description lang="en">UN/LOCODE</ns7:description>
            </ns4:LsdList>
          </ns3:RDEntry>

        val result = CodeList("QualifierOfIdentificationIncident").value

        result.name mustBe "QualifierOfIdentificationIncident"

        result.source mustBe RefDataFeed

        result.json(xml) mustBe Seq(
          Json.obj(
            "state"       -> "valid",
            "activeFrom"  -> "2024-03-18",
            "qualifier"   -> "U",
            "description" -> "UN/LOCODE"
          )
        )
      }

      "QualifierOfTheIdentification" in {
        val xml =
          <ns3:RDEntry>
            <ns4:RDEntryStatus>
              <ns5:state>valid</ns5:state>
              <ns5:activeFrom>2024-03-18</ns5:activeFrom>
            </ns4:RDEntryStatus>
            <ns4:dataItem name="QualifierOfTheIdentification">T</ns4:dataItem>
            <ns4:LsdList>
              <ns7:description lang="en">Postal code</ns7:description>
            </ns4:LsdList>
          </ns3:RDEntry>

        val result = CodeList("QualifierOfTheIdentification").value

        result.name mustBe "QualifierOfTheIdentification"

        result.source mustBe RefDataFeed

        result.json(xml) mustBe Seq(
          Json.obj(
            "state"       -> "valid",
            "activeFrom"  -> "2024-03-18",
            "qualifier"   -> "T",
            "description" -> "Postal code"
          )
        )
      }

      "RejectionCodeDepartureExport" in {
        val xml =
          <ns3:RDEntry>
            <ns4:RDEntryStatus>
              <ns5:state>valid</ns5:state>
              <ns5:activeFrom>2024-06-20</ns5:activeFrom>
            </ns4:RDEntryStatus>
            <ns4:dataItem name="RejectionDepartureExportCode">12</ns4:dataItem>
            <ns4:LsdList>
              <ns7:description lang="en">Message with functional error(s) - Violation of Rules &amp; Conditions</ns7:description>
            </ns4:LsdList>
          </ns3:RDEntry>

        val result = CodeList("RejectionCodeDepartureExport").value

        result.name mustBe "RejectionCodeDepartureExport"

        result.source mustBe RefDataFeed

        result.json(xml) mustBe Seq(
          Json.obj(
            "state"       -> "valid",
            "activeFrom"  -> "2024-06-20",
            "code"        -> "12",
            "description" -> "Message with functional error(s) - Violation of Rules & Conditions"
          )
        )
      }

      "RepresentativeStatusCode" in {
        val xml =
          <ns3:RDEntry>
            <ns4:RDEntryStatus>
              <ns5:state>valid</ns5:state>
              <ns5:activeFrom>2024-03-08</ns5:activeFrom>
            </ns4:RDEntryStatus>
            <ns4:dataItem name="RepresentativeStatusCode">2</ns4:dataItem>
            <ns4:LsdList>
              <ns7:description lang="en">Representative - direct representation (within the meaning of Article 18(1) of the Code)</ns7:description>
            </ns4:LsdList>
          </ns3:RDEntry>

        val result = CodeList("RepresentativeStatusCode").value

        result.name mustBe "RepresentativeStatusCode"

        result.source mustBe RefDataFeed

        result.json(xml) mustBe Seq(
          Json.obj(
            "state"       -> "valid",
            "activeFrom"  -> "2024-03-08",
            "code"        -> "2",
            "description" -> "Representative - direct representation (within the meaning of Article 18(1) of the Code)"
          )
        )
      }

      "RequestedDocumentType" in {
        val xml =
          <ns3:RDEntry>
            <ns4:RDEntryStatus>
              <ns5:state>valid</ns5:state>
              <ns5:activeFrom>2022-09-07</ns5:activeFrom>
            </ns4:RDEntryStatus>
            <ns4:dataItem name="DocumentType">C085</ns4:dataItem>
            <ns4:dataItem name="TransportDocument">0</ns4:dataItem>
            <ns4:LsdList>
              <ns7:description lang="en">Common Health Entry Document for...</ns7:description>
            </ns4:LsdList>
          </ns3:RDEntry>

        val result = CodeList("RequestedDocumentType").value

        result.name mustBe "RequestedDocumentType"

        result.source mustBe RefDataFeed

        result.json(xml) mustBe Seq(
          Json.obj(
            "state"       -> "valid",
            "activeFrom"  -> "2022-09-07",
            "code"        -> "C085",
            "description" -> "Common Health Entry Document for..."
          )
        )
      }

      "SpecificCircumstanceIndicatorCode" in {
        val xml =
          <ns3:RDEntry>
            <ns4:RDEntryStatus>
              <ns5:state>valid</ns5:state>
              <ns5:activeFrom>2024-03-18</ns5:activeFrom>
            </ns4:RDEntryStatus>
            <ns4:dataItem name="Code">A20</ns4:dataItem>
            <ns4:LsdList>
              <ns7:description lang="en">Express consignments in the context of exit summary declarations</ns7:description>
            </ns4:LsdList>
          </ns3:RDEntry>

        val result = CodeList("SpecificCircumstanceIndicatorCode").value

        result.name mustBe "SpecificCircumstanceIndicatorCode"

        result.source mustBe RefDataFeed

        result.json(xml) mustBe Seq(
          Json.obj(
            "state"       -> "valid",
            "activeFrom"  -> "2024-03-18",
            "code"        -> "A20",
            "description" -> "Express consignments in the context of exit summary declarations"
          )
        )
      }

      "SupportingDocumentType" in {
        val xml =
          <ns3:RDEntry>
            <ns4:RDEntryStatus>
              <ns5:state>valid</ns5:state>
              <ns5:activeFrom>2024-07-09</ns5:activeFrom>
            </ns4:RDEntryStatus>
            <ns4:dataItem name="SupportingDocumentCode">C085</ns4:dataItem>
            <ns4:LsdList>
              <ns7:description lang="en">Common Health Entry Document for...</ns7:description>
            </ns4:LsdList>
          </ns3:RDEntry>

        val result = CodeList("SupportingDocumentType").value

        result.name mustBe "SupportingDocumentType"

        result.source mustBe RefDataFeed

        result.json(xml) mustBe Seq(
          Json.obj(
            "state"       -> "valid",
            "activeFrom"  -> "2024-07-09",
            "code"        -> "C085",
            "description" -> "Common Health Entry Document for..."
          )
        )
      }

      "TransportChargesMethodOfPayment" in {
        val xml =
          <ns3:RDEntry>
            <ns4:RDEntryStatus>
              <ns5:state>valid</ns5:state>
              <ns5:activeFrom>2024-06-27</ns5:activeFrom>
            </ns4:RDEntryStatus>
            <ns4:dataItem name="TransportChargesMethodOfPayment">A</ns4:dataItem>
            <ns4:LsdList>
              <ns7:description lang="en">Payment in cash</ns7:description>
            </ns4:LsdList>
          </ns3:RDEntry>

        val result = CodeList("TransportChargesMethodOfPayment").value

        result.name mustBe "TransportChargesMethodOfPayment"

        result.source mustBe RefDataFeed

        result.json(xml) mustBe Seq(
          Json.obj(
            "state"       -> "valid",
            "activeFrom"  -> "2024-06-27",
            "method"      -> "A",
            "description" -> "Payment in cash"
          )
        )
      }

      "TransportDocumentType" in {
        val xml =
          <ns3:RDEntry>
            <ns4:RDEntryStatus>
              <ns5:state>valid</ns5:state>
              <ns5:activeFrom>2024-05-29</ns5:activeFrom>
            </ns4:RDEntryStatus>
            <ns4:dataItem name="Type">N235</ns4:dataItem>
            <ns4:LsdList>
              <ns7:description lang="en">Container list</ns7:description>
            </ns4:LsdList>
          </ns3:RDEntry>

        val result = CodeList("TransportDocumentType").value

        result.name mustBe "TransportDocumentType"

        result.source mustBe RefDataFeed

        result.json(xml) mustBe Seq(
          Json.obj(
            "state"       -> "valid",
            "activeFrom"  -> "2024-05-29",
            "code"        -> "N235",
            "description" -> "Container list"
          )
        )
      }

      "TransportModeCode" in {
        val xml =
          <ns3:RDEntry>
            <ns4:RDEntryStatus>
              <ns5:state>valid</ns5:state>
              <ns5:activeFrom>2024-06-20</ns5:activeFrom>
            </ns4:RDEntryStatus>
            <ns4:dataItem name="TransportModeCode">1</ns4:dataItem>
            <ns4:LsdList>
              <ns7:description lang="en">Maritime Transport</ns7:description>
            </ns4:LsdList>
          </ns3:RDEntry>

        val result = CodeList("TransportModeCode").value

        result.name mustBe "TransportModeCode"

        result.source mustBe RefDataFeed

        result.json(xml) mustBe Seq(
          Json.obj(
            "state"       -> "valid",
            "activeFrom"  -> "2024-06-20",
            "code"        -> "1",
            "description" -> "Maritime Transport"
          )
        )
      }

      "TypeOfIdentificationOfMeansOfTransport" in {
        val xml =
          <ns3:RDEntry>
            <ns4:RDEntryStatus>
              <ns5:state>valid</ns5:state>
              <ns5:activeFrom>2024-07-09</ns5:activeFrom>
            </ns4:RDEntryStatus>
            <ns4:dataItem name="TypeOfIdentification">10</ns4:dataItem>
            <ns4:LsdList>
              <ns7:description lang="en">IMO Ship Identification Number</ns7:description>
            </ns4:LsdList>
          </ns3:RDEntry>

        val result = CodeList("TypeOfIdentificationOfMeansOfTransport").value

        result.name mustBe "TypeOfIdentificationOfMeansOfTransport"

        result.source mustBe RefDataFeed

        result.json(xml) mustBe Seq(
          Json.obj(
            "state"       -> "valid",
            "activeFrom"  -> "2024-07-09",
            "type"        -> "10",
            "description" -> "IMO Ship Identification Number"
          )
        )
      }

      "TypeOfIdentificationOfMeansOfTransportActive" in {
        val xml =
          <ns3:RDEntry>
            <ns4:RDEntryStatus>
              <ns5:state>valid</ns5:state>
              <ns5:activeFrom>2024-05-29</ns5:activeFrom>
            </ns4:RDEntryStatus>
            <ns4:dataItem name="TypeOfIdentificationofMeansOfTransportActiveCode">10</ns4:dataItem>
            <ns4:LsdList>
              <ns7:description lang="en">IMO Ship Identification Number</ns7:description>
            </ns4:LsdList>
          </ns3:RDEntry>

        val result = CodeList("TypeOfIdentificationofMeansOfTransportActive").value

        result.name mustBe "TypeOfIdentificationofMeansOfTransportActive"

        result.source mustBe RefDataFeed

        result.json(xml) mustBe Seq(
          Json.obj(
            "state"       -> "valid",
            "activeFrom"  -> "2024-05-29",
            "code"        -> "10",
            "description" -> "IMO Ship Identification Number"
          )
        )
      }

      "TypeOfLocation" in {
        val xml =
          <ns3:RDEntry>
            <ns4:RDEntryStatus>
              <ns5:state>valid</ns5:state>
              <ns5:activeFrom>2024-03-18</ns5:activeFrom>
            </ns4:RDEntryStatus>
            <ns4:dataItem name="TypeOfLocation">A</ns4:dataItem>
            <ns4:LsdList>
              <ns7:description lang="en">Designated location</ns7:description>
            </ns4:LsdList>
          </ns3:RDEntry>

        val result = CodeList("TypeOfLocation").value

        result.name mustBe "TypeOfLocation"

        result.source mustBe RefDataFeed

        result.json(xml) mustBe Seq(
          Json.obj(
            "state"       -> "valid",
            "activeFrom"  -> "2024-03-18",
            "type"        -> "A",
            "description" -> "Designated location"
          )
        )
      }

      "UnDangerousGoodsCode" in {
        val xml =
          <ns3:RDEntry>
            <ns4:RDEntryStatus>
              <ns5:state>valid</ns5:state>
              <ns5:activeFrom>2024-02-21</ns5:activeFrom>
            </ns4:RDEntryStatus>
            <ns4:dataItem name="UnDangerousGoodsCode">0004</ns4:dataItem>
            <ns4:LsdList>
              <ns7:description lang="en">AMMONIUM PICRATE dry or wetted with less than 10% water, by mass</ns7:description>
            </ns4:LsdList>
          </ns3:RDEntry>

        val result = CodeList("UnDangerousGoodsCode").value

        result.name mustBe "UnDangerousGoodsCode"

        result.source mustBe RefDataFeed

        result.json(xml) mustBe Seq(
          Json.obj(
            "state"       -> "valid",
            "activeFrom"  -> "2024-02-21",
            "code"        -> "0004",
            "description" -> "AMMONIUM PICRATE dry or wetted with less than 10% water, by mass"
          )
        )
      }

      "UnLocodeExtended" in {
        val xml =
          <ns3:RDEntry>
            <ns4:RDEntryStatus>
              <ns5:state>valid</ns5:state>
              <ns5:activeFrom>2023-04-13</ns5:activeFrom>
            </ns4:RDEntryStatus>
            <ns4:dataItem name="UnLocodeExtendedCode">ADALV</ns4:dataItem>
            <ns4:dataItem name="Name">Andorra la Vella</ns4:dataItem>
            <ns4:dataItem name="Function">--34-6--</ns4:dataItem>
            <ns4:dataItem name="Status">AI</ns4:dataItem>
            <ns4:dataItem name="Date">0601</ns4:dataItem>
            <ns4:dataItem name="Coordinates">4230N 00131E</ns4:dataItem>
            <ns4:dataItem name="Comment">Muy Vella</ns4:dataItem>
          </ns3:RDEntry>

        val result = CodeList("UnLocodeExtended").value

        result.name mustBe "UnLocodeExtended"

        result.source mustBe RefDataFeed

        result.json(xml) mustBe Seq(
          Json.obj(
            "state"                -> "valid",
            "activeFrom"           -> "2023-04-13",
            "unLocodeExtendedCode" -> "ADALV",
            "name"                 -> "Andorra la Vella"
          )
        )
      }

      "Unit" in {
        val xml =
          <ns3:RDEntry>
            <ns4:RDEntryStatus>
              <ns5:state>valid</ns5:state>
              <ns5:activeFrom>2024-07-09</ns5:activeFrom>
            </ns4:RDEntryStatus>
            <ns4:dataItem name="Unit">ASV</ns4:dataItem>
            <ns4:LsdList>
              <ns7:description lang="en">%vol</ns7:description>
            </ns4:LsdList>
          </ns3:RDEntry>

        val result = CodeList("Unit").value

        result.name mustBe "Unit"

        result.source mustBe RefDataFeed

        result.json(xml) mustBe Seq(
          Json.obj(
            "state"       -> "valid",
            "activeFrom"  -> "2024-07-09",
            "code"        -> "ASV",
            "description" -> "%vol"
          )
        )
      }
    }

    "when invalid code list" in {
      forAll(Gen.alphaNumStr) {
        str =>
          val result = CodeList(str)
          result mustBe None
      }
    }
  }
}
