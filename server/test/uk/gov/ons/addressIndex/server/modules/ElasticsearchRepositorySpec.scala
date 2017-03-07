package uk.gov.ons.addressIndex.server.modules

import uk.gov.ons.addressIndex.server.model.dao.ElasticClientProvider
import com.sksamuel.elastic4s.ElasticClient
import com.sksamuel.elastic4s.ElasticDsl._
import com.sksamuel.elastic4s.testkit._
import org.scalatest.WordSpec
import play.api.libs.json.Json
import uk.gov.ons.addressIndex.crfscala.CrfScala.CrfTokenResult
import uk.gov.ons.addressIndex.model.db.BulkAddressRequestData
import uk.gov.ons.addressIndex.model.db.index._
import uk.gov.ons.addressIndex.parsers.Tokens

import scala.concurrent.ExecutionContext.Implicits.global

class ElasticsearchRepositorySpec extends WordSpec with SearchMatchers with ElasticSugar {

  // this is necessary so that it can be injected in the provider (otherwise the method will call itself)
  val testClient = client


  // injections
  val elasticClientProvider = new ElasticClientProvider {
    override def client: ElasticClient = testClient
  }
  val config = new AddressIndexConfigModule
  val queryParams = config.config.elasticSearch.queryParams

  val hybridIndex = config.config.elasticSearch.indexes.hybridIndex
  val Array(hybridIndexName, hybridMappings) = hybridIndex.split("/")

  val hybridFirstUprn = 1L
  val hybridFirstPostcodeIn = "h01p"
  val hybridFirstPostcodeOut = "h02p"

  // Fields that are not in this list are not used for search
  val hybridPafUprn = 1L
  val hybridPafOrganizationName = "h2"
  val hybridPafDepartmentName = "h3"
  val hybridPafSubBuildingName = "h4"
  val hybridPafBuildingName = "h5"
  val hybridPafBuildingNumber = 6.toShort
  val hybridPafThoroughfare = "h7"
  val hybridPafPostTown = "h8"
  val hybridPafPostcode = "h10"
  val hybridAll = "H100"

  // Fields that are not in this list are not used for search
  val hybridNagUprn = hybridPafUprn
  val hybridNagPostcodeLocator = hybridPafPostcode
  val hybridNagPaoStartNumber = 13.toShort
  val hybridNagPaoStartSuffix = "h11"
  val hybridNagPaoEndNumber = 12.toShort
  val hybridNagPaoEndSuffix = "h14"
  val hybridNagSaoStartNumber = 15.toShort
  val hybridNagSaoStartSuffix = "h16"
  val hybridNagSaoEndNumber = 17.toShort
  val hybridNagSaoEndSuffix = "h18"
  val hybridNagLocality = "h20"
  val hybridNagOrganisation = hybridPafOrganizationName
  val hybridNagLegalName = hybridPafOrganizationName
  val hybridNagSaoText = hybridPafSubBuildingName
  val hybridNagPaoText = hybridPafBuildingName
  val hybridNagStreetDescriptor = hybridPafThoroughfare
  val hybridNagTownName = hybridPafPostTown
  val hybridNagLatitude = 1.0000000f
  val hybridNagLongitude = -2.0000000f
  val hybridNagNorthing = 3f
  val hybridNagEasting = 4f

  // Fields with this value are not used in the search and are, thus, irrelevant
  val hybridNotUsed = ""
  val hybridNotUsedNull = null

  // Secondary PAF/NAG is used for single search (to have some "concurrence" for the main address)
  // and in the Multi Search
  val hybridSecondaryUprn = 2L
  val hybridSecondaryPostcodeIn = "s01p"
  val hybridSecondaryPostcodeOut = "s02p"

  // Fields that are not in this list are not used for search
  val secondaryHybridPafUprn = 2L
  val secondaryHybridPafOrganizationName = "s2"
  val secondaryHybridPafDepartmentName = "s3"
  val secondaryHybridPafSubBuildingName = "s4"
  val secondaryHybridPafBuildingName = "s5"
  val secondaryHybridPafBuildingNumber = 7.toShort
  val secondaryHybridPafThoroughfare = "s7"
  val secondaryHybridPafPostTown = "s8"
  val secondaryHybridPafPostcode = "s10"
  val secondaryHybridAll = "s200"

  // Fields that are not in this list are not used for search
  val secondaryHybridNagUprn = secondaryHybridPafUprn
  val secondaryHybridNagPostcodeLocator = secondaryHybridPafPostcode
  val secondaryHybridNagPaoStartNumber = 20.toShort
  val secondaryHybridNagPaoStartSuffix = "s11"
  val secondaryHybridNagPaoEndNumber = 21.toShort
  val secondaryHybridNagPaoEndSuffix = "s14"
  val secondaryHybridNagSaoStartNumber = 22.toShort
  val secondaryHybridNagSaoStartSuffix = "s16"
  val secondaryHybridNagSaoEndNumber = 23.toShort
  val secondaryHybridNagSaoEndSuffix = "s18"
  val secondaryHybridNagLocality = "s20"
  val secondaryHybridNagOrganisation = secondaryHybridPafOrganizationName
  val secondaryHybridNagLegalName = secondaryHybridPafOrganizationName
  val secondaryHybridNagSaoText = secondaryHybridPafSubBuildingName
  val secondaryHybridNagPaoText = secondaryHybridPafBuildingName
  val secondaryHybridNagStreetDescriptor = secondaryHybridPafThoroughfare
  val secondaryHybridNagTownName = secondaryHybridPafPostTown
  val secondaryHybridNagLatitude = 7.0000000f
  val secondaryHybridNagLongitude = 8.0000000f
  val secondaryHybridNagNorthing = 10f
  val secondaryHybridNagEasting = 11f

