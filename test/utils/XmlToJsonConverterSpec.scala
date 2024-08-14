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

package utils

import base.SpecBase
import org.mockito.Mockito.when
import play.api.libs.json.Json
import services.TimeService
import services.UUIDService
import utils.XmlToJsonConverter.CustomsOfficeListXmlToJsonConverter
import utils.XmlToJsonConverter.ReferenceDataListXmlToJsonConverter

import java.time.LocalDate
import java.util.UUID

class XmlToJsonConverterSpec extends SpecBase {

  private val mockUUIDService: UUIDService = mock[UUIDService]

  private val mockTimeService: TimeService = mock[TimeService]

  "XmlToJsonConverter" - {
    "must convert XML to JSON" - {
      "when COL code lists" in {
        val xml =
          <ns6:RDEntityList xmlns:ns0="http://xmlns.ec.eu/BusinessObjects/CSRD2/RDEntityEntryListType/V2" xmlns:ns1="http://xmlns.ec.eu/BusinessObjects/CSRD2/RDEntityType/V2" xmlns:ns2="http://xmlns.ec.eu/BusinessObjects/CSRD2/RDEntryType/V2" xmlns:ns3="http://xmlns.ec.eu/BusinessObjects/CSRD2/RDStatusType/V2" xmlns:ns4="http://xmlns.ec.eu/BusinessObjects/CSRD2/RDValidityPeriodType/V2" xmlns:ns5="http://xmlns.ec.eu/BusinessObjects/CSRD2/LsdListType/V2" xmlns:ns6="http://xmlns.ec.eu/BusinessObjects/CSRD2/ReferenceDataSubscriptionReceiverCBSServiceType/V2">
            <ns0:RDEntity name="CountryCodesFullList">
              <ns1:RDEntry>
                <ns2:RDEntryStatus>
                  <ns3:state>valid</ns3:state>
                  <ns3:activeFrom>2024-02-21</ns3:activeFrom>
                </ns2:RDEntryStatus>
                <ns2:dataItem name="CountryCode">AD</ns2:dataItem>
                <ns2:dataItem name="TccEntryDate">19000101</ns2:dataItem>
                <ns2:dataItem name="NctsEntryDate">19000101</ns2:dataItem>
                <ns2:dataItem name="GeoNomenclatureCode">043</ns2:dataItem>
                <ns2:dataItem name="CountryRegimeCode">TOC</ns2:dataItem>
                <ns2:LsdList>
                  <ns5:description lang="bg">Андора</ns5:description>
                  <ns5:description lang="cs">Andorra</ns5:description>
                  <ns5:description lang="da">Andorra</ns5:description>
                  <ns5:description lang="de">Andorra</ns5:description>
                  <ns5:description lang="el">Ανδόρα</ns5:description>
                  <ns5:description lang="en">Andorra</ns5:description>
                  <ns5:description lang="es">Andorra</ns5:description>
                  <ns5:description lang="et">Andorra</ns5:description>
                  <ns5:description lang="fi">Andorra</ns5:description>
                  <ns5:description lang="fr">Andorre</ns5:description>
                  <ns5:description lang="hr">Andora</ns5:description>
                  <ns5:description lang="hu">Andorra</ns5:description>
                  <ns5:description lang="is">Andorra</ns5:description>
                  <ns5:description lang="it">Andorra</ns5:description>
                  <ns5:description lang="lt">Andora</ns5:description>
                  <ns5:description lang="lv">Andora</ns5:description>
                  <ns5:description lang="mk">Андора</ns5:description>
                  <ns5:description lang="mt">Andorra</ns5:description>
                  <ns5:description lang="nl">Andorra</ns5:description>
                  <ns5:description lang="no">Andorra</ns5:description>
                  <ns5:description lang="pl">Andora</ns5:description>
                  <ns5:description lang="pt">Andorra</ns5:description>
                  <ns5:description lang="ro">Andorra</ns5:description>
                  <ns5:description lang="sk">Andora</ns5:description>
                  <ns5:description lang="sl">Andora</ns5:description>
                  <ns5:description lang="sr">Kneževina Andora</ns5:description>
                  <ns5:description lang="sv">Andorra</ns5:description>
                  <ns5:description lang="tr">Andorra</ns5:description>
                </ns2:LsdList>
              </ns1:RDEntry>
            </ns0:RDEntity>
            <ns0:RDEntity name="CountryRegion">
              <ns1:RDEntry>
                <ns2:RDEntryStatus>
                  <ns3:state>valid</ns3:state>
                  <ns3:activeFrom>1900-01-01</ns3:activeFrom>
                </ns2:RDEntryStatus>
                <ns2:dataItem name="CountryCode">BE</ns2:dataItem>
                <ns2:dataItem name="CountryRegionCode">BRU</ns2:dataItem>
                <ns2:dataGroup name="CountryRegionLsd">
                  <ns2:dataItem name="LanguageCode">DE</ns2:dataItem>
                  <ns2:dataItem name="CountryRegionName">REGION BRUESSEL HAUPTSTADT</ns2:dataItem>
                </ns2:dataGroup>
                <ns2:dataGroup name="CountryRegionLsd">
                  <ns2:dataItem name="LanguageCode">FR</ns2:dataItem>
                  <ns2:dataItem name="CountryRegionName">REGION DE BRUXELLES-CAPITALE</ns2:dataItem>
                </ns2:dataGroup>
                <ns2:dataGroup name="CountryRegionLsd">
                  <ns2:dataItem name="LanguageCode">NL</ns2:dataItem>
                  <ns2:dataItem name="CountryRegionName">BRUSSELS HOOFDSTEDELIJK GEWEST</ns2:dataItem>
                </ns2:dataGroup>
              </ns1:RDEntry>
            </ns0:RDEntity>
            <ns0:RDEntity name="CustomsOffices">
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
            </ns0:RDEntity>
          </ns6:RDEntityList>

        val uuid = "6e9e98cc-70b6-4055-b7e9-e258625e45e8"
        when(mockUUIDService.randomUUID()).thenReturn(UUID.fromString(uuid))

        when(mockTimeService.currentDate()).thenReturn(LocalDate.of(1996, 3, 2))

        val converter = new CustomsOfficeListXmlToJsonConverter(mockUUIDService, mockTimeService)

        val result = converter.convert(xml)

        result mustBe Json.parse(s"""
            |{
            |  "messageInformation" : {
            |    "messageID" : "$uuid",
            |    "snapshotDate" : "1996-03-02"
            |  },
            |  "lists" : {
            |    "customsOffices" : {
            |      "listName" : "CustomsOffices",
            |      "listEntries" : [
            |        {
            |          "state" : "valid",
            |          "activeFrom" : "2020-03-23",
            |          "languageCode" : "EN",
            |          "name" : "CUSTOMS OFFICE SANT JULIÀ DE LÒRIA",
            |          "phoneNumber" : "+ (376) 84 1090",
            |          "id" : "AD000001",
            |          "countryId" : "AD",
            |          "roles" : [
            |            {
            |              "role" : "AUT"
            |            },
            |            {
            |              "role" : "DEP"
            |            },
            |            {
            |              "role" : "DES"
            |            },
            |            {
            |              "role" : "TRA"
            |            }
            |          ]
            |        },
            |        {
            |          "state" : "valid",
            |          "activeFrom" : "2020-03-23",
            |          "languageCode" : "ES",
            |          "name" : "ADUANA DE ST. JULIÀ DE LÒRIA",
            |          "phoneNumber" : "+ (376) 84 1090",
            |          "id" : "AD000001",
            |          "countryId" : "AD",
            |          "roles" : [
            |            {
            |              "role" : "AUT"
            |            },
            |            {
            |              "role" : "DEP"
            |            },
            |            {
            |              "role" : "DES"
            |            },
            |            {
            |              "role" : "TRA"
            |            }
            |          ]
            |        },
            |        {
            |          "state" : "valid",
            |          "activeFrom" : "2020-03-23",
            |          "languageCode" : "FR",
            |          "name" : "BUREAU DE SANT JULIÀ DE LÒRIA",
            |          "phoneNumber" : "+ (376) 84 1090",
            |          "id" : "AD000001",
            |          "countryId" : "AD",
            |          "roles" : [
            |            {
            |              "role" : "AUT"
            |            },
            |            {
            |              "role" : "DEP"
            |            },
            |            {
            |              "role" : "DES"
            |            },
            |            {
            |              "role" : "TRA"
            |            }
            |          ]
            |        }
            |      ]
            |    }
            |  }
            |}
            |""".stripMargin)
      }

      "when RD code lists" in {
        val xml =
          <ns8:ExtractValidReferenceDataRespMsg xsi:schemaLocation="" xmlns:ns0="http://xmlns.ec.eu/BusinessObjects/CSRD2/ReferenceDataExportBASServiceType/V2" xmlns:ns1="http://xmlns.ec.eu/BusinessObjects/CSRD2/MessageHeaderType/V2" xmlns:ns2="http://xmlns.ec.eu/BusinessObjects/CSRD2/RDEntityEntryListType/V2" xmlns:ns3="http://xmlns.ec.eu/BusinessObjects/CSRD2/RDEntityType/V2" xmlns:ns4="http://xmlns.ec.eu/BusinessObjects/CSRD2/RDEntryType/V2" xmlns:ns5="http://xmlns.ec.eu/BusinessObjects/CSRD2/RDStatusType/V2" xmlns:ns6="http://xmlns.ec.eu/BusinessObjects/CSRD2/RDValidityPeriodType/V2" xmlns:ns7="http://xmlns.ec.eu/BusinessObjects/CSRD2/LsdListType/V2" xmlns:ns8="http://xmlns.ec.eu/BusinessActivityService/CSRD2/IReferenceDataExportBAS/V2" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
            <ns0:MessageHeader>
              <ns1:AddressingInformation>
                <ns1:messageID>2452e854-d987-4b19-ac09-65992c47f1b1</ns1:messageID>
                <ns1:relatesTo>3239777d-1576-48f4-90e4-c4ee38700b9b</ns1:relatesTo>
              </ns1:AddressingInformation>
              <ns1:RequestInformation>
                <ns4:ExtractValidReferenceDataReqMsg xmlns:ns6="http://xmlns.ec.eu/BusinessObjects/CSRD2/ExtractType/V2" xmlns:ns5="http://xmlns.ec.eu/BusinessObjects/CSRD2/OperationType/V2" xmlns:ns8="http://xmlns.ec.eu/BusinessObjects/CSRD2/ExportingEntitiesType/V2" xmlns:ns12="http://xmlns.ec.eu/BusinessObjects/CSRD2/FilterRefType/V2" xmlns:ns10="http://xmlns.ec.eu/BusinessObjects/CSRD2/RDEntityFilterPairType/V2" xmlns:ns16="http://xmlns.ec.eu/BusinessObjects/CSRD2/LanguageDescriptionsType/V2" xmlns:ns15="http://xmlns.ec.eu/BusinessObjects/CSRD2/SynchronisationExtractType/V2" xmlns:ns14="http://xmlns.ec.eu/BusinessObjects/CSRD2/RetrieveType/V2" xmlns:ns2="http://xmlns.ec.eu/BusinessObjects/CSRD2/ExportType/V2" xmlns:ns4="http://xmlns.ec.eu/BusinessActivityService/CSRD2/IReferenceDataExportBAS/V2" xmlns:ns3="http://xmlns.ec.eu/BusinessObjects/CSRD2/StatementsType/V2">
                  <ns0:MessageHeader>
                    <ns1:AddressingInformation>
                      <ns1:messageID>3239777d-1576-48f4-90e4-c4ee38700b9b</ns1:messageID>
                    </ns1:AddressingInformation>
                  </ns0:MessageHeader>
                  <ns0:ExportType>
                    <ns2:Operation>
                      <ns5:Extract>
                        <ns6:SnapshotDate value="2024-08-12"/>
                      </ns5:Extract>
                    </ns2:Operation>
                  </ns0:ExportType>
                </ns4:ExtractValidReferenceDataReqMsg>
              </ns1:RequestInformation>
            </ns0:MessageHeader>
            <ns0:RDEntityList>
              <ns2:RDEntity name="AdditionalInformation">
                <ns3:RDEntry>
                  <ns4:RDEntryStatus>
                    <ns5:state>valid</ns5:state>
                    <ns5:activeFrom>2024-06-20</ns5:activeFrom>
                  </ns4:RDEntryStatus>
                  <ns4:dataItem name="AdditionalInformationCode">00200</ns4:dataItem>
                  <ns4:LsdList>
                    <ns7:description lang="en">Several occurrences of documents and parties</ns7:description>
                    <ns7:description lang="sk">Niekoľko výskytov dokumentov a strán</ns7:description>
                  </ns4:LsdList>
                </ns3:RDEntry>
              </ns2:RDEntity>
              <ns2:RDEntity name="AdditionalReference">
                <ns3:RDEntry>
                  <ns4:RDEntryStatus>
                    <ns5:state>valid</ns5:state>
                    <ns5:activeFrom>2024-01-18</ns5:activeFrom>
                  </ns4:RDEntryStatus>
                  <ns4:dataItem name="DocumentType">C651</ns4:dataItem>
                  <ns4:LsdList>
                    <ns7:description lang="bg">Електронен административен документ (e-АД) в съответствие с член 3, параграф 1 от Регламент (ЕО) № 684/2009</ns7:description>
                    <ns7:description lang="cs">Elektronický správní doklad (e-AD) podle čl. 3 odst. 1 nařízení (ES) č. 684/2009</ns7:description>
                    <ns7:description lang="da">Det elektroniske administrative dokument (e-AD), som er nævnt i artikel 3, stk. 1, i forordning (EF) nr. 684/2009</ns7:description>
                    <ns7:description lang="de">Elektronisches Verwaltungsdokument (e-VD) gemäß Artikel 3 Absatz 1 der Verordnung (EG) Nr. 684/2009</ns7:description>
                    <ns7:description lang="el">Ηλεκτρονικό διοικητικό έγγραφο (e-AD), όπως αναφέρεται στο άρθρο 3 παράγραφος 1 του κανονισμού (EΚ) αριθ. 684/2009</ns7:description>
                    <ns7:description lang="en">Electronic administrative document (e-AD), as referred to in Article 3(1) of Reg. (EC) No 684/2009</ns7:description>
                    <ns7:description lang="es">Documento administrativo electrónico (e-AD), según lo dispuesto en el artículo 3, apartado 1), del Reglamento (CE) n.º 684/2009</ns7:description>
                    <ns7:description lang="et">Elektrooniline haldusdokument, millele on osutatud määruse (EÜ) nr 684/2009 artikli 3 lõikes 1</ns7:description>
                    <ns7:description lang="fi">Asetuksen (EY) N:o 684/2009 3 artiklan 1 kohdassa tarkoitettu sähköinen hallinnollinen asiakirja (e-AD)</ns7:description>
                    <ns7:description lang="fr">Document administratif électronique (e-AD), tel que visé à l’Article 3.1) du règlement (CE) n° 684/2009</ns7:description>
                    <ns7:description lang="hr">Elektronički trošarinski dokument (e-TD) iz članka 3. stavka 1. Uredbe (EZ) br. 684/2009</ns7:description>
                    <ns7:description lang="hu">a 684/2009/EK rendelet 3. cikkének (1) bekezdése szerinti elektronikus adminisztratív okmány (EAO)</ns7:description>
                    <ns7:description lang="it">Documento amministrativo elettronico (e-AD) di cui all'articolo 3, paragrafo 1, del regolamento (CE) n. 684/2009</ns7:description>
                    <ns7:description lang="lt">Elektroninis administracinis dokumentas (e-AD), nurodytas Komisijos Deleguotojo Reglamento (ES) 2022/1636 3 straipsnio</ns7:description>
                    <ns7:description lang="lv">Regulas (EK) Nr. 684/2009 3. panta 1. punktā minētais elektroniskais administratīvais dokuments (e-AD)</ns7:description>
                    <ns7:description lang="nl">Elektronisch administratief document (e-AD), als bedoeld in artikel 3, lid 1, van Verordening (EG) nr. 684/2009</ns7:description>
                    <ns7:description lang="pl">Elektroniczny dokument administracyjny (e-AD), o którym mowa w art. 3 ust. 1 rozporządzenia (WE) nr 684/2009</ns7:description>
                    <ns7:description lang="pt">Documento Administrativo eletrónico (e-AD), como referido no artigo 3.º, n.º 1, do Regulamento (CE) n.º 684/2009</ns7:description>
                    <ns7:description lang="ro">Document administrativ electronic (e-AD), menționat la articolul 3 alineatul (1) din Regulamentul (CE) nr. 684/2009</ns7:description>
                    <ns7:description lang="sk">Elektronický administratívny dokument (e-AD) uvedený v článku 3 ods. 1 nariadenia (ES) č. 684/2009</ns7:description>
                    <ns7:description lang="sl">Elektronski administrativni dokument (e-AD) iz člena 3(1) Uredbe (ES) št. 684/2009</ns7:description>
                    <ns7:description lang="sv">Elektroniskt administrativt dokument (e-AD), enligt artikel 3.1 i förordning (EG) nr 684/2009</ns7:description>
                  </ns4:LsdList>
                </ns3:RDEntry>
              </ns2:RDEntity>
            </ns0:RDEntityList>
          </ns8:ExtractValidReferenceDataRespMsg>

        val uuid = "6e9e98cc-70b6-4055-b7e9-e258625e45e8"
        when(mockUUIDService.randomUUID()).thenReturn(UUID.fromString(uuid))

        when(mockTimeService.currentDate()).thenReturn(LocalDate.of(1996, 3, 2))

        val converter = new ReferenceDataListXmlToJsonConverter(mockUUIDService, mockTimeService)

        val result = converter.convert(xml)

        result mustBe Json.parse(s"""
            |{
            |  "messageInformation" : {
            |    "messageID" : "$uuid",
            |    "snapshotDate" : "1996-03-02"
            |  },
            |  "lists" : {
            |    "AdditionalInformation" : {
            |      "listName" : "AdditionalInformation",
            |      "listEntries" : [
            |        {
            |          "state" : "valid",
            |          "activeFrom" : "2024-06-20",
            |          "code" : "00200",
            |          "description" : "Several occurrences of documents and parties"
            |        }
            |      ]
            |    },
            |    "AdditionalReference" : {
            |      "listName" : "AdditionalReference",
            |      "listEntries" : [
            |        {
            |          "state" : "valid",
            |          "activeFrom" : "2024-01-18",
            |          "documentType" : "C651",
            |          "description" : "Electronic administrative document (e-AD), as referred to in Article 3(1) of Reg. (EC) No 684/2009"
            |        }
            |      ]
            |    }
            |  }
            |}
            |""".stripMargin)
      }
    }
  }
}
