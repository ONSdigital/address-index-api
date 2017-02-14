package uk.gov.ons.addressIndex.server.modules

import uk.gov.ons.addressIndex.server.model.dao.ElasticClientProvider
import com.sksamuel.elastic4s.ElasticClient
import com.sksamuel.elastic4s.ElasticDsl._
import com.sksamuel.elastic4s.testkit._
import org.scalatest.WordSpec
import play.api.libs.json.Json
import uk.gov.ons.addressIndex.crfscala.CrfScala.CrfTokenResult
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
    "recordIdentifier" -> "a1",
    "changeType" -> "a2",
    "proOrder" -> "a3",
    "uprn" -> "a4",
    "udprn" -> "a5",
    "organizationName" -> "a6",
    "departmentName" -> "a7",
    "subBuildingName" -> "a8",
    "buildingName" -> "a9",
    "buildingNumber" -> "a10",
    "dependentThoroughfare" -> "a11",
    "thoroughfare" -> "a12",
    "doubleDependentLocality" -> "a13",
    "dependentLocality" -> "a14",
    "postTown" -> "a15",
    "postcode" -> "a16",
    "postcodeType" -> "a17",
    "deliveryPointSuffix" -> "a18",
    "welshDependentThoroughfare" -> "a19",
    "welshThoroughfare" -> "a20",
    "welshDoubleDependentLocality" -> "a21",
    "welshDependentLocality" -> "a22",
    "welshPostTown" -> "a23",
    "poBoxNumber" -> "a24",
    "processDate" -> "a25",
    "startDate" -> "a26",
    "endDate" -> "a27",
    "lastUpdateDate" -> "a28",
    "entryDate" -> "a29"
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
    "uprn" -> "1n1",
    "postcodeLocator" -> "1n2",
    "addressBasePostal" -> "1n3",
    "usrn" -> "1n4",
    "lpiKey" -> "1n5",
    "paoText" -> "1n6",
    "paoStartNumber" -> "1n7",
    "paoStartSuffix" -> "1n8",
    "paoEndNumber" -> "1n9",
    "paoEndSuffix" -> "1n10",
    "saoText" -> "1n11",
    "saoStartNumber" -> "1n12",
    "saoStartSuffix" -> "1n13",
    "saoEndNumber" -> "1n14",
    "saoEndSuffix" -> "1n15",
    "level" -> "1n16",
    "officialFlag" -> "1n17",
    "logicalStatus" -> "1n18",
    "streetDescriptor" -> "1n19",
    "townName" -> "1n20",
    "locality" -> "1n21",
    "organisation" -> "1n22",
    "legalName" -> "1n23",
    "lat" -> "1.0000000",
    "lon" -> "2.0000000",
    "northing" -> "3",
    "easting" -> "4",
    "classificationCode" -> "n24"
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
      val tokens = Seq(
        CrfTokenResult(hybridNagPaoStartNumber, Tokens.buildingNumber),
        CrfTokenResult(hybridNagLocality, Tokens.locality),
        CrfTokenResult(hybridNagOrganisation, Tokens.organisationName),
        CrfTokenResult(hybridNagPostcodeLocator, Tokens.postcode)
      )
      val expectedScore = 1.3f

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
      val tokens = Seq(
        CrfTokenResult("SomeStringThatWontHaveAnyResult", Tokens.buildingNumber)
      )

      // When
      val HybridAddresses(results, maxScore, total) = repository.queryAddresses(0, 10, tokens).await

      // Then
      results.length shouldBe 0
      maxScore shouldBe 0
      total shouldBe 0
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
                      "boost" : 1.0
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
                      "boost" : 1.0
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
                      "boost" : 0.5
                    }
                  }
                } ],
                "minimum_should_match" : "55%"
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

  }

}