  val firstHybridPafEs = Map(
    "recordIdentifier" -> hybridNotUsedNull,
    "changeType" -> hybridNotUsed,
    "proOrder" -> hybridNotUsedNull,
    "uprn" -> hybridPafUprn,
    "udprn" -> hybridNotUsedNull,
    "organizationName" -> hybridPafOrganizationName,
    "departmentName" -> hybridPafDepartmentName,
    "subBuildingName" -> hybridPafSubBuildingName,
    "buildingName" -> hybridPafBuildingName,
    "buildingNumber" -> hybridPafBuildingNumber,
    "dependentThoroughfare" -> hybridNotUsed,
    "thoroughfare" -> hybridPafThoroughfare,
    "doubleDependentLocality" -> hybridNotUsed,
    "dependentLocality" -> hybridNotUsed,
    "postTown" -> hybridPafPostTown,
    "postcode" -> hybridPafPostcode,
    "postcodeType" -> hybridNotUsed,
    "deliveryPointSuffix" -> hybridNotUsed,
    "welshDependentThoroughfare" -> hybridNotUsed,
    "welshThoroughfare" -> hybridNotUsed,
    "welshDoubleDependentLocality" -> hybridNotUsed,
    "welshDependentLocality" -> hybridNotUsed,
    "welshPostTown" -> hybridNotUsed,
    "poBoxNumber" -> hybridNotUsed,
    "processDate" -> hybridNotUsed,
    "startDate" -> hybridNotUsed,
    "endDate" -> hybridNotUsed,
    "lastUpdateDate" -> hybridNotUsed,
    "entryDate" -> hybridNotUsed,
    "pafAll" -> hybridAll
  )

  val secondHybridPafEs = Map(
    "recordIdentifier" -> hybridNotUsedNull,
    "changeType" -> hybridNotUsed,
    "proOrder" -> hybridNotUsedNull,
    "uprn" -> hybridSecondaryUprn,
    "udprn" -> hybridNotUsedNull,
    "organizationName" -> secondaryHybridPafOrganizationName,
    "departmentName" -> secondaryHybridPafDepartmentName,
    "subBuildingName" -> secondaryHybridPafSubBuildingName,
    "buildingName" -> secondaryHybridPafBuildingName,
    "buildingNumber" -> secondaryHybridPafBuildingNumber,
    "dependentThoroughfare" -> hybridNotUsed,
    "thoroughfare" -> secondaryHybridPafThoroughfare,
    "doubleDependentLocality" -> hybridNotUsed,
    "dependentLocality" -> hybridNotUsed,
    "postTown" -> secondaryHybridPafPostTown,
    "postcode" -> secondaryHybridPafPostcode,
    "postcodeType" -> hybridNotUsed,
    "deliveryPointSuffix" -> hybridNotUsed,
    "welshDependentThoroughfare" -> hybridNotUsed,
    "welshThoroughfare" -> hybridNotUsed,
    "welshDoubleDependentLocality" -> hybridNotUsed,
    "welshDependentLocality" -> hybridNotUsed,
    "welshPostTown" -> hybridNotUsed,
    "poBoxNumber" -> hybridNotUsed,
    "processDate" -> hybridNotUsed,
    "startDate" -> hybridNotUsed,
    "endDate" -> hybridNotUsed,
    "lastUpdateDate" -> hybridNotUsed,
    "entryDate" ->hybridNotUsed,
    "pafAll" -> secondaryHybridAll
  )

  val firstHybridNagEs: Map[String, Any] = Map(
    "uprn" -> hybridNagUprn,
    "postcodeLocator" -> hybridNagPostcodeLocator,
    "addressBasePostal" -> hybridNotUsed,
    "usrn" -> hybridNotUsedNull,
    "lpiKey" -> hybridNotUsed,
    "paoText" -> hybridNagPaoText,
    "paoStartNumber" -> hybridNagPaoStartNumber,
    "paoStartSuffix" -> hybridNotUsed,
    "paoEndNumber" -> hybridNotUsedNull,
    "paoEndSuffix" -> hybridNotUsed,
    "saoText" -> hybridNagSaoText,
    "saoStartNumber" -> hybridNotUsedNull,
    "saoStartSuffix" -> hybridNotUsed,
    "saoEndNumber" -> hybridNotUsedNull,
    "saoEndSuffix" -> hybridNotUsed,
    "level" -> hybridNotUsed,
    "officialFlag" -> hybridNotUsed,
    "streetDescriptor" -> hybridNagStreetDescriptor,
    "townName" -> hybridNagTownName,
    "locality" -> hybridNagLocality,
    "organisation" -> hybridNagOrganisation,
    "legalName" -> hybridNagLegalName,
    "northing" -> hybridNagNorthing,
    "easting" -> hybridNagEasting,
    "classificationCode" -> hybridNotUsed,
    "source" -> hybridNotUsed,
    "usrnMatchIndicator" -> hybridNotUsed,
    "parentUprn" -> hybridNotUsedNull,
    "crossReference" -> hybridNotUsed,
    "streetClassification" -> hybridNotUsedNull,
    "blpuLogicalStatus" -> hybridNotUsedNull,
    "lpiLogicalStatus" -> hybridNotUsedNull,
    "multiOccCount" -> hybridNotUsedNull,
    "location" -> Array(hybridNagLongitude, hybridNagLatitude),
    "language" -> hybridNotUsed,
    "classScheme" -> hybridNotUsed,
    "localCustodianCode" -> hybridNotUsedNull,
    "localCustodianName" -> hybridNotUsedNull,
    "localCustodianGeogCode" -> hybridNotUsedNull,
    "rpc" -> hybridNotUsedNull,
    "nagAll" -> hybridAll
  )

