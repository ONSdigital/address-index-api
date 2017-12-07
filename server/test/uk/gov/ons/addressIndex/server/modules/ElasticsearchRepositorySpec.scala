package uk.gov.ons.addressIndex.server.modules

import uk.gov.ons.addressIndex.server.model.dao.ElasticClientProvider
import com.sksamuel.elastic4s.http.HttpClient
import com.sksamuel.elastic4s.mappings.MappingDefinition
import com.sksamuel.elastic4s.analyzers.{CustomAnalyzerDefinition, StandardTokenizer}
import com.sksamuel.elastic4s.http.search.SearchBodyBuilderFn
import com.sksamuel.elastic4s.testkit._
import org.scalatest.WordSpec
import play.api.libs.json.Json
import uk.gov.ons.addressIndex.model.db.BulkAddressRequestData
import uk.gov.ons.addressIndex.model.db.index._
import uk.gov.ons.addressIndex.parsers.Tokens

import scala.concurrent.ExecutionContext.Implicits.global

class ElasticsearchRepositorySpec extends WordSpec with SearchMatchers with ClassLocalNodeProvider with HttpElasticSugar {

 val testClient = http
  // injections
  val elasticClientProvider = new ElasticClientProvider {
    override def client: HttpClient = testClient
  }

  val config = new AddressIndexConfigModule
  val queryParams = config.config.elasticSearch.queryParams

  val hybridIndexName = config.config.elasticSearch.indexes.hybridIndex
  val hybridMappings = config.config.elasticSearch.indexes.hybridMapping

  val hybridRelLevel = 1
  val hybridRelSibArray = List(6L,7L)
  val hybridRelParArray = List(8L,9L)

  val firstHybridRelEs = Map(
    "level" -> hybridRelLevel,
    "siblings" -> hybridRelSibArray,
    "parents" -> hybridRelParArray
  )

  val secondHybridRelEs = Map(
    "level" -> hybridRelLevel,
    "siblings" -> hybridRelSibArray,
    "parents" -> hybridRelParArray
  )

  val hybridFirstUprn = 1L
  val hybridFirstParentUprn = 3L
  val hybridFirstRelative = firstHybridRelEs
  val hybridFirstPostcodeIn = "h01p"
  val hybridFirstPostcodeOut = "h02p"
  // Fields that are not in this list are not used for search
  val hybridPafUprn = 1L
  val hybridPafOrganisationName = "h2"
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
  val hybridNagOrganisation = hybridPafOrganisationName
  val hybridNagLegalName = hybridPafOrganisationName
  val hybridNagSaoText = hybridPafSubBuildingName
  val hybridNagPaoText = hybridPafBuildingName
  val hybridNagStreetDescriptor = hybridPafThoroughfare
  val hybridNagTownName = hybridPafPostTown
  val hybridNagLatitude = 1.0000000f
  val hybridNagLongitude = -2.0000000f
  val hybridNagNorthing = 3f
  val hybridNagEasting = 4f
  val hybridNagCustCode = "1110"
  val hybridNagCustName = "EXETER"
  val hybridNagCustGeogCode = "E07000041"
  // Fields with this value are not used in the search and are, thus, irrelevant
  val hybridNotUsed = ""
  val hybridNotUsedNull = null

  // Secondary PAF/NAG is used for single search (to have some "concurrence" for the main address)
  // and in the Multi Search
  val hybridSecondaryUprn = 2L
  val hybridSecondaryParentUprn = 4L
  val hybridSecondaryRelative = secondHybridRelEs
  val hybridSecondaryPostcodeIn = "s01p"
  val hybridSecondaryPostcodeOut = "s02p"

  // Fields that are not in this list are not used for search
  val secondaryHybridPafUprn = 2L
  val secondaryHybridPafOrganisationName = "s2"
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
  val secondaryHybridNagOrganisation = secondaryHybridPafOrganisationName
  val secondaryHybridNagLegalName = secondaryHybridPafOrganisationName
  val secondaryHybridNagSaoText = secondaryHybridPafSubBuildingName
  val secondaryHybridNagPaoText = secondaryHybridPafBuildingName
  val secondaryHybridNagStreetDescriptor = secondaryHybridPafThoroughfare
  val secondaryHybridNagTownName = secondaryHybridPafPostTown
  val secondaryHybridNagLatitude = 7.0000000f
  val secondaryHybridNagLongitude = 8.0000000f
  val secondaryHybridNagNorthing = 10f
  val secondaryHybridNagEasting = 11f
  val secondardyHybridNagLocalCustodianName = "EXETER"
  val secondardyHybridNagLocalCustodianCode = "1110"

