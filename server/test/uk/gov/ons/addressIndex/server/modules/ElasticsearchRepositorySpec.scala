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

  val hybridIndex = config.config.elasticSearch.indexes.hybridIndex
  val Array(hybridIndexName, hybridMappings) = hybridIndex.split("/")

  val hybridFirstUprn = "uprn1"

  // Fields that are not in this list are not used for search
  val hybridPafUprn = "h1"
  val hybridPafOrganizationName = "h2"
  val hybridPafDepartmentName = "h3"
  val hybridPafSubBuildingName = "h4"
  val hybridPafBuildingName = "h5"
  val hybridPafBuildingNumber = "h6"
  val hybridPafThoroughfare = "h7"
  val hybridPafPostTown = "h8"
  val hybridPafPostcode = "h9"

  // Fields that are not in this list are not used for search
  val hybridNagUprn = hybridPafUprn
  val hybridNagPostcodeLocator = hybridPafPostcode
  val hybridNagPaoStartNumber = "h13"
  val hybridNagPaoStartSuffix = "h11"
  val hybridNagPaoEndNumber = "h12"
  val hybridNagLocality = "h10"
  val hybridNagOrganisation = hybridPafOrganizationName
  val hybridNagLegalName = hybridPafOrganizationName
  val hybridNagSaoText = hybridPafSubBuildingName
  val hybridNagPaoText = hybridPafBuildingName
  val hybridNagStreetDescriptor = hybridPafThoroughfare
  val hybridNagTownName = hybridPafPostTown
  val hybridNagLatitude = "1.0000000"
  val hybridNagLongitude = "2.0000000"
  val hybridNagNorthing = "3"
  val hybridNagEasting = "4"

  // Fields with this value are not used in the search and are, thus, irrelevant
  val hybridNotUsed = ""

  // Secondary PAF/NAG is used for single search (to have some "concurrence" for the main address)
  // and in the Multi Search
  val hybridSecondaryUprn = "uprn2"

  // Fields that are not in this list are not used for search
  val secondaryHybridPafUprn = "s1"
  val secondaryHybridPafOrganizationName = "s2"
  val secondaryHybridPafDepartmentName = "s3"
  val secondaryHybridPafSubBuildingName = "s4"
  val secondaryHybridPafBuildingName = "s5"
  val secondaryHybridPafBuildingNumber = "s6"
  val secondaryHybridPafThoroughfare = "s7"
  val secondaryHybridPafPostTown = "s8"
  val secondaryHybridPafPostcode = "s9"

  // Fields that are not in this list are not used for search
  val secondaryHybridNagUprn = secondaryHybridPafUprn
  val secondaryHybridNagPostcodeLocator = secondaryHybridPafPostcode
  val secondaryHybridNagPaoStartNumber = "s13"
  val secondaryHybridNagPaoStartSuffix = "s11"
  val secondaryHybridNagPaoEndNumber = "s12"
  val secondaryHybridNagLocality = "s10"
  val secondaryHybridNagOrganisation = secondaryHybridPafOrganizationName
  val secondaryHybridNagLegalName = secondaryHybridPafOrganizationName
  val secondaryHybridNagSaoText = secondaryHybridPafSubBuildingName
  val secondaryHybridNagPaoText = secondaryHybridPafBuildingName
  val secondaryHybridNagStreetDescriptor = secondaryHybridPafThoroughfare
  val secondaryHybridNagTownName = secondaryHybridPafPostTown
  val secondaryHybridNagLatitude = "3.0000000"
  val secondaryHybridNagLongitude = "4.0000000"
  val secondaryHybridNagNorthing = "5"
  val secondaryHybridNagEasting = "6"

  val firstHybridPafEs = Map(
    "recordIdentifier" -> hybridNotUsed,
    "changeType" -> hybridNotUsed,
    "proOrder" -> hybridNotUsed,
    "uprn" -> hybridPafUprn,
    "udprn" -> hybridNotUsed,
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
    "entryDate" ->hybridNotUsed
  )

  val secondHybridPafEs = Map(
    "recordIdentifier" -> hybridNotUsed,
    "changeType" -> hybridNotUsed,
    "proOrder" -> hybridNotUsed,
    "uprn" -> hybridSecondaryUprn,
    "udprn" -> hybridNotUsed,
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
    "entryDate" ->hybridNotUsed
  )

  val firstHybridNagEs = Map(
    "uprn" -> hybridNagUprn,
    "postcodeLocator" -> hybridNagPostcodeLocator,
    "addressBasePostal" -> hybridNotUsed,
    "usrn" -> hybridNotUsed,
    "lpiKey" -> hybridNotUsed,
    "paoText" -> hybridNagPaoText,
    "paoStartNumber" -> hybridNagPaoStartNumber,
    "paoStartSuffix" -> hybridNotUsed,
    "paoEndNumber" -> hybridNotUsed,
    "paoEndSuffix" -> hybridNotUsed,
    "saoText" -> hybridNagSaoText,
    "saoStartNumber" -> hybridNotUsed,
    "saoStartSuffix" -> hybridNotUsed,
    "saoEndNumber" -> hybridNotUsed,
    "saoEndSuffix" -> hybridNotUsed,
    "level" -> hybridNotUsed,
    "officialFlag" -> hybridNotUsed,
    "logicalStatus" -> hybridNotUsed,
    "streetDescriptor" -> hybridNagStreetDescriptor,
    "townName" -> hybridNagTownName,
    "locality" -> hybridNagLocality,
    "organisation" -> hybridNagOrganisation,
    "legalName" -> hybridNagLegalName,
    "latitude" -> hybridNagLatitude,
    "longitude" -> hybridNagLongitude,
    "northing" -> hybridNagNorthing,
    "easting" -> hybridNagEasting,
    "classificationCode" -> hybridNotUsed
  )

  val secondHybridNagEs = Map(
    "uprn" -> hybridSecondaryUprn,
    "postcodeLocator" -> secondaryHybridNagPostcodeLocator,
    "addressBasePostal" -> hybridNotUsed,
    "usrn" -> hybridNotUsed,
    "lpiKey" -> hybridNotUsed,
    "paoText" -> secondaryHybridNagPaoText,
    "paoStartNumber" -> secondaryHybridNagPaoStartNumber,
    "paoStartSuffix" -> hybridNotUsed,
    "paoEndNumber" -> hybridNotUsed,
    "paoEndSuffix" -> hybridNotUsed,
    "saoText" -> secondaryHybridNagSaoText,
    "saoStartNumber" -> hybridNotUsed,
    "saoStartSuffix" -> hybridNotUsed,
    "saoEndNumber" -> hybridNotUsed,
    "saoEndSuffix" -> hybridNotUsed,
    "level" -> hybridNotUsed,
    "officialFlag" -> hybridNotUsed,
    "logicalStatus" -> hybridNotUsed,
    "streetDescriptor" -> secondaryHybridNagStreetDescriptor,
    "townName" -> secondaryHybridNagTownName,
    "locality" -> secondaryHybridNagLocality,
    "organisation" -> secondaryHybridNagOrganisation,
    "legalName" -> secondaryHybridNagLegalName,
    "latitude" -> secondaryHybridNagLatitude,
    "longitude" -> secondaryHybridNagLongitude,
    "northing" -> secondaryHybridNagNorthing,
    "easting" -> secondaryHybridNagEasting,
    "classificationCode" -> hybridNotUsed
  )

  val firstHybridEs = Map(
    "uprn" -> hybridFirstUprn,
    "paf" -> Seq(firstHybridPafEs),
    "lpi" -> Seq(firstHybridNagEs)
  )

  // This one is used to create a "concurrent" for the first one (the first one should be always on top)
  val secondHybridEs = Map(
    "uprn" -> "uprn2",
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
    hybridPafUprn,
    hybridNotUsed,
    hybridPafOrganizationName,
    hybridPafDepartmentName,
    hybridPafSubBuildingName,
    hybridPafBuildingName,
    hybridPafBuildingNumber,
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
    hybridNotUsed
  )

  val expectedNag = NationalAddressGazetteerAddress(
    hybridNagUprn,
    hybridNagPostcodeLocator,
    hybridNotUsed,
    hybridNagLatitude,
    hybridNagLongitude,
    hybridNagEasting,
    hybridNagNorthing,
    hybridNagOrganisation,
    hybridNagLegalName,
    hybridNotUsed,
    hybridNotUsed,
    hybridNotUsed,
    hybridNagPaoText,
    hybridNagPaoStartNumber,
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
    hybridNotUsed,
    hybridNagStreetDescriptor,
    hybridNagTownName,
    hybridNagLocality
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
            "uprn" : "uprn1"
          }
          }
        }
        """
      )

      // When
      val result = Json.parse(repository.generateQueryUprnRequest(hybridFirstUprn).toString)

      // Then
      result shouldBe expected
    }

    "find HYBRID address by UPRN" in {
      // Given
      val repository = new AddressIndexRepository(config, elasticClientProvider)
      val expectedHybrid = HybridAddress(
        uprn = hybridFirstUprn,
        lpi = Seq(expectedNag),
        paf = Seq(expectedPaf),
        score = 1.0f
      )
      val expected = Some(expectedHybrid)

      // When
      val result = repository.queryUprn(hybridFirstUprn).await

      // Then
      result shouldBe expected
    }

    "find Hybrid addresses by building number, postcode, locality and organisation name" in {
      // Given
      val repository = new AddressIndexRepository(config, elasticClientProvider)
      val tokens = Map(
        Tokens.buildingNumber -> hybridNagPaoStartNumber,
        Tokens.locality -> hybridNagLocality,
        Tokens.organisationName -> hybridNagOrganisation,
        Tokens.postcode -> hybridNagPostcodeLocator
      )
      val expectedScore = 0.4f

      val expected = HybridAddress(
        uprn = hybridFirstUprn,
        lpi = Seq(expectedNag),
        paf = Seq(expectedPaf),
        score = 0f
      )

      // When
      val HybridAddresses(results, maxScore, total) = repository.queryAddresses(0, 10, tokens).await

      // Then
      results.length shouldBe 1
      total shouldBe 1

      val resultHybrid = results.head
      resultHybrid shouldBe expected.copy(score = resultHybrid.score)

      // Score is random, but should always be close to some number
      resultHybrid.score shouldBe expectedScore +- 0.1f
      maxScore shouldBe expectedScore +- 0.1f
    }

    "have score of `0` if no addresses found" in {
      // Given
      val repository = new AddressIndexRepository(config, elasticClientProvider)
      val tokens = Map(
        Tokens.buildingNumber -> "SomeStringThatWontHaveAnyResult"
      )

      // When
      val HybridAddresses(results, maxScore, total) = repository.queryAddresses(0, 10, tokens).await

      // Then
      results.length shouldBe 0
      maxScore shouldBe 0f
      total shouldBe 0f
    }

    "generate valid query for search by tokens" in {
      // Given
      val repository = new AddressIndexRepository(config, elasticClientProvider)
      val tokens = Map(
        Tokens.buildingNumber -> hybridPafBuildingNumber,
        Tokens.paoStartNumber -> hybridNagPaoStartNumber,
        Tokens.paoStartSuffix -> hybridNagPaoStartSuffix,
        Tokens.paoEndNumber -> hybridNagPaoEndNumber,
        Tokens.locality -> hybridNagLocality,
        Tokens.organisationName -> hybridNagOrganisation,
        Tokens.postcode -> hybridNagPostcodeLocator,
        Tokens.departmentName -> hybridPafDepartmentName,
        Tokens.subBuildingName -> hybridPafSubBuildingName,
        Tokens.buildingName -> hybridPafBuildingName,
        Tokens.streetName -> hybridNagStreetDescriptor,
        Tokens.townName -> hybridNagTownName
      )

      val expected = Json.parse(
        s"""
          {
            "query" : {
              "bool" : {
                "should" : [ {
                  "match" : {
                    "lpi.paoStartNumber" : {
                      "query" : "$hybridPafBuildingNumber",
                      "type" : "boolean",
                      "boost" : 5.0
                    }
                  }
                }, {
                  "match" : {
                    "lpi.paoStartNumber" : {
                      "query" : "$hybridNagPaoStartNumber",
                      "type" : "boolean",
                      "boost" : 1.0
                    }
                  }
                }, {
                  "match" : {
                    "lpi.paoStartSuffix" : {
                      "query" : "$hybridNagPaoStartSuffix",
                      "type" : "boolean",
                      "boost" : 1.0
                    }
                  }
                }, {
                  "match" : {
                    "lpi.paoEndNumber" : {
                      "query" : "$hybridNagPaoEndNumber",
                      "type" : "boolean",
                      "boost" : 1.0
                    }
                  }
                }, {
                  "match" : {
                    "lpi.locality" : {
                      "query" : "$hybridNagLocality",
                      "type" : "boolean"
                    }
                  }
                }, {
                  "match" : {
                    "lpi.organisation" : {
                      "query" : "$hybridPafOrganizationName",
                      "type" : "boolean",
                      "boost" : 1.0
                    }
                  }
                }, {
                  "match" : {
                    "lpi.legalName" : {
                      "query" : "$hybridPafOrganizationName",
                      "type" : "boolean",
                      "boost" : 1.0
                    }
                  }
                }, {
                  "match" : {
                    "lpi.paoText" : {
                      "query" : "$hybridPafOrganizationName",
                      "type" : "boolean",
                      "boost" : 1.0
                    }
                  }
                }, {
                  "match" : {
                    "lpi.saoText" : {
                      "query" : "$hybridPafOrganizationName",
                      "type" : "boolean",
                      "boost" : 0.5
                    }
                  }
                }, {
                  "match" : {
                    "lpi.saoText" : {
                      "query" : "$hybridPafSubBuildingName",
                      "type" : "boolean",
                      "boost" : 1.0
                    }
                  }
                }, {
                  "match" : {
                    "lpi.paoText" : {
                      "query" : "$hybridPafBuildingName",
                      "type" : "boolean"
                    }
                  }
                }, {
                  "fuzzy" : {
                    "lpi.streetDescriptor" : {
                      "value" : "$hybridPafThoroughfare",
                      "fuzziness" : "2",
                      "boost" : 1.0
                    }
                  }
                }, {
                  "match" : {
                    "lpi.townName" : {
                      "query" : "$hybridPafPostTown",
                      "type" : "boolean"
                    }
                  }
                }, {
                  "match" : {
                    "lpi.postcodeLocator" : {
                      "query" : "$hybridPafPostcode",
                      "type" : "boolean"
                    }
                  }
                }, {
                  "match" : {
                    "paf.buildingNumber" : {
                      "query" : "$hybridPafBuildingNumber",
                      "type" : "boolean",
                      "boost" : 5.0
                    }
                  }
                }, {
                  "match" : {
                    "paf.organizationName" : {
                      "query" : "$hybridPafOrganizationName",
                      "type" : "boolean"
                    }
                  }
                }, {
                  "match" : {
                    "paf.departmentName" : {
                      "query" : "$hybridPafDepartmentName",
                      "type" : "boolean"
                    }
                  }
                }, {
                  "match" : {
                    "paf.subBuildingName" : {
                      "query" : "$hybridPafSubBuildingName",
                      "type" : "boolean",
                      "boost" : 1.0
                    }
                  }
                }, {
                  "match" : {
                    "paf.buildingName" : {
                      "query" : "$hybridPafSubBuildingName",
                      "type" : "boolean",
                      "boost" : 0.5
                    }
                  }
                }, {
                  "match" : {
                    "paf.buildingName" : {
                      "query" : "$hybridPafBuildingName",
                      "type" : "boolean"
                    }
                  }
                }, {
                  "fuzzy" : {
                    "paf.thoroughfare" : {
                      "value" : "$hybridPafThoroughfare",
                      "fuzziness" : "2",
                      "boost" : 1.0
                    }
                  }
                }, {
                  "match" : {
                    "paf.postTown" : {
                      "query" : "$hybridPafPostTown",
                      "type" : "boolean"
                    }
                  }
                }, {
                  "match" : {
                    "paf.postcode" : {
                      "query" : "$hybridPafPostcode",
                      "type" : "boolean"
                    }
                  }
                }, {
                  "match" : {
                    "paf.dependentLocality" : {
                      "query" : "$hybridNagLocality",
                      "type" : "boolean"
                    }
                  }
                }, {
                  "match" : {
                    "_all" : {
                      "query" : "$hybridPafThoroughfare $hybridPafBuildingNumber $hybridNagPaoStartSuffix $hybridPafDepartmentName $hybridPafSubBuildingName $hybridPafPostTown $hybridPafPostcode $hybridNagPaoEndNumber $hybridPafOrganizationName $hybridNagLocality $hybridNagPaoStartNumber $hybridPafBuildingName",
                      "type" : "boolean",
                      "boost" : 30.0
                    }
                  }
                } ],
                "minimum_should_match" : "45%"
              }
            }
          }
        """
      )

      // When
      val result = Json.parse(repository.generateQueryAddressRequest(tokens).toString)

      // Then
      result shouldBe expected
    }

    "bulk search addresses" in {
      // Given
      val repository = new AddressIndexRepository(config, elasticClientProvider)

      val firstAddressTokens = Map(
        Tokens.buildingNumber -> hybridNagPaoStartNumber,
        Tokens.locality -> hybridNagLocality,
        Tokens.organisationName -> hybridNagOrganisation,
        Tokens.postcode -> hybridNagPostcodeLocator
      )

      val secondAddressTokens = Map(
        Tokens.buildingNumber -> secondaryHybridNagPaoStartNumber,
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

      addresses(0).hybridAddress.uprn shouldBe hybridFirstUprn
      addresses(1).hybridAddress.uprn shouldBe hybridSecondaryUprn
    }

    "return empty BulkAddress if there were no results for an address" in {
      // Given
      val repository = new AddressIndexRepository(config, elasticClientProvider)

      val firstAddressTokens = Map(
        Tokens.buildingNumber -> "ThisBuildingNumberDoesNotExist"
      )

      val secondAddressTokens = Map(
        Tokens.buildingNumber -> "ThisBuildingNumberDoesNotExist"
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