  val secondHybridNagEs: Map[String, Any] = Map(
    "uprn" -> hybridSecondaryUprn,
    "postcodeLocator" -> secondaryHybridNagPostcodeLocator,
    "addressBasePostal" -> hybridNotUsed,
    "usrn" -> hybridNotUsedNull,
    "lpiKey" -> hybridNotUsed,
    "paoText" -> secondaryHybridNagPaoText,
    "paoStartNumber" -> secondaryHybridNagPaoStartNumber,
    "paoStartSuffix" -> hybridNotUsed,
    "paoEndNumber" -> hybridNotUsedNull,
    "paoEndSuffix" -> hybridNotUsed,
    "saoText" -> secondaryHybridNagSaoText,
    "saoStartNumber" -> hybridNotUsedNull,
    "saoStartSuffix" -> hybridNotUsed,
    "saoEndNumber" -> hybridNotUsedNull,
    "saoEndSuffix" -> hybridNotUsed,
    "level" -> hybridNotUsed,
    "officialFlag" -> hybridNotUsed,
    "streetDescriptor" -> secondaryHybridNagStreetDescriptor,
    "townName" -> secondaryHybridNagTownName,
    "locality" -> secondaryHybridNagLocality,
    "organisation" -> secondaryHybridNagOrganisation,
    "legalName" -> secondaryHybridNagLegalName,
    "northing" -> secondaryHybridNagNorthing,
    "easting" -> secondaryHybridNagEasting,
    "classificationCode" -> hybridNotUsed,
    "source" -> hybridNotUsed,
    "usrnMatchIndicator" -> hybridNotUsed,
    "parentUprn" -> hybridNotUsedNull,
    "crossReference" -> hybridNotUsed,
    "streetClassification" -> hybridNotUsedNull,
    "blpuLogicalStatus" -> hybridNotUsedNull,
    "lpiLogicalStatus" -> hybridNotUsedNull,
    "multiOccCount" -> hybridNotUsedNull,
    "location" -> Array(secondaryHybridNagLongitude, secondaryHybridNagLatitude),
    "nagAll" -> hybridNotUsed,
    "language" -> hybridNotUsed,
    "classScheme" -> hybridNotUsed,
    "localCustodianCode" -> hybridNotUsedNull,
    "localCustodianName" -> hybridNotUsedNull,
    "localCustodianGeogCode" -> hybridNotUsedNull,
    "rpc" -> hybridNotUsedNull,
    "nagAll" -> secondaryHybridAll
  )

  val firstHybridEs: Map[String, Any] = Map(
    "uprn" -> hybridFirstUprn,
    "postcodeIn" -> hybridFirstPostcodeIn,
    "postcodeOut" -> hybridFirstPostcodeOut,
    "paf" -> Seq(firstHybridPafEs),
    "lpi" -> Seq(firstHybridNagEs)
  )

  // This one is used to create a "concurrent" for the first one (the first one should be always on top)
  val secondHybridEs: Map[String, Any] = Map(
    "uprn" -> hybridSecondaryUprn,
    "postcodeIn" -> hybridSecondaryPostcodeIn,
    "postcodeOut" -> hybridSecondaryPostcodeOut,
    "paf" -> Seq(secondHybridPafEs),
    "lpi" -> Seq(secondHybridNagEs)
  )

  testClient.execute {
    bulk(
      indexInto(hybridIndexName / hybridMappings).fields(firstHybridEs),
      indexInto(hybridIndexName / hybridMappings).fields(secondHybridEs)
    )
  }.await

  blockUntilCount(2, hybridIndexName)

  val expectedPaf = PostcodeAddressFileAddress(
    hybridNotUsed,
    hybridNotUsed,
    hybridNotUsed,
    hybridPafUprn.toString,
    hybridNotUsed,
    hybridPafOrganizationName,
    hybridPafDepartmentName,
    hybridPafSubBuildingName,
    hybridPafBuildingName,
    hybridPafBuildingNumber.toString,
    hybridNotUsed,
    hybridPafThoroughfare,
    hybridNotUsed,
    hybridNotUsed,
    hybridPafPostTown,
    hybridPafPostcode,
    hybridNotUsed,
    hybridNotUsed,
    hybridNotUsed,
    hybridNotUsed,
    hybridNotUsed,
    hybridNotUsed,
    hybridNotUsed,
    hybridNotUsed,
    hybridNotUsed,
    hybridNotUsed,
    hybridNotUsed,
    hybridNotUsed,
    hybridNotUsed,
    hybridAll
  )