  val firstHybridPafEs = Map(
    "recordIdentifier" -> hybridNotUsedNull,
    "changeType" -> hybridNotUsed,
    "proOrder" -> hybridNotUsedNull,
    "uprn" -> hybridPafUprn,
    "udprn" -> hybridNotUsedNull,
    "organisationName" -> hybridPafOrganisationName,
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
    "organisationName" -> secondaryHybridPafOrganisationName,
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
    "location" -> List(hybridNagLongitude, hybridNagLatitude),
    "language" -> hybridNotUsed,
    "classScheme" -> hybridNotUsed,
    "localCustodianCode" ->  secondardyHybridNagLocalCustodianCode,
    "localCustodianName" ->  secondardyHybridNagLocalCustodianName,
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
    "location" -> List(secondaryHybridNagLongitude, secondaryHybridNagLatitude),
    "nagAll" -> hybridNotUsed,
    "language" -> hybridNotUsed,
    "classScheme" -> hybridNotUsed,
    "localCustodianCode" -> secondardyHybridNagLocalCustodianCode,
    "localCustodianName" -> secondardyHybridNagLocalCustodianName,
    "localCustodianGeogCode" -> hybridNotUsedNull,
    "rpc" -> hybridNotUsedNull,
    "nagAll" -> secondaryHybridAll
  )

  val firstHybridEs: Map[String, Any] = Map(
    "uprn" -> hybridFirstUprn,
    "parentUprn" -> hybridFirstParentUprn,
    "relatives" -> Seq(hybridFirstRelative),
    "postcodeIn" -> hybridFirstPostcodeIn,
    "postcodeOut" -> hybridFirstPostcodeOut,
    "paf" -> Seq(firstHybridPafEs),
    "lpi" -> Seq(firstHybridNagEs)
  )

  // This one is used to create a "concurrent" for the first one (the first one should be always on top)
  val secondHybridEs: Map[String, Any] = Map(
    "uprn" -> hybridSecondaryUprn,
    "parentUprn" -> hybridSecondaryParentUprn,
    "relatives" -> Seq(hybridSecondaryRelative),
    "postcodeIn" -> hybridSecondaryPostcodeIn,
    "postcodeOut" -> hybridSecondaryPostcodeOut,
    "paf" -> Seq(secondHybridPafEs),
    "lpi" -> Seq(secondHybridNagEs)
  )