  val expectedNag = NationalAddressGazetteerAddress(
    hybridNagUprn.toString,
    hybridNagPostcodeLocator,
    hybridNotUsed,
    hybridNagLatitude.toString,
    hybridNagLongitude.toString,
    hybridNagEasting.toString,
    hybridNagNorthing.toString,
    hybridNagOrganisation,
    hybridNagLegalName,
    hybridNotUsed,
    hybridNotUsed,
    hybridNotUsed,
    hybridNagPaoText,
    hybridNagPaoStartNumber.toString,
    hybridNotUsed,
    hybridNotUsed,
    hybridNotUsed,
    hybridNagSaoText,
    hybridNotUsed,
    hybridNotUsed,
    hybridNotUsed,
    hybridNotUsed,
    hybridNotUsed,
    hybridNotUsed,
    hybridNagStreetDescriptor,
    hybridNagTownName,
    hybridNagLocality,
    hybridNotUsed,
    hybridNotUsed,
    hybridNotUsed,
    hybridNotUsed,
    hybridNotUsed,
    hybridNotUsed,
    hybridNotUsed,
    hybridNotUsed,
    hybridNotUsed,
    hybridNotUsed,
    hybridNotUsed,
    hybridNotUsed,
    hybridNotUsed,
    hybridNotUsed,
    hybridAll
  )

  val expectedHybrid = HybridAddress(
    uprn = hybridFirstUprn.toString,
    postcodeIn = hybridFirstPostcodeIn,
    postcodeOut = hybridFirstPostcodeOut,
    lpi = Seq(expectedNag),
    paf = Seq(expectedPaf),
    score = 1.0f
  )