 testClient.execute{
    createIndex(hybridIndexName)
      .mappings(MappingDefinition.apply(hybridMappings))
        .analysis(Some(CustomAnalyzerDefinition("welsh_split_synonyms_analyzer",
          StandardTokenizer("myTokenizer1"))
      ))
  }.await

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
    hybridPafOrganisationName,
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
    hybridNagCustCode,
    hybridNagCustName,
    hybridNagCustGeogCode,
    hybridNotUsed,
    hybridAll,
    hybridNotUsed
  )

  val expectedRelative = Relative (
    level = hybridRelLevel,
    siblings = hybridRelSibArray,
    parents = hybridRelParArray
  )

  val expectedHybrid = HybridAddress(
    uprn = hybridFirstUprn.toString,
    parentUprn = hybridFirstParentUprn.toString,
    relatives = Seq(expectedRelative),
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
          "version":true,
          "query" : {
            "term" : {
            "uprn" : {"value":"1"}
            }
          }
        }
        """
      )

      // When
      val result = Json.parse(SearchBodyBuilderFn(repository.generateQueryUprnRequest(hybridFirstUprn.toString)).string())

      // Then
      result shouldBe expected
    }

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

      val expected = expectedHybrid

      // When
      val HybridAddresses(results, maxScore, total) = repository.queryAddresses(tokens, 0, 10).await

      // Then
      results.length should be > 0 // it MAY return more than 1 addresses, but the top one should remain the same
      total should be > 0l

      val resultHybrid = results.head
      resultHybrid shouldBe expected.copy(score = resultHybrid.score)

      // Score is random, but should always be positive
      resultHybrid.score should be > 0f
      maxScore should be > 0d
    }
    "have score of `0` if no addresses found" in {
      // Given
      val repository = new AddressIndexRepository(config, elasticClientProvider)
      val tokens = Map(
        Tokens.buildingNumber -> "9999"
      )

      // When
      val HybridAddresses(results, maxScore, total) = repository.queryAddresses(tokens, 0, 10).await

      // Then
      results.length shouldBe 0
      maxScore shouldBe 0f
      total shouldBe 0f
    }

    "remove empty boolean queries from the query " in {
      // Given
      val repository = new AddressIndexRepository(config, elasticClientProvider)

      val tokens: Map[String, String] = Map.empty

      val expected = Json.parse(
        s"""
          {
            "version":true,
            "query":{
              "bool":{
                "must":[{
                  "dis_max":{
                  "tie_breaker":0,
                  "queries":[{
                    "match":{
                      "lpi.nagAll":{
                        "query":"",
                        "analyzer":"welsh_split_synonyms_analyzer",
                        "boost":${queryParams.fallback.fallbackLpiBoost},
                        "minimum_should_match":"${queryParams.fallback.fallbackMinimumShouldMatch}"
                      }
                    }
                  },{
                    "match":{
                      "paf.pafAll":{
                        "query":"",
                        "analyzer":"welsh_split_synonyms_analyzer",
                        "boost":${queryParams.fallback.fallbackPafBoost},
                        "minimum_should_match":"${queryParams.fallback.fallbackMinimumShouldMatch}"
                      }
                    }
                  }]
                }
              }],
              "should":[{
                "dis_max":{
                  "tie_breaker":0,
                  "queries":[{
                    "match":{
                      "lpi.nagAll.bigram":{
                        "query":"",
                          "boost":${queryParams.fallback.fallbackLpiBigramBoost},
                          "fuzziness":"${queryParams.fallback.bigramFuzziness}"
                        }
                      }
                    },{
                      "match":{
                        "paf.pafAll.bigram":{
                          "query":"",
                           "boost":${queryParams.fallback.fallbackPafBigramBoost},
                           "fuzziness":"${queryParams.fallback.bigramFuzziness}"
                          }
                        }
                      }]
                    }
                  }],
                  "boost":0.075
                }
              },
              "sort":[{
                "_score":{
                  "order":"desc"
                }
              },{
                "uprn":{"order":"asc"}
            }],
            "track_scores":true
          }
        """.stripMargin)

      // When
      val result = Json.parse(SearchBodyBuilderFn(repository.generateQueryAddressRequest(tokens)).string())

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
           	"version": true,
           	"query": {
           		"dis_max": {
           			"tie_breaker": 1,
           			"queries": [{
           				"bool": {
           					"should": [{
           						"dis_max": {
           							"tie_breaker": 0,
           							"queries": [{
           								"constant_score": {
           									"filter": {
           										"match": {
           											"paf.buildingName": {
           												"query": "h5",
           												"fuzziness": "1"
           											}
           										}
           									},
           									"boost": ${queryParams.buildingName.pafBuildingNameBoost}
           								}
           							}, {
           								"constant_score": {
           									"filter": {
           										"match": {
           											"lpi.paoText": {
           												"query": "h5",
           												"fuzziness": "1",
           												"minimum_should_match": "-45%"
           											}
           										}
           									},
           									"boost": ${queryParams.buildingName.lpiPaoTextBoost}
           								}
           							}, {
           								"constant_score": {
           									"filter": {
           										"bool": {
           											"must": [{
           												"match": {
           													"lpi.paoStartNumber": {
           														"query": "13"
           													}
           												}
           											}, {
           												"match": {
           													"lpi.paoStartSuffix": {
           														"query": "h11"
           													}
           												}
           											}]
           										}
           									},
           									"boost": ${queryParams.buildingName.lpiPaoStartSuffixBoost}
           								}
           							}]
           						}
           					}, {
           						"dis_max": {
           							"tie_breaker": 0,
           							"queries": [{
           								"constant_score": {
           									"filter": {
           										"match": {
           											"paf.subBuildingName": {
           												"query": "h4"
           											}
           										}
           									},
           									"boost": ${queryParams.subBuildingName.pafSubBuildingNameBoost}
           								}
           							}, {
           								"constant_score": {
           									"filter": {
           										"match": {
           											"lpi.saoText": {
           												"query": "h4",
           												"minimum_should_match": "-45%"
           											}
           										}
           									},
           									"boost": ${queryParams.subBuildingName.lpiSaoTextBoost}
           								}
           							}, {
           								"dis_max": {
           									"tie_breaker": 0.5,
           									"queries": [{
           										"constant_score": {
           											"filter": {
           												"match": {
           													"lpi.saoStartNumber": {
           														"query": "15"
           													}
           												}
           											},
           											"boost": ${queryParams.subBuildingRange.lpiSaoStartNumberBoost}
           										}
           									}, {
           										"constant_score": {
           											"filter": {
           												"match": {
           													"lpi.saoStartSuffix": {
           														"query": "h16"
           													}
           												}
           											},
           											"boost": ${queryParams.subBuildingRange.lpiSaoStartSuffixBoost}
           										}
           									}]
           								}
           							}]
           						}
           					}, {
           						"dis_max": {
           							"tie_breaker": 0,
           							"queries": [{
           								"constant_score": {
           									"filter": {
           										"match": {
           											"paf.thoroughfare": {
           												"query": "h7",
           												"fuzziness": "1"
           											}
           										}
           									},
           									"boost": ${queryParams.streetName.pafThoroughfareBoost}
           								}
           							}, {
           								"constant_score": {
           									"filter": {
           										"match": {
           											"paf.welshThoroughfare": {
           												"query": "h7",
           												"fuzziness": "1"
           											}
           										}
           									},
           									"boost": ${queryParams.streetName.pafWelshThoroughfareBoost}
           								}
           							}, {
           								"constant_score": {
           									"filter": {
           										"match": {
           											"paf.dependentThoroughfare": {
           												"query": "h7",
           												"fuzziness": "1"
           											}
           										}
           									},
           									"boost": ${queryParams.streetName.pafDependentThoroughfareBoost}
           								}
           							}, {
           								"constant_score": {
           									"filter": {
           										"match": {
           											"paf.welshDependentThoroughfare": {
           												"query": "h7",
           												"fuzziness": "1"
           											}
           										}
           									},
           									"boost": ${queryParams.streetName.pafWelshDependentThoroughfareBoost}
           								}
           							}, {
           								"constant_score": {
           									"filter": {
           										"match": {
           											"lpi.streetDescriptor": {
           												"query": "h7",
           												"fuzziness": "1"
           											}
           										}
           									},
           									"boost": ${queryParams.streetName.lpiStreetDescriptorBoost}
           								}
           							}]
           						}
           					}, {
           						"dis_max": {
           							"tie_breaker": 0,
           							"queries": [{
           								"constant_score": {
           									"filter": {
           										"match": {
           											"paf.postcode": {
           												"query": "h10"
           											}
           										}
           									},
           									"boost": ${queryParams.postcode.pafPostcodeBoost}
           								}
           							}, {
           								"constant_score": {
           									"filter": {
           										"match": {
           											"lpi.postcodeLocator": {
           												"query": "h10"
           											}
           										}
           									},
           									"boost": ${queryParams.postcode.lpiPostcodeLocatorBoost}
           								}
           							}, {
           								"constant_score": {
           									"filter": {
           										"bool": {
           											"must": [{
           												"match": {
           													"postcodeOut": {
           														"query": "h02p",
           														"fuzziness": "1"
           													}
           												}
           											}, {
           												"match": {
           													"postcodeIn": {
           														"query": "h01p",
           														"fuzziness": "2"
           													}
           												}
           											}]
           										}
           									},
           									"boost": ${queryParams.postcode.postcodeInOutBoost}
           								}
           							}]
           						}
           					}, {
           						"dis_max": {
           							"tie_breaker": 0,
           							"queries": [{
           								"constant_score": {
           									"filter": {
           										"match": {
           											"paf.organisationName": {
           												"query": "h2",
           												"minimum_should_match": "30%"
           											}
           										}
           									},
           									"boost": ${queryParams.organisationName.pafOrganisationNameBoost}
           								}
           							}, {
           								"constant_score": {
           									"filter": {
           										"match": {
           											"lpi.organisation": {
           												"query": "h2",
           												"minimum_should_match": "30%"
           											}
           										}
           									},
           									"boost": ${queryParams.organisationName.lpiOrganisationBoost}
           								}
           							}, {
           								"constant_score": {
           									"filter": {
           										"match": {
           											"lpi.paoText": {
           												"query": "h2",
           												"minimum_should_match": "30%"
           											}
           										}
           									},
           									"boost": ${queryParams.organisationName.lpiPaoTextBoost}
           								}
           							}, {
           								"constant_score": {
           									"filter": {
           										"match": {
           											"lpi.legalName": {
           												"query": "h2",
           												"minimum_should_match": "30%"
           											}
           										}
           									},
           									"boost": ${queryParams.organisationName.lpiLegalNameBoost}
           								}
           							}, {
           								"constant_score": {
           									"filter": {
           										"match": {
           											"lpi.saoText": {
           												"query": "h2",
           												"minimum_should_match": "30%"
           											}
           										}
           									},
           									"boost": ${queryParams.organisationName.lpiSaoTextBoost}
           								}
           							}]
           						}
           					}, {
           						"dis_max": {
           							"tie_breaker": 0,
           							"queries": [{
           								"constant_score": {
           									"filter": {
           										"match": {
           											"paf.departmentName": {
           												"query": "h3",
           												"minimum_should_match": "30%"
           											}
           										}
           									},
           									"boost": ${queryParams.departmentName.pafDepartmentNameBoost}
           								}
           							}, {
           								"constant_score": {
           									"filter": {
           										"match": {
           											"lpi.legalName": {
           												"query": "h3",
           												"minimum_should_match": "30%"
           											}
           										}
           									},
           									"boost": ${queryParams.departmentName.lpiLegalNameBoost}
           								}
           							}]
           						}
           					}, {
           						"dis_max": {
           							"tie_breaker": 0.5,
           							"queries": [{
           								"dis_max": {
           									"tie_breaker": 0,
           									"queries": [{
           										"constant_score": {
           											"filter": {
           												"match": {
           													"paf.postTown": {
           														"query": "h8",
           														"fuzziness": "1"
           													}
           												}
           											},
           											"boost": ${queryParams.townName.pafPostTownBoost}
           										}
           									}, {
           										"constant_score": {
           											"filter": {
           												"match": {
           													"paf.welshPostTown": {
           														"query": "h8",
           														"fuzziness": "1"
           													}
           												}
           											},
           											"boost": ${queryParams.townName.pafWelshPostTownBoost}
           										}
           									}, {
           										"constant_score": {
           											"filter": {
           												"match": {
           													"lpi.townName": {
           														"query": "h8",
           														"fuzziness": "1"
           													}
           												}
           											},
           											"boost": ${queryParams.townName.lpiTownNameBoost}
           										}
           									}, {
           										"constant_score": {
           											"filter": {
           												"match": {
           													"paf.dependentLocality": {
           														"query": "h8",
           														"fuzziness": "1"
           													}
           												}
           											},
           											"boost": ${queryParams.townName.pafDependentLocalityBoost}
           										}
           									}, {
           										"constant_score": {
           											"filter": {
           												"match": {
           													"paf.welshDependentLocality": {
           														"query": "h8",
           														"fuzziness": "1"
           													}
           												}
           											},
           											"boost": ${queryParams.townName.pafWelshDependentLocalityBoost}
           										}
           									}, {
           										"constant_score": {
           											"filter": {
           												"match": {
           													"lpi.locality": {
           														"query": "h8",
           														"fuzziness": "1"
           													}
           												}
           											},
           											"boost": 0.5
           										}
           									}, {
           										"constant_score": {
           											"filter": {
           												"match": {
           													"paf.doubleDependentLocality": {
           														"query": "h8",
           														"fuzziness": "1"
           													}
           												}
           											},
           											"boost": ${queryParams.townName.pafDoubleDependentLocalityBoost}
           										}
           									}, {
           										"constant_score": {
           											"filter": {
           												"match": {
           													"paf.welshDoubleDependentLocality": {
           														"query": "h8",
           														"fuzziness": "1"
           													}
           												}
           											},
           											"boost": ${queryParams.townName.pafWelshDoubleDependentLocalityBoost}
           										}
           									}]
           								}
           							}, {
           								"dis_max": {
           									"tie_breaker": 0,
           									"queries": [{
           										"constant_score": {
           											"filter": {
           												"match": {
           													"paf.postTown": {
           														"query": "h20",
           														"fuzziness": "1"
           													}
           												}
           											},
           											"boost": ${queryParams.locality.pafPostTownBoost}
           										}
           									}, {
           										"constant_score": {
           											"filter": {
           												"match": {
           													"paf.welshPostTown": {
           														"query": "h20",
           														"fuzziness": "1"
           													}
           												}
           											},
           											"boost": ${queryParams.locality.pafWelshPostTownBoost}
           										}
           									}, {
           										"constant_score": {
           											"filter": {
           												"match": {
           													"lpi.townName": {
           														"query": "h20",
           														"fuzziness": "1"
           													}
           												}
           											},
           											"boost": ${queryParams.locality.lpiTownNameBoost}
           										}
           									}, {
           										"constant_score": {
           											"filter": {
           												"match": {
           													"paf.dependentLocality": {
           														"query": "h20",
           														"fuzziness": "1"
           													}
           												}
           											},
           											"boost": ${queryParams.locality.pafDependentLocalityBoost}
           										}
           									}, {
           										"constant_score": {
           											"filter": {
           												"match": {
           													"paf.welshDependentLocality": {
           														"query": "h20",
           														"fuzziness": "1"
           													}
           												}
           											},
           											"boost": ${queryParams.locality.pafWelshDependentLocalityBoost}
           										}
           									}, {
           										"constant_score": {
           											"filter": {
           												"match": {
           													"lpi.locality": {
           														"query": "h20",
           														"fuzziness": "1"
           													}
           												}
           											},
           											"boost": ${queryParams.locality.lpiLocalityBoost}
           										}
           									}, {
           										"constant_score": {
           											"filter": {
           												"match": {
           													"paf.doubleDependentLocality": {
           														"query": "h20",
           														"fuzziness": "1"
           													}
           												}
           											},
           											"boost": ${queryParams.locality.pafDoubleDependentLocalityBoost}
           										}
           									}, {
           										"constant_score": {
           											"filter": {
           												"match": {
           													"paf.welshDoubleDependentLocality": {
           														"query": "h20",
           														"fuzziness": "1"
           													}
           												}
           											},
           											"boost": ${queryParams.locality.pafWelshDoubleDependentLocalityBoost}
           										}
           									}]
           								}
           							}]
           						}
           					}, {
           						"dis_max": {
           							"tie_breaker": 0.5,
           							"queries": [{
           								"constant_score": {
           									"filter": {
           										"match": {
           											"lpi.paoStartNumber": {
           												"query": "13"
           											}
           										}
           									},
           									"boost": ${queryParams.buildingRange.lpiPaoStartNumberBoost}
           								}
           							}, {
           								"constant_score": {
           									"filter": {
           										"match": {
           											"lpi.paoStartSuffix": {
           												"query": "h11"
           											}
           										}
           									},
           									"boost": ${queryParams.buildingRange.lpiPaoStartSuffixBoost}
           								}
           							}, {
           								"constant_score": {
           									"filter": {
           										"match": {
           											"lpi.paoEndNumber": {
           												"query": "12"
           											}
           										}
           									},
           									"boost": ${queryParams.buildingRange.lpiPaoEndNumberBoost}
           								}
           							}, {
           								"constant_score": {
           									"filter": {
           										"match": {
           											"lpi.paoEndSuffix": {
           												"query": "h14"
           											}
           										}
           									},
           									"boost": ${queryParams.buildingRange.lpiPaoEndSuffixBoost}
           								}
           							}, {
           								"constant_score": {
           									"filter": {
           										"match": {
           											"lpi.paoStartNumber": {
           												"query": "12"
           											}
           										}
           									},
           									"boost": ${queryParams.buildingRange.lpiPaoStartEndBoost}
           								}
           							}, {
           								"constant_score": {
           									"filter": {
           										"match": {
           											"paf.buildingNumber": {
           												"query": "12"
           											}
           										}
           									},
           									"boost": ${queryParams.buildingRange.pafBuildingNumberBoost}
           								}
           							}, {
           								"constant_score": {
           									"filter": {
           										"match": {
           											"paf.buildingNumber": {
           												"query": "13"
           											}
           										}
           									},
           									"boost": ${queryParams.buildingRange.pafBuildingNumberBoost}
           								}
           							}]
           						}
           					}, {
           						"dis_max": {
           							"tie_breaker": 0.5,
           							"queries": [{
           								"constant_score": {
           									"filter": {
           										"match": {
           											"lpi.saoStartNumber": {
           												"query": "15"
           											}
           										}
           									},
           									"boost": ${queryParams.subBuildingRange.lpiSaoStartNumberBoost}
           								}
           							}, {
           								"constant_score": {
           									"filter": {
           										"match": {
           											"lpi.saoStartSuffix": {
           												"query": "h16"
           											}
           										}
           									},
           									"boost": ${queryParams.subBuildingRange.lpiSaoStartSuffixBoost}
           								}
           							}, {
           								"constant_score": {
           									"filter": {
           										"match": {
           											"lpi.saoEndNumber": {
           												"query": "17"
           											}
           										}
           									},
           									"boost": ${queryParams.subBuildingRange.lpiSaoEndNumberBoost}
           								}
           							}, {
           								"constant_score": {
           									"filter": {
           										"match": {
           											"lpi.saoEndSuffix": {
           												"query": "h18"
           											}
           										}
           									},
           									"boost": ${queryParams.subBuildingRange.lpiSaoEndSuffixBoost}
           								}
           							}, {
           								"constant_score": {
           									"filter": {
           										"match": {
           											"lpi.saoStartNumber": {
           												"query": "17"
           											}
           										}
           									},
           									"boost": ${queryParams.subBuildingRange.lpiSaoStartEndBoost}
           								}
           							}]
           						}
           					}],
           					"minimum_should_match": "-40%"
           				}
           			}, {
           				"bool": {
           					"must": [{
           						"dis_max": {
           							"tie_breaker": 0,
           							"queries": [{
           								"match": {
           									"lpi.nagAll": {
           										"query": "h2 h3 h4 h5 6 h7 h20 h8 h10",
           										"analyzer": "welsh_split_synonyms_analyzer",
           										"boost": ${queryParams.fallback.fallbackLpiBoost},
           										"minimum_should_match": "-40%"
           									}
           								}
           							}, {
           								"match": {
           									"paf.pafAll": {
           										"query": "h2 h3 h4 h5 6 h7 h20 h8 h10",
           										"analyzer": "welsh_split_synonyms_analyzer",
           										"boost": ${queryParams.fallback.fallbackPafBoost},
           										"minimum_should_match": "-40%"
           									}
           								}
           							}]
           						}
           					}],
           					"should": [{
           						"dis_max": {
           							"tie_breaker": 0,
           							"queries": [{
           								"match": {
           									"lpi.nagAll.bigram": {
           										"query": "h2 h3 h4 h5 6 h7 h20 h8 h10",
           										"boost": ${queryParams.fallback.fallbackLpiBigramBoost},
           										"fuzziness": "0"
           									}
           								}
           							}, {
           								"match": {
           									"paf.pafAll.bigram": {
           										"query": "h2 h3 h4 h5 6 h7 h20 h8 h10",
           										"boost": ${queryParams.fallback.fallbackPafBigramBoost},
           										"fuzziness": "0"
           									}
           								}
           							}]
           						}
           					}],
           					"boost": ${queryParams.fallback.fallbackQueryBoost}
           				}
           			}]
           		}
           	},
           	"sort": [{
           		"_score": {
           			"order": "desc"
           		}
           	}, {
           		"uprn": {
           			"order": "asc"
           		}
           	}],
           	"track_scores": true
           }
      """.stripMargin)

      // When
      val result = Json.parse(SearchBodyBuilderFn(repository.generateQueryAddressRequest(tokens)).string())

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