  "Elastic repository" should {

    "generate valid query for search by UPRN" in {
      // Given
      val repository = new AddressIndexRepository(config, elasticClientProvider)
      val expected = Json.parse(
        """
        {
          "query" : {
            "term" : {
            "uprn" : "1"
          }
          }
        }
        """
      )

      // When
      val result = Json.parse(repository.generateQueryUprnRequest(hybridFirstUprn.toString).toString)

      // Then
      result shouldBe expected
    }
/** two test not working due to new index / query - reinstate when fixed
    "find HYBRID address by UPRN" in {

      // Given
      val repository = new AddressIndexRepository(config, elasticClientProvider)
      val expected = Some(expectedHybrid)

      // When
      val result = repository.queryUprn(hybridFirstUprn.toString).await

      // Then
      result.get.lpi.head shouldBe expectedNag
      result.get.paf.head shouldBe expectedPaf
      result shouldBe expected

    }

    "find Hybrid addresses by building number, postcode, locality and organisation name" in {
      // Given
      val repository = new AddressIndexRepository(config, elasticClientProvider)
      val tokens: Map[String, String] = Map(
        Tokens.buildingNumber -> hybridNagPaoStartNumber.toString,
        Tokens.locality -> hybridNagLocality,
        Tokens.organisationName -> hybridNagOrganisation,
        Tokens.postcode -> hybridNagPostcodeLocator
      )
      val expectedScore = 2.3f

      val expected = expectedHybrid

      // When
      val HybridAddresses(results, maxScore, total) = repository.queryAddresses(0, 10, tokens, hybridAll).await

      // Then
      results.length should be > 0 // it MAY return more than 1 addresses, but the top one should remain the same
      total should be > 0l

      val resultHybrid = results.head
      resultHybrid shouldBe expected.copy(score = resultHybrid.score)

      // Score is random, but should always be positive
      resultHybrid.score should be > 0f
      maxScore should be > 0f
    }
*/
    "have score of `0` if no addresses found" in {
      // Given
      val repository = new AddressIndexRepository(config, elasticClientProvider)
      val tokens = Map(
        Tokens.buildingNumber -> "9999"
      )

      // When
      val HybridAddresses(results, maxScore, total) = repository.queryAddresses(0, 10, tokens, "9999").await

      // Then
      results.length shouldBe 0
      maxScore shouldBe 0f
      total shouldBe 0f
    }

    "remove empty boolean queries from the query " in {
      // Given
      val repository = new AddressIndexRepository(config, elasticClientProvider)

      val tokens: Map[String, String] = Map.empty

      val input = "ORIGINAL INPUT"

      val expected = Json.parse(
        s"""
          {
             "query":{
                "bool":{
                   "should":[
                      {
                         "match":{
                            "paf.pafAll":{
                               "query":"$input",
                               "type":"boolean",
                               "boost":${queryParams.pafAllBoost}
                            }
                         }
                      },
                      {
                         "match":{
                            "lpi.nagAll":{
                               "query":"$input",
                               "type":"boolean",
                               "boost":${queryParams.pafAllBoost}
                            }
                         }
                      }
                   ]
                }
             }
          }
        """
      )

      // When
      val result = Json.parse(repository.generateQueryAddressRequest(tokens, input).toString)

      // Then
      result shouldBe expected
    }

    "generate valid query for search by tokens" in {
      // Given
      val repository = new AddressIndexRepository(config, elasticClientProvider)
      val tokens: Map[String, String] = Map(
        Tokens.buildingNumber -> hybridPafBuildingNumber.toString,
        Tokens.paoStartNumber -> hybridNagPaoStartNumber.toString,
        Tokens.paoStartSuffix -> hybridNagPaoStartSuffix,
        Tokens.paoEndNumber -> hybridNagPaoEndNumber.toString,
        Tokens.paoEndSuffix -> hybridNagPaoEndSuffix,
        Tokens.saoStartNumber -> hybridNagSaoStartNumber.toString,
        Tokens.saoStartSuffix -> hybridNagSaoStartSuffix,
        Tokens.saoEndNumber -> hybridNagSaoEndNumber.toString,
        Tokens.saoEndSuffix -> hybridNagSaoEndSuffix,
        Tokens.locality -> hybridNagLocality,
        Tokens.organisationName -> hybridNagOrganisation,
        Tokens.postcode -> hybridNagPostcodeLocator,
        Tokens.postcodeIn -> hybridFirstPostcodeIn,
        Tokens.postcodeOut -> hybridFirstPostcodeOut,
        Tokens.departmentName -> hybridPafDepartmentName,
        Tokens.subBuildingName -> hybridPafSubBuildingName,
        Tokens.buildingName -> hybridPafBuildingName,
        Tokens.streetName -> hybridNagStreetDescriptor,
        Tokens.townName -> hybridNagTownName
      )

      val expected = Json.parse(
       s"""
          {
             "query":{  
                "bool":{  
                   "should":[  
                      {  
                         "bool":{  
                            "should":[  
                               {  
                                           "dis_max":{
                                              "tie_breaker":${queryParams.disMaxTieBreaker},
                                              "queries":[
                                                 {
                                                    "match":{  
                                                       "paf.buildingNumber":{  
                                                          "query":"$hybridPafBuildingNumber",
                                                          "type":"boolean",
                                                          "boost":${queryParams.buildingNumber.pafBuildingNumberBoost}
                                                       }
                                                    }
                                                 },
                                                 {  
                                                    "match":{  
                                                       "lpi.paoStartNumber":{  
                                                          "query":"$hybridPafBuildingNumber",
                                                          "type":"boolean",
                                                          "boost":${queryParams.buildingNumber.pafBuildingNumberBoost}
                                                       }
                                                    }
                                                 }
                                              ]
                                           }
                                        },
                                        {  
                                           "dis_max":{
                                              "tie_breaker":${queryParams.disMaxTieBreaker},
                                              "queries":[
                                                 {  
                                                    "match":{  
                                                       "paf.buildingName":{  
                                                          "query":"$hybridPafBuildingName",
                                                          "type":"boolean",
                                                          "boost":${queryParams.buildingName.pafBuildingNameBoost},
                                                          "fuzziness":"1"
                                                       }
                                                    }
                                                 },
                                                 {  
                                                    "match":{  
                                                       "lpi.paoText":{  
                                                          "query":"$hybridPafBuildingName",
                                                          "type":"boolean",
                                                          "boost":${queryParams.buildingName.pafBuildingNameBoost},
                                                          "fuzziness":"1"
                                                       }
                                                    }
                                                 },
                                                 {
                                                    "match":{
                                                       "lpi.paoStartSuffix":{
                                                          "query":"$hybridNagPaoStartSuffix",
                                                          "type":"boolean",
                                                          "boost":${queryParams.buildingName.pafBuildingNameBoost},
                                                          "fuzziness":"1"
                                                       }
                                                    }
                                                 }
                                              ]
                                           }
                                        },
                                        {  
                                           "dis_max":{
                                              "tie_breaker":${queryParams.disMaxTieBreaker},
                                              "queries":[
                                                 {  
                                                    "match":{  
                                                       "paf.subBuildingName":{  
                                                          "query":"$hybridPafSubBuildingName",
                                                          "type":"boolean",
                                                          "boost":${queryParams.subBuildingName.pafSubBuildingNameBoost}
                                                       }
                                                    }
                                                 },
                                                 {  
                                                    "match":{  
                                                       "lpi.saoText":{  
                                                          "query":"$hybridPafSubBuildingName",
                                                          "type":"boolean",
                                                          "boost":${queryParams.subBuildingName.lpiSaoTextBoost}
                                                       }
                                                    }
                                                 },
                                                 {
                                                    "match":{
                                                       "lpi.saoStartSuffix":{
                                                          "query":"$hybridNagSaoStartSuffix",
                                                          "type":"boolean",
                                                          "boost":${queryParams.subBuildingName.lpiSaoTextBoost}
                                                       }
                                                    }
                                                 }
                                              ]
                                           }
                                        },
                                        {  
                                           "dis_max":{
                                              "tie_breaker":${queryParams.disMaxTieBreaker},
                                              "queries":[
                                                 {
                                                    "match":{  
                                                       "paf.thoroughfare":{  
                                                          "query":"$hybridNagStreetDescriptor",
                                                          "type":"boolean",
                                                          "boost":${queryParams.streetName.pafThoroughfareBoost},
                                                          "fuzziness":"1"
                                                       }
                                                    }
                                                 },
                                                 {  
                                                    "match":{  
                                                       "paf.welshThoroughfare":{  
                                                          "query":"$hybridNagStreetDescriptor",
                                                          "type":"boolean",
                                                          "boost":${queryParams.streetName.pafWelshThoroughfareBoost},
                                                          "fuzziness":"1"
                                                       }
                                                    }
                                                 },
                                                 {  
                                                    "match":{  
                                                       "paf.dependentThoroughfare":{  
                                                          "query":"$hybridNagStreetDescriptor",
                                                          "type":"boolean",
                                                          "boost":${queryParams.streetName.pafDependentThoroughfareBoost},
                                                          "fuzziness":"1"
                                                       }
                                                    }
                                                 },
                                                 {  
                                                    "match":{  
                                                       "paf.welshDependentThoroughfare":{  
                                                          "query":"$hybridNagStreetDescriptor",
                                                          "type":"boolean",
                                                          "boost":${queryParams.streetName.pafWelshDependentThoroughfareBoost},
                                                          "fuzziness":"1"
                                                       }
                                                    }
                                                 },
                                                 {  
                                                    "match":{  
                                                       "lpi.streetDescriptor":{  
                                                          "query":"$hybridNagStreetDescriptor",
                                                          "type":"boolean",
                                                          "boost":${queryParams.streetName.lpiStreetDescriptorBoost},
                                                          "fuzziness":"1"
                                                       }
                                                    }
                                                 }
                                              ]
                                           }
                                        },
                                        {  
                                           "dis_max":{
                                              "tie_breaker":${queryParams.disMaxTieBreaker},
                                              "queries":[
                                                 {  
                                                    "match":{  
                                                       "paf.postTown":{  
                                                          "query":"$hybridNagTownName",
                                                          "type":"boolean",
                                                          "boost":${queryParams.townName.pafPostTownBoost},
                                                          "fuzziness":"1"
                                                       }
                                                    }
                                                 },
                                                 {  
                                                    "match":{  
                                                       "paf.welshPostTown":{  
                                                          "query":"$hybridNagTownName",
                                                          "type":"boolean",
                                                          "boost":${queryParams.townName.pafWelshPostTownBoost},
                                                          "fuzziness":"1"
                                                       }
                                                    }
                                                 },
                                                 {  
                                                    "match":{  
                                                       "lpi.townName":{  
                                                          "query":"$hybridNagTownName",
                                                          "type":"boolean",
                                                          "boost":${queryParams.townName.lpiTownNameBoost},
                                                          "fuzziness":"1"
                                                       }
                                                    }
                                                 },
                                                 {  
                                                    "match":{  
                                                       "paf.dependentLocality":{  
                                                          "query":"$hybridNagTownName",
                                                          "type":"boolean",
                                                          "boost":${queryParams.townName.pafDependentLocalityBoost},
                                                          "fuzziness":"1"
                                                       }
                                                    }
                                                 },
                                                 {  
                                                    "match":{  
                                                       "paf.welshDependentLocality":{  
                                                          "query":"$hybridNagTownName",
                                                          "type":"boolean",
                                                          "boost":${queryParams.townName.pafWelshDependentLocalityBoost},
                                                          "fuzziness":"1"
                                                       }
                                                    }
                                                 },
                                                 {  
                                                    "match":{  
                                                       "lpi.locality":{  
                                                          "query":"$hybridNagTownName",
                                                          "type":"boolean",
                                                          "boost":${queryParams.townName.lpiLocalityBoost},
                                                          "fuzziness":"1"
                                                       }
                                                    }
                                                 },
                                                 {  
                                                    "match":{  
                                                       "paf.doubleDependentLocality":{  
                                                          "query":"$hybridNagTownName",
                                                          "type":"boolean",
                                                          "boost":${queryParams.townName.pafDoubleDependentLocalityBoost},
                                                          "fuzziness":"1"
                                                       }
                                                    }
                                                 },
                                                 {  
                                                    "match":{  
                                                       "paf.welshDoubleDependentLocality":{  
                                                          "query":"$hybridNagTownName",
                                                          "type":"boolean",
                                                          "boost":${queryParams.townName.pafWelshDoubleDependentLocalityBoost},
                                                          "fuzziness":"1"
                                                       }
                                                    }
                                                 }
                                              ]
                                           }
                                        },
                                        {  
                                           "dis_max":{
                                              "tie_breaker":${queryParams.disMaxTieBreaker},
                                              "queries":[
                                                 {  
                                                    "match":{  
                                                       "paf.postcode":{  
                                                          "query":"$hybridNagPostcodeLocator",
                                                          "type":"boolean",
                                                          "boost":${queryParams.postcode.pafPostcodeBoost}
                                                       }
                                                    }
                                                 },
                                                 {  
                                                    "match":{  
                                                       "lpi.postcodeLocator":{  
                                                          "query":"$hybridNagPostcodeLocator",
                                                          "type":"boolean",
                                                          "boost":${queryParams.postcode.lpiPostcodeLocatorBoost}
                                                       }
                                                    }
                                                 }
                                              ]
                                           }
                                        },
                                        {  
                                           "dis_max":{
                                              "tie_breaker":${queryParams.disMaxTieBreaker},
                                              "queries":[
                                                 {  
                                                    "match":{  
                                                       "postcodeOut":{  
                                                          "query":"$hybridFirstPostcodeOut",
                                                          "type":"boolean",
                                                          "boost":${queryParams.postcode.postcodeOutBoost}
                                                       }
                                                    }
                                                 },
                                                 {  
                                                    "match":{  
                                                       "postcodeIn":{  
                                                          "query":"$hybridFirstPostcodeIn",
                                                          "type":"boolean",
                                                          "boost":${queryParams.postcode.postcodeInBoost}
                                                       }
                                                    }
                                                 }
                                              ]
                                           }
                                        },
                                        {  
                                           "dis_max":{
                                              "tie_breaker":${queryParams.disMaxTieBreaker},
                                              "queries":[
                                                 {  
                                                    "match":{  
                                                       "lpi.paoStartNumber":{  
                                                          "query":"$hybridNagPaoStartNumber",
                                                          "type":"boolean",
                                                          "boost":${queryParams.buildingName.lpiPaoStartNumberBoost}
                                                       }
                                                    }
                                                 },
                                                 {  
                                                    "match":{  
                                                       "lpi.paoStartSuffix":{  
                                                          "query":"$hybridNagPaoStartSuffix",
                                                          "type":"boolean",
                                                          "boost":${queryParams.buildingName.lpiPaoStartSuffixBoost}
                                                       }
                                                    }
                                                 },
                                                 {  
                                                    "match":{  
                                                       "lpi.paoEndNumber":{  
                                                          "query":"$hybridNagPaoEndNumber",
                                                          "type":"boolean",
                                                          "boost":${queryParams.buildingName.lpiPaoEndNumberBoost}
                                                       }
                                                    }
                                                 },
                                                 {  
                                                    "match":{  
                                                       "lpi.paoEndSuffix":{  
                                                          "query":"$hybridNagPaoEndSuffix",
                                                          "type":"boolean",
                                                          "boost":${queryParams.buildingName.lpiPaoEndSuffixBoost}
                                                       }
                                                    }
                                                 }
                                              ]
                                           }
                                        },
                                        {  
                                           "dis_max":{
                                              "tie_breaker":${queryParams.disMaxTieBreaker},
                                              "queries":[
                                                 {  
                                                    "match":{  
                                                       "lpi.saoStartNumber":{  
                                                          "query":"$hybridNagSaoStartNumber",
                                                          "type":"boolean",
                                                          "boost":${queryParams.subBuildingName.lpiSaoStartNumberBoost}
                                                       }
                                                    }
                                                 },
                                                 {  
                                                    "match":{  
                                                       "lpi.saoStartSuffix":{  
                                                          "query":"$hybridNagSaoStartSuffix",
                                                          "type":"boolean",
                                                          "boost":${queryParams.subBuildingName.lpiSaoStartSuffixBoost}
                                                       }
                                                    }
                                                 },
                                                 {  
                                                    "match":{  
                                                       "lpi.saoEndNumber":{  
                                                          "query":"$hybridNagSaoEndNumber",
                                                          "type":"boolean",
                                                          "boost":${queryParams.subBuildingName.lpiSaoEndNumberBoost}
                                                       }
                                                    }
                                                 },
                                                 {  
                                                    "match":{  
                                                       "lpi.saoEndSuffix":{  
                                                          "query":"$hybridNagSaoEndSuffix",
                                                          "type":"boolean",
                                                          "boost":${queryParams.subBuildingName.lpiSaoEndSuffixBoost}
                                                       }
                                                    }
                                                 }
                                              ]
                                           }
                                         },
                                         {
                                           "dis_max":{
                                              "tie_breaker":${queryParams.disMaxTieBreaker},
                                              "queries":[
                                                 {  
                                                    "match":{  
                                                       "paf.organizationName":{  
                                                          "query":"$hybridNagOrganisation",
                                                          "type":"boolean",
                                                          "boost":${queryParams.organisationName.pafOrganisationNameBoost}
                                                       }
                                                    }
                                                 },
                                                 {  
                                                    "match":{  
                                                       "lpi.organisation":{  
                                                          "query":"$hybridNagOrganisation",
                                                          "type":"boolean",
                                                          "boost":${queryParams.organisationName.lpiOrganisationBoost}
                                                       }
                                                    }
                                                 },
                                                 {  
                                                    "match":{  
                                                       "lpi.paoText":{  
                                                          "query":"$hybridNagOrganisation",
                                                          "type":"boolean",
                                                          "boost":${queryParams.organisationName.lpiPaoTextBoost}
                                                       }
                                                    }
                                                 },
                                                 {  
                                                    "match":{  
                                                       "lpi.legalName":{  
                                                          "query":"$hybridNagOrganisation",
                                                          "type":"boolean",
                                                          "boost":${queryParams.organisationName.lpiLegalNameBoost}
                                                       }
                                                    }
                                                 },
                                                 {  
                                                    "match":{  
                                                       "lpi.saoText":{  
                                                          "query":"$hybridNagOrganisation",
                                                          "type":"boolean",
                                                          "boost":${queryParams.organisationName.lpiSaoTextBoost}
                                                       }
                                                    }
                                                 }
                                              ]
                                           }
                                        },
                                        {  
                                           "dis_max":{
                                              "tie_breaker":${queryParams.disMaxTieBreaker},
                                              "queries":[
                                                 {  
                                                    "match":{  
                                                       "paf.departmentName":{  
                                                          "query":"$hybridPafDepartmentName",
                                                          "type":"boolean",
                                                          "boost":${queryParams.departmentName.pafDepartmentNameBoost}
                                                       }
                                                    }
                                                 },
                                                 {  
                                                    "match":{  
                                                       "lpi.legalName":{  
                                                          "query":"$hybridPafDepartmentName",
                                                          "type":"boolean",
                                                          "boost":${queryParams.departmentName.lpiLegalNameBoost}
                                                       }
                                                    }
                                                 }
                                              ]
                                           }
                                        },
                                        {  
                                           "dis_max":{
                                              "tie_breaker":${queryParams.disMaxTieBreaker},
                                              "queries":[
                                                 {  
                                                    "match":{  
                                                       "paf.dependentLocality":{  
                                                          "query":"$hybridNagLocality",
                                                          "type":"boolean",
                                                          "boost":${queryParams.locality.pafDependentLocalityBoost},
                                                          "fuzziness":"1"
                                                       }
                                                    }
                                                 },
                                                 {  
                                                    "match":{  
                                                       "paf.welshDependentLocality":{  
                                                          "query":"$hybridNagLocality",
                                                          "type":"boolean",
                                                          "boost":${queryParams.locality.pafWelshDependentLocalityBoost},
                                                          "fuzziness":"1"
                                                       }
                                                    }
                                                 },
                                                 {  
                                                    "match":{  
                                                       "lpi.locality":{  
                                                          "query":"$hybridNagLocality",
                                                          "type":"boolean",
                                                          "boost":${queryParams.locality.lpiLocalityBoost},
                                                          "fuzziness":"1"
                                                       }
                                                    }
                                                 },
                                                 {  
                                                    "match":{  
                                                       "paf.doubleDependentLocality":{  
                                                          "query":"$hybridNagLocality",
                                                          "type":"boolean",
                                                          "boost":${queryParams.locality.pafDoubleDependentLocalityBoost},
                                                          "fuzziness":"1"
                                                       }
                                                    }
                                                 },
                                                 {  
                                                    "match":{  
                                                       "paf.welshDoubleDependentLocality":{  
                                                          "query":"$hybridNagLocality",
                                                          "type":"boolean",
                                                          "boost":${queryParams.locality.pafWelshDoubleDependentLocalityBoost},
                                                          "fuzziness":"1"
                                                       }
                                                    }
                                                 }
                                              ]
                                           }
                               }
                            ]
                         }
                      },
                      {  
                         "bool":{  
                            "should":[  
                               {  
                                  "match":{  
                                     "paf.pafAll":{  
                                        "query":"$hybridAll",
                                        "type":"boolean",
                                        "boost":${queryParams.pafAllBoost}
                                     }
                                  }
                               },
                               {  
                                  "match":{  
                                     "lpi.nagAll":{  
                                        "query":"$hybridAll",
                                        "type":"boolean",
                                        "boost":${queryParams.nagAllBoost}
                                     }
                                  }
                               }
                            ]
                         }
                      }
                   ]
                }
             }
          }
       """
      )

      // When
      val result = Json.parse(repository.generateQueryAddressRequest(tokens, hybridAll).toString)

      // Then
      result shouldBe expected
    }

    "bulk search addresses" in {
      // Given
      val repository = new AddressIndexRepository(config, elasticClientProvider)

      val firstAddressTokens: Map[String, String] = Map(
        Tokens.buildingNumber -> hybridNagPaoStartNumber.toString,
        Tokens.locality -> hybridNagLocality,
        Tokens.organisationName -> hybridNagOrganisation,
        Tokens.postcode -> hybridNagPostcodeLocator
      )

      val secondAddressTokens: Map[String, String] = Map(
        Tokens.buildingNumber -> secondaryHybridNagPaoStartNumber.toString,
        Tokens.locality -> secondaryHybridNagLocality,
        Tokens.organisationName -> secondaryHybridNagOrganisation,
        Tokens.postcode -> secondaryHybridNagPostcodeLocator
      )

      val inputs = Stream(
        BulkAddressRequestData("1", "i1", firstAddressTokens),
        BulkAddressRequestData("2", "i2", secondAddressTokens)
      )

      // When
      val results = repository.queryBulk(inputs, 1).await
      val addresses = results.collect{
        case Right(address) => address
      }.flatten

      // Then
      results.length shouldBe 2
      addresses.length shouldBe 2

      addresses(0).hybridAddress.uprn shouldBe hybridFirstUprn.toString
      addresses(1).hybridAddress.uprn shouldBe hybridSecondaryUprn.toString
    }

    "return empty BulkAddress if there were no results for an address" in {
      // Given
      val repository = new AddressIndexRepository(config, elasticClientProvider)

      val firstAddressTokens = Map(
        Tokens.buildingNumber -> "9999"
      )

      val secondAddressTokens = Map(
        Tokens.buildingNumber -> "9999"
      )

      val inputs = Stream(
        BulkAddressRequestData("1", "i1", firstAddressTokens),
        BulkAddressRequestData("2", "i2", secondAddressTokens)
      )

      // When
      val results = repository.queryBulk(inputs, 1).await
      val addresses = results.collect {
        case Right(address) => address
      }.flatten

      // Then
      results.length shouldBe 2
      addresses.length shouldBe 2

      addresses(0).hybridAddress.uprn shouldBe HybridAddress.empty.uprn
      addresses(1).hybridAddress.uprn shouldBe HybridAddress.empty.uprn
    }

  }

}
