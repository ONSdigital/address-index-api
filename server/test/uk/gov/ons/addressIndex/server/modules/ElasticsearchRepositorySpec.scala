package uk.gov.ons.addressIndex.server.modules

import com.sksamuel.elastic4s.http.JavaClient
import com.sksamuel.elastic4s.requests.analyzers.{CustomAnalyzerDefinition, StandardTokenizer}
import com.sksamuel.elastic4s.requests.searches.SearchBodyBuilderFn
import com.sksamuel.elastic4s.testkit._
import com.sksamuel.elastic4s.{ElasticClient, ElasticNodeEndpoint, ElasticProperties}
import org.joda.time.DateTime
import org.scalatest.WordSpec
import org.testcontainers.elasticsearch.ElasticsearchContainer
import play.api.libs.json.Json
import uk.gov.ons.addressIndex.model.config.QueryParamsConfig
import uk.gov.ons.addressIndex.model.db.BulkAddressRequestData
import uk.gov.ons.addressIndex.model.db.index._
import uk.gov.ons.addressIndex.parsers.Tokens
import uk.gov.ons.addressIndex.server.model.dao.ElasticClientProvider

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.Try

class ElasticsearchRepositorySpec extends WordSpec with SearchMatchers with ElasticClientProvider with ClientProvider with ElasticSugar {

  val container = new ElasticsearchContainer()
  container.setDockerImageName("docker.elastic.co/elasticsearch/elasticsearch-oss:7.3.1")
  container.start()
  val containerHost: String = container.getHttpHostAddress
  val host: String =  containerHost.split(":").headOption.getOrElse("localhost")
  val port:Int =  Try(containerHost.split(":").lastOption.getOrElse("9200").toInt).getOrElse(9200)

  val elEndpoint: ElasticNodeEndpoint = ElasticNodeEndpoint("http",host,port,None)
  val eProps: ElasticProperties = ElasticProperties(endpoints = Seq(elEndpoint))

  val client: ElasticClient = ElasticClient(JavaClient(eProps))
  val clientFullmatch: ElasticClient = ElasticClient(JavaClient(eProps))
  val clientSpecialCensus: ElasticClient = ElasticClient(JavaClient(eProps))

  val testClient: ElasticClient = client.copy()
  val testClient2: ElasticClient = clientFullmatch.copy()
  val testClient3: ElasticClient = clientSpecialCensus.copy()

 //  injections
   val elasticClientProvider: ElasticClientProvider = new ElasticClientProvider {
      override def client: ElasticClient = testClient
      /* Not currently used in tests as it doesn't look like you can have two test ES instances */
      override def clientFullmatch: ElasticClient = testClient2
      override def clientSpecialCensus: ElasticClient = testClient3
   }

  val defaultLat = "50.705948"
  val defaultLon = "-3.5091076"

  val defaultEpoch = "_current"

  val config = new AddressIndexConfigModule
  val queryParams: QueryParamsConfig = config.config.elasticSearch.queryParams
  val dateMillis: Long = DateTime.now().getMillis
  val hybridIndexName: String = config.config.elasticSearch.indexes.hybridIndex + "_" + dateMillis + defaultEpoch
  val hybridIndexHistoricalName: String = config.config.elasticSearch.indexes.hybridIndexHistorical + "_" +  dateMillis + defaultEpoch

  val hybridRelLevel = 1
  val hybridRelSibArray = List(6L, 7L)
  val hybridRelParArray = List(8L, 9L)

  val firstHybridRelEs: Map[String, Any] = Map(
    "level" -> hybridRelLevel,
    "siblings" -> hybridRelSibArray,
    "parents" -> hybridRelParArray
  )

  val secondHybridRelEs: Map[String, Any] = Map(
    "level" -> hybridRelLevel,
    "siblings" -> hybridRelSibArray,
    "parents" -> hybridRelParArray
  )

  val hybridCrossRefReference = "osgb1000000347959147"
  val hybridCrossRefSource = "7666MT"
  val hybridCrossRefReference2 = "acrossref"
  val hybridCrossRefSource2 = "7663TU"

  val firstHybridCrossRefEs: Map[String, Any] = Map(
    "crossReference" -> hybridCrossRefReference,
    "source" -> hybridCrossRefSource
  )

  val secondHybridCrossRefEs: Map[String, Any] = Map(
    "crossReference" -> hybridCrossRefReference2,
    "source" -> hybridCrossRefSource2
  )

  val hybridFirstUprn = 1L
  val hybridFirstUprnHist = 2L
  val hybridFirstParentUprn = 3L
  val hybridFirstDateUprn = 10L
  val hybridFirstClassificationCode = "R"
  val hybridSecondDateUprn = 11L
  val hybridThirdDateUprn = 12L
  val hybridFirstRelative: Map[String, Any] = firstHybridRelEs
  val hybridFirstPostcodeIn = "h01p"
  val hybridFirstPostcodeOut = "h02p"
  // Fields that are not in this list are not used for search
  val hybridPafUprn = 1L
  val hybridPafOrganisationName = "h2"
  val hybridPafDepartmentName = "h3"
  val hybridPafSubBuildingName = "h4"
  val hybridPafBuildingName = "h5"
  val hybridPafBuildingNumber: Short = 6.toShort
  val hybridPafThoroughfare = "h7"
  val hybridPafPostTown = "h8"
  val hybridPafPostcode = "h10"
  val hybridAll = "H100 H4 H6"
  val hybridMixedPaf = "mixedPaf"
  val hybridMixedWelshPaf = "MixedWelshPaf"
  val hybridMixedNag = "mixedNag"
  val hybridCensusAddressType = "NA"
  val hybridCensusEstabType = "NA"
  val hybridFromSource = "EW"
  val hybridCountryCode = "E"

  // Fields that are not in this list are not used for search
  val hybridNagUprn: Long = hybridPafUprn
  val hybridNagPostcodeLocator: String = hybridPafPostcode
  val hybridNagPaoStartNumber: Short = 13.toShort
  val hybridNagPaoStartSuffix: String = "h11"
  val hybridNagPaoEndNumber: Short = 12.toShort
  val hybridNagPaoEndSuffix: String = "h14"
  val hybridNagSaoStartNumber: Short = 15.toShort
  val hybridNagSaoStartSuffix: String = "h16"
  val hybridNagSaoEndNumber: Short = 17.toShort
  val hybridNagSaoEndSuffix: String = "h18"
  val hybridNagLocality: String = "h20"
  val hybridNagOrganisation: String = hybridPafOrganisationName
  val hybridNagLegalName: String = hybridPafOrganisationName
  val hybridNagSaoText: String = hybridPafSubBuildingName
  val hybridNagPaoText: String = hybridPafBuildingName
  val hybridNagStreetDescriptor: String = hybridPafThoroughfare
  val hybridNagTownName: String = hybridPafPostTown
  val hybridNagLatitude: Float = 1.0000000f
  val hybridNagLongitude: Float = -2.0000000f
  val hybridNagNorthing: Float = 3f
  val hybridNagEasting: Float = 4f
  val hybridNagCustCode: String = "1110"
  val hybridNagCustName: String = "Exeter"

  val hybridMixedNisra = "mixedNisra"
  val hybridNisraOrganisationName: String = hybridPafOrganisationName
  val hybridNisraSubBuildingName: String = hybridPafSubBuildingName
  val hybridNisraBuildingName: String = hybridPafBuildingName
  val hybridNisraBuildingNumber = "h26"
  val hybridNisraThoroughfare: String = hybridPafThoroughfare
  val hybridNisraAltThoroughfare = "h27"
  val hybridNisraDependentThoroughfare = "h28"
  val hybridNisraLocality = "h29"
  val hybridNisraTownland = "h30"
  val hybridNisraTownName = "h31"
  val hybridNisraPostcode = "h32"
  val hybridNisraUprn = "h25"
  val hybridNisraPostTown = "h33"
  val hybridNisraEasting = "h21"
  val hybridNisraNorthing = "h22"
  val hybridNisraLatitude = "h23"
  val hybridNisraLongitude = "h24"
  val hybridNisraLocalCouncil = "BELFAST"
  val hybridNisraLGDCode = "N09000003"

  val hybridNagCustGeogCode = "E07000041"
  val hybridStartDate = "2013-01-01"
  val hybridEndDate = "2014-01-01"
  val hybridSecondStartDate = "2014-01-02"
  val hybridCurrentEndDate: String = DateTime.now.toString("yyyy-MM-dd")
  val hybridThirdStartDate = "2015-01-01"

  // Fields with this value are not used in the search and are, thus, irrelevant
  val hybridNotUsed = ""
  val hybridNotUsedNull: Null = null

  // Secondary PAF/NAG is used for single search (to have some "concurrence" for the main address)
  // and in the Multi Search
  val hybridSecondaryUprn = 2L
  val hybridSecondaryParentUprn = 4L
  val hybridSecondaryRelative: Map[String, Any] = secondHybridRelEs
  val hybridSecondaryCrossref: Map[String, Any] = secondHybridCrossRefEs
  val hybridSecondaryPostcodeIn = "s01p"
  val hybridSecondaryPostcodeOut = "s02p"

  // Fields that are not in this list are not used for search
  val secondaryHybridPafUprn = 2L
  val secondaryHybridPafOrganisationName = "s2"
  val secondaryHybridPafDepartmentName = "s3"
  val secondaryHybridPafSubBuildingName = "s4"
  val secondaryHybridPafBuildingName = "s5"
  val secondaryHybridPafBuildingNumber: Short = 7.toShort
  val secondaryHybridPafThoroughfare = "s7"
  val secondaryHybridPafPostTown = "s8"
  val secondaryHybridPafPostcode = "s10"
  val secondaryHybridAll = "s200"

  // Fields that are not in this list are not used for search
  val secondaryHybridNagUprn: Long = secondaryHybridPafUprn
  val secondaryHybridNagPostcodeLocator: String = secondaryHybridPafPostcode
  val secondaryHybridNagPaoStartNumber: Short = 20.toShort
  val secondaryHybridNagPaoStartSuffix = "s11"
  val secondaryHybridNagPaoEndNumber: Short = 21.toShort
  val secondaryHybridNagPaoEndSuffix = "s14"
  val secondaryHybridNagSaoStartNumber: Short = 22.toShort
  val secondaryHybridNagSaoStartSuffix = "s16"
  val secondaryHybridNagSaoEndNumber: Short = 23.toShort
  val secondaryHybridNagSaoEndSuffix = "s18"
  val secondaryHybridNagLocality = "s20"
  val secondaryHybridNagOrganisation: String = secondaryHybridPafOrganisationName
  val secondaryHybridNagLegalName: String = secondaryHybridPafOrganisationName
  val secondaryHybridNagSaoText: String = secondaryHybridPafSubBuildingName
  val secondaryHybridNagPaoText: String = secondaryHybridPafBuildingName
  val secondaryHybridNagStreetDescriptor: String = secondaryHybridPafThoroughfare
  val secondaryHybridNagTownName: String = secondaryHybridPafPostTown
  val secondaryHybridNagLatitude = 7.0000000f
  val secondaryHybridNagLongitude = 8.0000000f
  val secondaryHybridNagNorthing = 10f
  val secondaryHybridNagEasting = 11f
  val secondardyHybridNagLocalCustodianName = "Exeter"
  val secondardyHybridNagLocalCustodianCode = "1110"

  val firstHybridPafEs: Map[String, Any] = Map[String, Any](
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
    "pafAll" -> hybridAll,
    "mixedPaf" -> hybridMixedPaf,
    "mixedWelshPaf" -> hybridMixedWelshPaf
  )

  val secondHybridPafEs: Map[String, Any] = Map[String, Any](
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
    "entryDate" -> hybridNotUsed,
    "pafAll" -> secondaryHybridAll,
    "mixedPaf" -> hybridMixedPaf,
    "mixedWelshPaf" -> hybridMixedWelshPaf
  )

  val thirdHybridPafEs: Map[String, Any] = firstHybridPafEs + (
    "uprn" -> hybridSecondDateUprn,
    "startDate" -> hybridSecondStartDate,
    "endDate" -> hybridCurrentEndDate
  )

  val fourthHybridPafEs: Map[String, Any] = firstHybridPafEs + (
    "uprn" -> hybridThirdDateUprn,
    "startDate" -> hybridThirdStartDate,
    "endDate" -> hybridCurrentEndDate
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
    "usrnMatchIndicator" -> hybridNotUsed,
    "parentUprn" -> hybridNotUsedNull,
    "streetClassification" -> hybridNotUsedNull,
    "blpuLogicalStatus" -> hybridNotUsedNull,
    "lpiLogicalStatus" -> hybridNotUsedNull,
    "multiOccCount" -> hybridNotUsedNull,
    "location" -> List(hybridNagLongitude, hybridNagLatitude),
    "language" -> hybridNotUsed,
    "localCustodianCode" -> secondardyHybridNagLocalCustodianCode,
    "localCustodianName" -> secondardyHybridNagLocalCustodianName,
    "localCustodianGeogCode" -> hybridNotUsedNull,
    "rpc" -> hybridNotUsedNull,
    "nagAll" -> hybridAll,
    "mixedNag" -> hybridMixedNag
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
    "usrnMatchIndicator" -> hybridNotUsed,
    "parentUprn" -> hybridNotUsedNull,
    "streetClassification" -> hybridNotUsedNull,
    "blpuLogicalStatus" -> hybridNotUsedNull,
    "lpiLogicalStatus" -> hybridNotUsedNull,
    "multiOccCount" -> hybridNotUsedNull,
    "location" -> List(secondaryHybridNagLongitude, secondaryHybridNagLatitude),
    "nagAll" -> hybridNotUsed,
    "language" -> hybridNotUsed,
    "localCustodianCode" -> secondardyHybridNagLocalCustodianCode,
    "localCustodianName" -> secondardyHybridNagLocalCustodianName,
    "localCustodianGeogCode" -> hybridNotUsedNull,
    "rpc" -> hybridNotUsedNull,
    "nagAll" -> secondaryHybridAll,
    "mixedNag" -> hybridMixedNag
  )

  val thirdHybridNagEs: Map[String, Any] = firstHybridNagEs + (
    "uprn" -> hybridFirstDateUprn,
    "lpiStartDate" -> hybridStartDate,
    "lpiEndDate" -> hybridEndDate
  )

  val fourthHybridNagEs: Map[String, Any] = firstHybridNagEs + (
    "uprn" -> hybridSecondDateUprn,
    "lpiStartDate" -> hybridSecondStartDate,
    "lpiEndDate" -> hybridCurrentEndDate
  )

  val fifthHybridNagEs: Map[String, Any] = firstHybridNagEs + (
    "uprn" -> hybridSecondDateUprn,
    "lpiStartDate" -> hybridStartDate,
    "lpiEndDate" -> hybridEndDate
  )

  val firstHybridEs: Map[String, Any] = Map(
    "uprn" -> hybridFirstUprn,
    "parentUprn" -> hybridFirstParentUprn,
    "relatives" -> Seq(hybridFirstRelative),
    "crossRefs" -> Seq(firstHybridCrossRefEs, secondHybridCrossRefEs),
    "postcodeIn" -> hybridFirstPostcodeIn,
    "postcodeOut" -> hybridFirstPostcodeOut,
    "paf" -> Seq(firstHybridPafEs),
    "lpi" -> Seq(firstHybridNagEs),
    "classificationCode" -> hybridFirstClassificationCode,
    "censusAddressType" -> hybridCensusAddressType,
    "censusEstabType" -> hybridCensusEstabType,
    "fromSource" -> hybridFromSource,
    "countryCode" -> hybridCountryCode
  )

  val firstHybridHistEs: Map[String, Any] = firstHybridEs + ("uprn" -> hybridFirstUprnHist)

  // This one is used to create a "concurrent" for the first one (the first one should be always on top)
  val secondHybridEs: Map[String, Any] = Map(
    "uprn" -> hybridSecondaryUprn,
    "parentUprn" -> hybridSecondaryParentUprn,
    "relatives" -> Seq(hybridSecondaryRelative),
    "crossRefs" -> Seq(hybridSecondaryCrossref),
    "postcodeIn" -> hybridSecondaryPostcodeIn,
    "postcodeOut" -> hybridSecondaryPostcodeOut,
    "paf" -> Seq(secondHybridPafEs),
    "lpi" -> Seq(secondHybridNagEs),
    "classificationCode" -> hybridFirstClassificationCode,
    "censusAddressType" -> hybridCensusAddressType,
    "censusEstabType" -> hybridCensusEstabType,
    "fromSource" -> hybridFromSource,
    "countryCode" -> hybridCountryCode
  )

  val thirdHybridEs: Map[String, Any] = firstHybridEs + (
    "uprn" -> hybridFirstDateUprn,
    "lpi" -> Seq(thirdHybridNagEs),
    "paf" -> Seq())

  val fourthHybridEs: Map[String, Any] = firstHybridEs + (
    "uprn" -> hybridSecondDateUprn,
    "lpi" -> Seq(fourthHybridNagEs, fifthHybridNagEs),
    "paf" -> Seq(thirdHybridPafEs))

  val fifthHybridEs: Map[String, Any] = firstHybridEs + (
    "uprn" -> hybridThirdDateUprn,
    "lpi" -> Seq(),
    "paf" -> Seq(fourthHybridPafEs))

  // todo get it to work with new analysis package
  testClient.execute {
    createIndex(hybridIndexName)
      .analysis(Some(CustomAnalyzerDefinition("welsh_split_synonyms_analyzer",
        StandardTokenizer("myTokenizer1"))
     ))
  }.await

  def blockUntilCountLocal(expected: Long, index: String): Unit = {
    blockUntil(s"Expected count of $expected") { () =>
      val result = testClient.execute {
        search(index).matchAllQuery().size(0)
      }.await
      expected <= result.toOption.getOrElse(null).totalHits
    }
  }

  testClient.execute {
    bulk(
        indexInto(hybridIndexName).fields(firstHybridHistEs)
    )
  }.await

  blockUntilCountLocal(1, hybridIndexName)

  // todo get it to work with new analysis package
  testClient.execute {
    createIndex(hybridIndexHistoricalName)
//      .analysis(testAnalysis)
          .analysis(Some(CustomAnalyzerDefinition("welsh_split_synonyms_analyzer",
            StandardTokenizer("myTokenizer1"))
        ))
  }.await


  testClient.execute {
    bulk(
      indexInto(hybridIndexHistoricalName).fields(firstHybridEs),
      indexInto(hybridIndexHistoricalName).fields(secondHybridEs)
    )
  }.await

  blockUntilCountLocal(2, hybridIndexHistoricalName)

  // The following documents are added separately as the blocking action on 5 documents was timing out the test
  testClient.execute {
    bulk(
      indexInto(hybridIndexHistoricalName).fields(thirdHybridEs),
      indexInto(hybridIndexHistoricalName).fields(fourthHybridEs),
      indexInto(hybridIndexHistoricalName).fields(fifthHybridEs)
    )
  }.await

  blockUntilCountLocal(3, hybridIndexHistoricalName)

  testClient.execute{
    addAlias("index_full_nohist_current",hybridIndexName)
  }.await

  testClient.execute{
    addAlias("index_full_hist_current",hybridIndexHistoricalName)
  }.await

  testClient.execute{
    updateIndexLevelSettings(hybridIndexName).numberOfReplicas(0)
  }.await

  testClient.execute{
    updateIndexLevelSettings(hybridIndexHistoricalName).numberOfReplicas(0)
  }.await

  val expectedPaf: PostcodeAddressFileAddress = PostcodeAddressFileAddress(
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
    hybridPafThoroughfare,
    hybridNotUsed,
    hybridNotUsed,
    hybridPafPostTown,
    hybridNotUsed,
    hybridNotUsed,
    hybridNotUsed,
    hybridNotUsed,
    hybridNotUsed,
    hybridNotUsed,
    hybridAll,
    hybridMixedPaf,
    hybridMixedWelshPaf
  )

  val expectedDatePaf: PostcodeAddressFileAddress = expectedPaf.copy(
    uprn = hybridSecondDateUprn.toString,
    startDate = hybridSecondStartDate,
    endDate = hybridCurrentEndDate
  )

  val expectedSecondDatePaf: PostcodeAddressFileAddress = expectedPaf.copy(
    uprn = hybridThirdDateUprn.toString,
    startDate = hybridThirdStartDate,
    endDate = hybridCurrentEndDate
  )

  val expectedNag: NationalAddressGazetteerAddress = NationalAddressGazetteerAddress(
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
    hybridNagCustCode,
    hybridNagCustName,
    hybridNagCustGeogCode,
    hybridNotUsed,
    hybridAll,
    hybridNotUsed,
    hybridNotUsed,
    hybridMixedNag,
    hybridNotUsed

  )

  val expectedNisra: NisraAddress = NisraAddress(
    hybridNisraOrganisationName,
    hybridNisraSubBuildingName,
    hybridNisraBuildingName,
    hybridNisraBuildingNumber,
    Nil,
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
    hybridNisraThoroughfare,
    hybridNisraAltThoroughfare,
    hybridNisraDependentThoroughfare,
    hybridNisraLocality,
    hybridNisraTownName,
    hybridNisraPostcode,
    hybridNisraUprn,
    hybridNotUsed,
    hybridNotUsed,
    hybridNisraPostTown,
    hybridNisraEasting,
    hybridNisraNorthing,
    hybridNotUsed,
    hybridNotUsed,
    hybridNotUsed,
    hybridNisraLatitude,
    hybridNisraLongitude,
    hybridNotUsed,
    hybridNotUsed,
    hybridNisraLocalCouncil,
    hybridNisraLGDCode,
    hybridMixedNisra
  )

  val expectedDateNag: NationalAddressGazetteerAddress = expectedNag.copy(
    uprn = hybridFirstDateUprn.toString,
    lpiStartDate = hybridStartDate,
    lpiEndDate = hybridEndDate
  )

  val expectedSecondDateNag: NationalAddressGazetteerAddress = expectedNag.copy(
    uprn = hybridSecondDateUprn.toString,
    lpiStartDate = hybridSecondStartDate,
    lpiEndDate = hybridCurrentEndDate
  )

  val expectedThirdDateNag: NationalAddressGazetteerAddress = expectedNag.copy(
    uprn = hybridSecondDateUprn.toString,
    lpiStartDate = hybridStartDate,
    lpiEndDate = hybridEndDate
  )

  val expectedRelative: Relative = Relative(
    level = hybridRelLevel,
    siblings = hybridRelSibArray,
    parents = hybridRelParArray
  )

  val expectedCrossRef: CrossRef = CrossRef(
    crossReference = hybridCrossRefReference,
    source = hybridCrossRefSource
  )

  val expectedCrossRef2: CrossRef = CrossRef(
    crossReference = hybridCrossRefReference2,
    source = hybridCrossRefSource2
  )

  val expectedHybrid: HybridAddress = HybridAddress(
    uprn = hybridFirstUprn.toString,
    parentUprn = hybridFirstParentUprn.toString,
    relatives = Some(Seq(expectedRelative)),
    crossRefs = Some(Seq(expectedCrossRef, expectedCrossRef2)),
    postcodeIn = Some(hybridFirstPostcodeIn),
    postcodeOut = Some(hybridFirstPostcodeOut),
    lpi = Seq(expectedNag),
    paf = Seq(expectedPaf),
    nisra = Seq(),
    auxiliary = Seq(),
    score = 1.0f,
    classificationCode = hybridFirstClassificationCode,
    censusAddressType = "NA",
    censusEstabType = "NA",
    fromSource = "EW",
    countryCode ="E",
    highlights = Seq(Map())
  )

  val expectedDateHybrid: HybridAddress = HybridAddress(
    uprn = hybridFirstDateUprn.toString,
    parentUprn = hybridFirstParentUprn.toString,
    relatives = Some(Seq(expectedRelative)),
    crossRefs = Some(Seq(expectedCrossRef, expectedCrossRef2)),
    postcodeIn = Some(hybridFirstPostcodeIn),
    postcodeOut = Some(hybridFirstPostcodeOut),
    lpi = Seq(expectedDateNag),
    paf = Seq(),
    nisra = Seq(),
    auxiliary = Seq(),
    score = 1.0f,
    classificationCode = hybridFirstClassificationCode,
    censusAddressType = "NA",
    censusEstabType = "NA",
    fromSource = "EW",
    countryCode ="E",
    highlights = Seq()
  )

  val expectedSecondDateHybrid: HybridAddress = HybridAddress(
    uprn = hybridSecondDateUprn.toString,
    parentUprn = hybridFirstParentUprn.toString,
    relatives = Some(Seq(expectedRelative)),
    crossRefs = Some(Seq(expectedCrossRef, expectedCrossRef2)),
    postcodeIn = Some(hybridFirstPostcodeIn),
    postcodeOut = Some(hybridFirstPostcodeOut),
    lpi = Seq(expectedSecondDateNag, expectedThirdDateNag),
    paf = Seq(expectedDatePaf),
    nisra = Seq(),
    auxiliary = Seq(),
    score = 1.0f,
    classificationCode = hybridFirstClassificationCode,
    censusAddressType = "NA",
    censusEstabType = "NA",
    fromSource = "EW",
    countryCode = "E",
    highlights = Seq()
  )

  val expectedThirdDateHybrid: HybridAddress = HybridAddress(
    uprn = hybridThirdDateUprn.toString,
    parentUprn = hybridFirstParentUprn.toString,
    relatives = Some(Seq(expectedRelative)),
    crossRefs = Some(Seq(expectedCrossRef, expectedCrossRef2)),
    postcodeIn = Some(hybridFirstPostcodeIn),
    postcodeOut = Some(hybridFirstPostcodeOut),
    lpi = Seq(),
    paf = Seq(expectedSecondDatePaf),
    nisra = Seq(),
    auxiliary = Seq(),
    score = 1.0f,
    classificationCode = hybridFirstClassificationCode,
    censusAddressType = "NA",
    censusEstabType = "NA",
    fromSource = "EW",
    countryCode ="E",
    highlights = Seq()
  )

  val expectedHybridHist: HybridAddress = expectedHybrid.copy(uprn = hybridFirstUprnHist.toString)

  val partialInput = "7 Gate Re"
  val partialInputWithout = "Gate Re"
  val partialInputFallback = "7 Gate Ret"
  val partialInputWithoutFallback = "Gate Ret"
  val partialFilterNone = ""
  val partialFilterCode = "RD"
  val partialFilterPrefix = "residential"

  "Elastic repository" should {

    "generate valid query for search by UPRN" in {
      // Given
      val repository = new AddressIndexRepository(config, elasticClientProvider)

      val expected = Json.parse(
        """
        {
          "query" : {
            "term" : {
            "uprn" : {"value":"1"}
            }
          }
        }
        """
      )

      // When
      val args = UPRNArgs(
        uprn = hybridFirstUprn.toString,
      )
      val result = Json.parse(SearchBodyBuilderFn(repository.makeQuery(args)).string())

      // Then
      result shouldBe expected
    }

    "find HYBRID address by UPRN" in {
      // Given
      val repository = new AddressIndexRepository(config, elasticClientProvider)
      val expected = Some(expectedHybrid)

      // When
      val args = UPRNArgs(
        uprn = hybridFirstUprn.toString,
      )
      val result = repository.runUPRNQuery(args).await

      // Then
      result.get.lpi.head shouldBe expectedNag
      result.get.paf.head shouldBe expectedPaf
      result shouldBe expected
    }

    "generate valid query from partial address" in {
      // Given
      val repository = new AddressIndexRepository(config, elasticClientProvider)
      val expected = Json.parse(
        s"""
          {"query":{"function_score":{"query":{"bool":{"must":[{"multi_match":{"query":"h4","fields":["mixedPartial"],"type":"phrase","slop":25}}],"should":[{"dis_max":{"queries":[{"prefix":{"lpi.mixedNagStart":{"value":"H4","boost":1.25}}},{"prefix":{"lpi.mixedWelshNagStart":{"value":"H4","boost":1.25}}},{"prefix":{"paf.mixedPafStart":{"value":"H4","boost":1.25}}},{"prefix":{"paf.mixedWelshPafStart":{"value":"H4","boost":1.25}}},{"prefix":{"nisra.mixedNisraStart":{"value":"H4","boost":1.25}}}]}}],"filter":[{"bool":{"should":[{"prefix":{"classificationCode":{"value":"RD"}}},{"prefix":{"classificationCode":{"value":"RH"}}},{"prefix":{"classificationCode":{"value":"RI"}}}],"minimum_should_match":"1"}}]}},"min_score":1,"boost_mode":"replace","functions":[{"script_score":{"script":{"source":"Math.round(_score/1.8)"}}}]}},"from":0,"size":1,"sort":[{"_score":{"order":"desc"}},{"postcodeStreetTown":{"order":"asc"}},{"lpi.paoStartNumber":{"order":"asc"}},{"lpi.paoStartSuffix.keyword":{"order":"asc"}},{"lpi.secondarySort":{"order":"asc"}},{"nisra.paoStartNumber":{"order":"asc"}},{"nisra.secondarySort":{"order":"asc"}},{"uprn":{"order":"asc"}}],"highlight":{"number_of_fragments":0,"fields":{"mixedPartial":{}}}}
        """.stripMargin
      )

      // When
      val args = PartialArgs(
        input = "h4",
        filters = "residential",
        limit = 1,
      )
      val query = repository.makeQuery(args)
      val result = Json.parse(SearchBodyBuilderFn(query).string())

      // Then
      result shouldBe expected
    }

    "generate valid fallback query from partial address" in {
      // Given
      val repository = new AddressIndexRepository(config, elasticClientProvider)
      val expected = Json.parse(
        s"""
          {"query":{"function_score":{"query":{"bool":{"must":[{"multi_match":{"query":"h4","fields":["mixedPartial"],"type":"best_fields"}}],"should":[{"dis_max":{"queries":[{"prefix":{"lpi.mixedNagStart":{"value":"H4","boost":1.25}}},{"prefix":{"lpi.mixedWelshNagStart":{"value":"H4","boost":1.25}}},{"prefix":{"paf.mixedPafStart":{"value":"H4","boost":1.25}}},{"prefix":{"paf.mixedWelshPafStart":{"value":"H4","boost":1.25}}},{"prefix":{"nisra.mixedNisraStart":{"value":"H4","boost":1.25}}}]}}],"filter":[{"bool":{"should":[{"prefix":{"classificationCode":{"value":"RD"}}},{"prefix":{"classificationCode":{"value":"RH"}}},{"prefix":{"classificationCode":{"value":"RI"}}}],"minimum_should_match":"1"}}]}},"min_score":1,"boost_mode":"replace","functions":[{"script_score":{"script":{"source":"Math.round(_score/1.8)"}}}]}},"from":0,"size":1,"sort":[{"_score":{"order":"desc"}},{"postcodeStreetTown":{"order":"asc"}},{"lpi.paoStartNumber":{"order":"asc"}},{"lpi.paoStartSuffix.keyword":{"order":"asc"}},{"lpi.secondarySort":{"order":"asc"}},{"nisra.paoStartNumber":{"order":"asc"}},{"nisra.secondarySort":{"order":"asc"}},{"uprn":{"order":"asc"}}],"highlight":{"number_of_fragments":0,"fields":{"mixedPartial":{}}}}
        """.stripMargin
      )

      // When
      val args = PartialArgs(
        input = "h4",
        fallback = true,
        filters = "residential",
        limit = 1
      )
      val query = repository.makePartialSearch(args, fallback = true)
      val result = Json.parse(SearchBodyBuilderFn(query).string())

      // Then
      result shouldBe expected
    }

    "find HYBRID address by UPRN in non-historical index" in {
      // Given
      val repository = new AddressIndexRepository(config, elasticClientProvider)
      val expected = Some(expectedHybrid)

      // When
      val args = UPRNArgs(
        uprn = hybridFirstUprn.toString,
        historical = true,
      )
      val result = repository.runUPRNQuery(args).await

      // Then
      result.get.lpi.head shouldBe expectedNag
      result.get.paf.head shouldBe expectedPaf
      result shouldBe expected
    }

    "find HYBRID address by postcode" in {
      // Given
      val repository = new AddressIndexRepository(config, elasticClientProvider)
      val expected = Json.parse(
        s"""
          {"query":{"bool":{"must":[{"bool":{"must":[{"term":{"postcode":{"value":" H4"}}}],"filter":[{"bool":{"should":[{"prefix":{"classificationCode":{"value":"RD"}}},{"prefix":{"classificationCode":{"value":"RH"}}},{"prefix":{"classificationCode":{"value":"RI"}}}],"minimum_should_match":"1"}}]}}]}},"from":0,"size":1,"sort":[{"lpi.streetDescriptor.keyword":{"order":"asc"}},{"lpi.paoStartNumber":{"order":"asc"}},{"lpi.paoStartSuffix.keyword":{"order":"asc"}},{"lpi.secondarySort":{"order":"asc"}},{"nisra.thoroughfare.keyword":{"order":"asc"}},{"nisra.paoStartNumber":{"order":"asc"}},{"nisra.secondarySort":{"order":"asc"}},{"uprn":{"order":"asc"}}]}
         """.stripMargin
      )

      // When
      val args = PostcodeArgs(
        postcode = "h4",
        limit = 1,
        filters = "residential",
      )
      val result = Json.parse(SearchBodyBuilderFn(repository.makeQuery(args)).string())

      // Then
      result shouldBe expected
    }

    "return aggregration query for partial postcode" in {
      // Given
      val repository = new AddressIndexRepository(config, elasticClientProvider)
      val expected = Json.parse(
        s"""
          {"query":{"bool":{"must":[{"bool":{"must":[{"prefix":{"postcode":{"value":"H4"}}}],"filter":[{"bool":{"should":[{"prefix":{"classificationCode":{"value":"RD"}}},{"prefix":{"classificationCode":{"value":"RH"}}},{"prefix":{"classificationCode":{"value":"RI"}}}],"minimum_should_match":"1"}}]}}]}},"from":0,"size":1,"aggs":{"uniquepostcodes":{"terms":{"field":"postcodeStreetTown","size":1000,"order":{"_key":"asc"}},"aggs":{"uprns":{"terms":{"field":"uprn","size":1}},"paftowns":{"terms":{"field":"postTown","size":1}}}}}}
         """.stripMargin
      )

      // When
      val args = GroupedPostcodeArgs(
        postcode = "h4",
        limit = 1,
        filters = "residential",
      )
      val result = Json.parse(SearchBodyBuilderFn(repository.makeQuery(args)).string())

      // Then
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
      val args = AddressArgs(
        input = "",
        tokens = tokens,
        region = None,
        filters = "",
        limit = 10,
        verbose = false,
      )
      val HybridAddressCollection(results, aggregations@_, maxScore, total) = repository.runMultiResultQuery(args).await

      // Then
      results.length should be > 0 // it MAY return more than 1 addresses, but the top one should remain the same
      total should be > 0L

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
      val args = AddressArgs(
        input = "",
        tokens = tokens,
        region = None,
        filters = "",
        filterDateRange = DateRange(hybridStartDate, hybridEndDate),
        limit = 10,
        verbose = false,
      )
      val HybridAddressCollection(results, aggregations@_, maxScore, total) = repository.runMultiResultQuery(args).await

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

            "query":{
              "bool":{
                "must":[{
                  "dis_max":{
                  "tie_breaker":0,
                  "queries":[{
                    "match":{
                      "tokens.addressAll":{
                        "query":"",
                        "analyzer":"welsh_split_synonyms_analyzer",
                        "boost":${queryParams.fallback.fallbackAuxBoost},
                        "minimum_should_match":"${queryParams.fallback.fallbackMinimumShouldMatch}"
                      }
                    }
                  },{
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
                      "nisra.nisraAll":{
                      "query":"",
                      "analyzer":"welsh_split_synonyms_analyzer",
                      "boost":${queryParams.fallback.fallbackPafBoost},
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
                      "tokens.addressAll.bigram":{
                        "query":"",
                          "boost":${queryParams.fallback.fallbackAuxBigramBoost},
                          "fuzziness":"${queryParams.fallback.bigramFuzziness}"
                        }
                      }
                    },{
                    "match":{
                      "lpi.nagAll.bigram":{
                        "query":"",
                          "boost":${queryParams.fallback.fallbackLpiBigramBoost},
                          "fuzziness":"${queryParams.fallback.bigramFuzziness}"
                        }
                      }
                    },{
                      "match":{
                        "nisra.nisraAll.bigram":{
                          "query":"",
                           "boost":${queryParams.fallback.fallbackPafBigramBoost},
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
                  "boost":0.5
                }
              },
              "from": 0,
              "size": 1,
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
      val args = AddressArgs(
        input = "",
        tokens = tokens,
        region = None,
        filters = "",
        limit = 1,
        verbose = false,
      )
      val result = Json.parse(SearchBodyBuilderFn(repository.makeQuery(args)).string())

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
          {"query":{"dis_max":{"tie_breaker":1,"queries":[{"bool":{"should":[{"dis_max":{"tie_breaker":0,"queries":[{"constant_score":{"filter":{"match":{"tokens.subBuildingName":{"query":"h4","minimum_should_match":"-45%"}}},"boost":1}},{"constant_score":{"filter":{"match":{"paf.subBuildingName":{"query":"h4","minimum_should_match":"-45%"}}},"boost":1.5}},{"constant_score":{"filter":{"match":{"nisra.subBuildingName":{"query":"h4","minimum_should_match":"-45%"}}},"boost":1.5}},{"constant_score":{"filter":{"match":{"lpi.saoText":{"query":"h4","minimum_should_match":"-45%"}}},"boost":1.5}},{"dis_max":{"tie_breaker":0.5,"queries":[{"constant_score":{"filter":{"match":{"tokens.subBuildingName":{"query":"15"}}},"boost":1}},{"constant_score":{"filter":{"match":{"lpi.saoStartNumber":{"query":"15"}}},"boost":1}},{"constant_score":{"filter":{"match":{"paf.subBuildingName":{"query":"15"}}},"boost":1}},{"constant_score":{"filter":{"match":{"nisra.subBuildingName":{"query":"15"}}},"boost":1}},{"constant_score":{"filter":{"match":{"lpi.saoText":{"query":"15"}}},"boost":1}},{"constant_score":{"filter":{"match":{"tokens.saoStartSuffix":{"query":"h16"}}},"boost":1}},{"constant_score":{"filter":{"match":{"lpi.saoStartSuffix":{"query":"h16"}}},"boost":1}},{"constant_score":{"filter":{"match":{"paf.subBuildingName":{"query":"h16"}}},"boost":1}},{"constant_score":{"filter":{"match":{"nisra.subBuildingName":{"query":"h16"}}},"boost":1}},{"constant_score":{"filter":{"match":{"lpi.saoText":{"query":"h16"}}},"boost":1}}]}}]}},{"dis_max":{"tie_breaker":0,"queries":[{"constant_score":{"filter":{"match":{"tokens.streetName":{"query":"h7","fuzziness":"1"}}},"boost":2}},{"constant_score":{"filter":{"match":{"paf.thoroughfare":{"query":"h7","fuzziness":"1"}}},"boost":2}},{"constant_score":{"filter":{"match":{"nisra.thoroughfare":{"query":"h7","fuzziness":"1"}}},"boost":2}},{"constant_score":{"filter":{"match":{"paf.welshThoroughfare":{"query":"h7","fuzziness":"1"}}},"boost":2}},{"constant_score":{"filter":{"match":{"paf.dependentThoroughfare":{"query":"h7","fuzziness":"1"}}},"boost":0.5}},{"constant_score":{"filter":{"match":{"paf.welshDependentThoroughfare":{"query":"h7","fuzziness":"1"}}},"boost":0.5}},{"constant_score":{"filter":{"match":{"nisra.dependentThoroughfare":{"query":"h7","fuzziness":"1"}}},"boost":0.5}},{"constant_score":{"filter":{"match":{"nisra.altThoroughfare":{"query":"h7","fuzziness":"1"}}},"boost":0.5}},{"constant_score":{"filter":{"match":{"lpi.streetDescriptor":{"query":"h7","fuzziness":"1"}}},"boost":2}}]}},{"dis_max":{"tie_breaker":0,"queries":[{"constant_score":{"filter":{"match":{"postcode":{"query":"h10"}}},"boost":1}},{"constant_score":{"filter":{"match":{"paf.postcode":{"query":"h10"}}},"boost":1}},{"constant_score":{"filter":{"match":{"nisra.postcode":{"query":"h10"}}},"boost":1}},{"constant_score":{"filter":{"match":{"lpi.postcodeLocator":{"query":"h10"}}},"boost":1}},{"constant_score":{"filter":{"bool":{"must":[{"match":{"postcodeOut":{"query":"h02p","fuzziness":"1"}}},{"match":{"postcodeIn":{"query":"h01p","fuzziness":"2"}}}]}},"boost":0.5}}]}},{"dis_max":{"tie_breaker":0.5,"queries":[{"dis_max":{"tie_breaker":0,"queries":[{"constant_score":{"filter":{"match":{"tokens.buildingName":{"query":"h5","fuzziness":"1"}}},"boost":2.5}},{"constant_score":{"filter":{"match":{"paf.buildingName":{"query":"h5","fuzziness":"1"}}},"boost":2.5}},{"constant_score":{"filter":{"match":{"nisra.buildingName":{"query":"h5","fuzziness":"1"}}},"boost":2.5}},{"constant_score":{"filter":{"match":{"lpi.paoText":{"query":"h5","fuzziness":"1","minimum_should_match":"-45%"}}},"boost":2.5}},{"constant_score":{"filter":{"bool":{"must":[{"match":{"lpi.paoStartNumber":{"query":"13"}}},{"match":{"lpi.paoStartSuffix":{"query":"h11"}}}]}},"boost":3}}]}},{"dis_max":{"tie_breaker":0,"queries":[{"dis_max":{"tie_breaker":0,"boost":0.5,"queries":[{"constant_score":{"filter":{"match":{"tokens.buildingName":{"query":"h5","minimum_should_match":"-45%"}}},"boost":1}},{"constant_score":{"filter":{"match":{"paf.subBuildingName":{"query":"h5","minimum_should_match":"-45%"}}},"boost":1.5}},{"constant_score":{"filter":{"match":{"nisra.subBuildingName":{"query":"h5","minimum_should_match":"-45%"}}},"boost":1.5}},{"constant_score":{"filter":{"match":{"lpi.saoText":{"query":"h5","minimum_should_match":"-45%"}}},"boost":1.5}},{"dis_max":{"tie_breaker":0.5,"queries":[{"constant_score":{"filter":{"match":{"tokens.paoStartNumber":{"query":"13"}}},"boost":1}},{"constant_score":{"filter":{"match":{"lpi.saoStartNumber":{"query":"13"}}},"boost":1}},{"constant_score":{"filter":{"match":{"paf.subBuildingName":{"query":"13"}}},"boost":1}},{"constant_score":{"filter":{"match":{"nisra.subBuildingName":{"query":"13"}}},"boost":1}},{"constant_score":{"filter":{"match":{"lpi.saoText":{"query":"13"}}},"boost":1}},{"constant_score":{"filter":{"match":{"tokens.paoStartSuffix":{"query":"h11"}}},"boost":1}},{"constant_score":{"filter":{"match":{"lpi.saoStartSuffix":{"query":"h11"}}},"boost":1}},{"constant_score":{"filter":{"match":{"paf.subBuildingName":{"query":"h11"}}},"boost":1}},{"constant_score":{"filter":{"match":{"nisra.subBuildingName":{"query":"h11"}}},"boost":1}},{"constant_score":{"filter":{"match":{"lpi.saoText":{"query":"h11"}}},"boost":1}}]}}]}}]}}]}},{"dis_max":{"tie_breaker":0.5,"queries":[{"dis_max":{"tie_breaker":0,"queries":[{"constant_score":{"filter":{"match":{"tokens.organisationName":{"query":"h2","minimum_should_match":"30%"}}},"boost":1}},{"constant_score":{"filter":{"match":{"paf.organisationName":{"query":"h2","minimum_should_match":"30%"}}},"boost":1}},{"constant_score":{"filter":{"match":{"nisra.organisationName":{"query":"h2","minimum_should_match":"30%"}}},"boost":1}},{"constant_score":{"filter":{"match":{"lpi.organisation":{"query":"h2","minimum_should_match":"30%"}}},"boost":1}},{"constant_score":{"filter":{"match":{"lpi.paoText":{"query":"h2","minimum_should_match":"30%"}}},"boost":1}},{"constant_score":{"filter":{"match":{"lpi.legalName":{"query":"h2","minimum_should_match":"30%"}}},"boost":1}},{"constant_score":{"filter":{"match":{"lpi.saoText":{"query":"h2","minimum_should_match":"30%"}}},"boost":0.5}}]}},{"dis_max":{"tie_breaker":0,"queries":[{"constant_score":{"filter":{"match":{"tokens.departmentName":{"query":"h3","minimum_should_match":"30%"}}},"boost":1}},{"constant_score":{"filter":{"match":{"paf.departmentName":{"query":"h3","minimum_should_match":"30%"}}},"boost":1}},{"constant_score":{"filter":{"match":{"lpi.legalName":{"query":"h3","minimum_should_match":"30%"}}},"boost":0.5}}]}}]}},{"dis_max":{"tie_breaker":0.5,"queries":[{"dis_max":{"tie_breaker":0,"queries":[{"constant_score":{"filter":{"match":{"tokens.townName":{"query":"h8","fuzziness":"1"}}},"boost":1}},{"constant_score":{"filter":{"match":{"paf.postTown":{"query":"h8","fuzziness":"1"}}},"boost":1}},{"constant_score":{"filter":{"match":{"paf.welshPostTown":{"query":"h8","fuzziness":"1"}}},"boost":1}},{"constant_score":{"filter":{"match":{"nisra.townName":{"query":"h8","fuzziness":"1"}}},"boost":1}},{"constant_score":{"filter":{"match":{"lpi.townName":{"query":"h8","fuzziness":"1"}}},"boost":1}},{"constant_score":{"filter":{"match":{"paf.dependentLocality":{"query":"h8","fuzziness":"1"}}},"boost":0.5}},{"constant_score":{"filter":{"match":{"paf.welshDependentLocality":{"query":"h8","fuzziness":"1"}}},"boost":0.5}},{"constant_score":{"filter":{"match":{"lpi.locality":{"query":"h8","fuzziness":"1"}}},"boost":0.5}},{"constant_score":{"filter":{"match":{"paf.doubleDependentLocality":{"query":"h8","fuzziness":"1"}}},"boost":0.2}},{"constant_score":{"filter":{"match":{"paf.welshDoubleDependentLocality":{"query":"h8","fuzziness":"1"}}},"boost":0.2}}]}},{"dis_max":{"tie_breaker":0,"queries":[{"constant_score":{"filter":{"match":{"tokens.locality":{"query":"h20","fuzziness":"1"}}},"boost":0.6}},{"constant_score":{"filter":{"match":{"paf.postTown":{"query":"h20","fuzziness":"1"}}},"boost":0.2}},{"constant_score":{"filter":{"match":{"nisra.townName":{"query":"h20","fuzziness":"1"}}},"boost":0.2}},{"constant_score":{"filter":{"match":{"paf.welshPostTown":{"query":"h20","fuzziness":"1"}}},"boost":0.2}},{"constant_score":{"filter":{"match":{"lpi.townName":{"query":"h20","fuzziness":"1"}}},"boost":0.2}},{"constant_score":{"filter":{"match":{"paf.dependentLocality":{"query":"h20","fuzziness":"1"}}},"boost":0.6}},{"constant_score":{"filter":{"match":{"paf.welshDependentLocality":{"query":"h20","fuzziness":"1"}}},"boost":0.6}},{"constant_score":{"filter":{"match":{"nisra.locality":{"query":"h20","fuzziness":"1"}}},"boost":0.6}},{"constant_score":{"filter":{"match":{"lpi.locality":{"query":"h20","fuzziness":"1"}}},"boost":0.6}},{"constant_score":{"filter":{"match":{"paf.doubleDependentLocality":{"query":"h20","fuzziness":"1"}}},"boost":0.3}},{"constant_score":{"filter":{"match":{"paf.welshDoubleDependentLocality":{"query":"h20","fuzziness":"1"}}},"boost":0.3}}]}}]}},{"dis_max":{"tie_breaker":0.5,"queries":[{"constant_score":{"filter":{"match":{"tokens.paoStartNumber":{"query":"12"}}},"boost":0.1}},{"constant_score":{"filter":{"match":{"tokens.paoStartNumber":{"query":"13"}}},"boost":0.1}},{"constant_score":{"filter":{"match":{"lpi.paoStartNumber":{"query":"13"}}},"boost":2}},{"constant_score":{"filter":{"match":{"lpi.paoStartSuffix":{"query":"h11"}}},"boost":2}},{"constant_score":{"filter":{"match":{"lpi.paoEndNumber":{"query":"12"}}},"boost":2}},{"constant_score":{"filter":{"match":{"lpi.paoEndSuffix":{"query":"h14"}}},"boost":2}},{"constant_score":{"filter":{"match":{"lpi.paoStartNumber":{"query":"12"}}},"boost":0.1}},{"constant_score":{"filter":{"match":{"paf.buildingNumber":{"query":"12"}}},"boost":0.1}},{"constant_score":{"filter":{"match":{"paf.buildingNumber":{"query":"13"}}},"boost":0.1}},{"constant_score":{"filter":{"match":{"nisra.paoStartNumber":{"query":"12"}}},"boost":0.1}},{"constant_score":{"filter":{"match":{"nisra.paoStartNumber":{"query":"13"}}},"boost":0.1}}]}},{"dis_max":{"tie_breaker":0.5,"queries":[{"constant_score":{"filter":{"match":{"lpi.saoStartNumber":{"query":"15"}}},"boost":1}},{"constant_score":{"filter":{"match":{"lpi.saoStartSuffix":{"query":"h16"}}},"boost":1}},{"constant_score":{"filter":{"match":{"lpi.saoEndNumber":{"query":"17"}}},"boost":1}},{"constant_score":{"filter":{"match":{"lpi.saoEndSuffix":{"query":"h18"}}},"boost":1}},{"constant_score":{"filter":{"match":{"lpi.saoStartNumber":{"query":"17"}}},"boost":0.1}},{"constant_score":{"filter":{"match":{"token.saoStartSuffix":{"query":"h16"}}},"boost":1}},{"constant_score":{"filter":{"match":{"lpi.saoEndSuffix":{"query":"h18"}}},"boost":1}}]}}],"minimum_should_match":"-40%"}},{"bool":{"must":[{"dis_max":{"tie_breaker":0,"queries":[{"match":{"tokens.addressAll":{"query":"h2 h3 h4 h5 6 h7 h20 h8 h10","analyzer":"welsh_split_synonyms_analyzer","boost":8,"minimum_should_match":"-40%"}}},{"match":{"lpi.nagAll":{"query":"h2 h3 h4 h5 6 h7 h20 h8 h10","analyzer":"welsh_split_synonyms_analyzer","boost":1,"minimum_should_match":"-40%"}}},{"match":{"nisra.nisraAll":{"query":"h2 h3 h4 h5 6 h7 h20 h8 h10","analyzer":"welsh_split_synonyms_analyzer","boost":1,"minimum_should_match":"-40%"}}},{"match":{"paf.pafAll":{"query":"h2 h3 h4 h5 6 h7 h20 h8 h10","analyzer":"welsh_split_synonyms_analyzer","boost":1,"minimum_should_match":"-40%"}}}]}}],"should":[{"dis_max":{"tie_breaker":0,"queries":[{"match":{"tokens.addressAll.bigram":{"query":"h2 h3 h4 h5 6 h7 h20 h8 h10","boost":2,"fuzziness":"0"}}},{"match":{"lpi.nagAll.bigram":{"query":"h2 h3 h4 h5 6 h7 h20 h8 h10","boost":0.4,"fuzziness":"0"}}},{"match":{"nisra.nisraAll.bigram":{"query":"h2 h3 h4 h5 6 h7 h20 h8 h10","boost":0.4,"fuzziness":"0"}}},{"match":{"paf.pafAll.bigram":{"query":"h2 h3 h4 h5 6 h7 h20 h8 h10","boost":0.4,"fuzziness":"0"}}}]}}],"boost":0.5}}]}},"from":0,"size":1,"sort":[{"_score":{"order":"desc"}},{"uprn":{"order":"asc"}}],"track_scores":true}
         """.stripMargin)

      // When
      val args = AddressArgs(
        input = "",
        tokens = tokens,
        filters = "",
        region = None,
        limit = 1,
        verbose = false,
      )
      val result = Json.parse(SearchBodyBuilderFn(repository.makeQuery(args)).string())

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
      val args = BulkArgs(
        requestsData = inputs,
        limit = 1,
        matchThreshold = 5F,
      )
      val results = repository.runBulkQuery(args).await
      val addresses = results.collect {
        case Right(address) => address
      }.flatten

      // Then
      results.length shouldBe 2
      addresses.length shouldBe 2

      addresses.head.uprn shouldBe hybridFirstUprn.toString
      addresses(1).uprn shouldBe hybridSecondaryUprn.toString
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
      val args = BulkArgs(
        requestsData = inputs,
        limit = 1,
        matchThreshold = 5F,
      )
      val results = repository.runBulkQuery(args).await
      val addresses = results.collect {
        case Right(address) => address
      }.flatten

      // Then
      results.length shouldBe 2
      addresses.length shouldBe 2

      addresses.head.uprn shouldBe HybridAddress.empty.uprn
      addresses(1).uprn shouldBe HybridAddress.empty.uprn
    }

    "return prefix filter for 'R' when passed filter 'residential' " in {
      // Given
      val repository = new AddressIndexRepository(config, elasticClientProvider)

      val tokens: Map[String, String] = Map.empty

      val filters: String = "residential"

      val expected = Json.parse(
        s"""
          {

            "query":{
              "bool":{
                "must":[{
                  "dis_max":{
                  "tie_breaker":0,
                  "queries":[{
                    "match":{
                      "tokens.addressAll":{
                        "query":"",
                        "analyzer":"welsh_split_synonyms_analyzer",
                        "boost":${queryParams.fallback.fallbackAuxBoost},
                        "minimum_should_match":"${queryParams.fallback.fallbackMinimumShouldMatch}"
                      }
                    }
                  },{
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
                      "nisra.nisraAll":{
                        "query":"",
                        "analyzer":"welsh_split_synonyms_analyzer",
                        "boost":${queryParams.fallback.fallbackPafBoost},
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
                      "tokens.addressAll.bigram":{
                        "query":"",
                          "boost":${queryParams.fallback.fallbackAuxBigramBoost},
                          "fuzziness":"${queryParams.fallback.bigramFuzziness}"
                        }
                      }
                    },{
                    "match":{
                      "lpi.nagAll.bigram":{
                        "query":"",
                          "boost":${queryParams.fallback.fallbackLpiBigramBoost},
                          "fuzziness":"${queryParams.fallback.bigramFuzziness}"
                        }
                      }
                    },{
                      "match":{
                        "nisra.nisraAll.bigram":{
                          "query":"",
                           "boost":${queryParams.fallback.fallbackPafBigramBoost},
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
                  "filter": [{"bool":{"should":[{"prefix":{"classificationCode":{"value":"RD"}}},{"prefix":{"classificationCode":{"value":"RH"}}},{"prefix":{"classificationCode":{"value":"RI"}}}],"minimum_should_match":"1"}}],
                  "boost":0.5
                }
              },
              "from": 0,
              "size": 1,
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
      val args = AddressArgs(
        input = "",
        tokens = tokens,
        filters = filters,
        region = None,
        limit = 1,
        verbose = false,
      )
      val result = Json.parse(SearchBodyBuilderFn(repository.makeQuery(args)).string())

      // Then
      result shouldBe expected
    }

    "return prefix filter for 'C' when passed filter 'commercial' " in {
      // Given
      val repository = new AddressIndexRepository(config, elasticClientProvider)

      val tokens: Map[String, String] = Map.empty

      val filters: String = "commercial"

      val expected = Json.parse(
        s"""
          {

            "query":{
              "bool":{
                "must":[{
                  "dis_max":{
                  "tie_breaker":0,
                  "queries":[{
                    "match":{
                      "tokens.addressAll":{
                        "query":"",
                        "analyzer":"welsh_split_synonyms_analyzer",
                        "boost":${queryParams.fallback.fallbackAuxBoost},
                        "minimum_should_match":"${queryParams.fallback.fallbackMinimumShouldMatch}"
                      }
                    }
                  },{
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
                      "nisra.nisraAll":{
                        "query":"",
                        "analyzer":"welsh_split_synonyms_analyzer",
                        "boost":${queryParams.fallback.fallbackPafBoost},
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
                      "tokens.addressAll.bigram":{
                        "query":"",
                          "boost":${queryParams.fallback.fallbackAuxBigramBoost},
                          "fuzziness":"${queryParams.fallback.bigramFuzziness}"
                        }
                      }
                    },{
                    "match":{
                      "lpi.nagAll.bigram":{
                        "query":"",
                          "boost":${queryParams.fallback.fallbackLpiBigramBoost},
                          "fuzziness":"${queryParams.fallback.bigramFuzziness}"
                        }
                      }
                    },{
                      "match":{
                        "nisra.nisraAll.bigram":{
                          "query":"",
                           "boost":${queryParams.fallback.fallbackPafBigramBoost},
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
                  "filter": [
                    {
                      "prefix": {
                        "classificationCode": {
                          "value": "C"
                        }
                      }
                    }
                  ],
                  "boost":0.5
                }
              },
              "from": 0,
              "size": 1,
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
      val args = AddressArgs(
        input = "",
        tokens = tokens,
        filters = filters,
        region = None,
        limit = 1,
        verbose = false,
      )
      val result = Json.parse(SearchBodyBuilderFn(repository.makeQuery(args)).string())

      // Then
      result shouldBe expected
    }

    "return term filter for 'RD06' when passed filter 'RD06' " in {
      // Given
      val repository = new AddressIndexRepository(config, elasticClientProvider)

      val tokens: Map[String, String] = Map.empty

      val filters: String = "RD06"

      val expected = Json.parse(
        s"""
          {

            "query":{
              "bool":{
                "must":[{
                  "dis_max":{
                  "tie_breaker":0,
                  "queries":[{
                    "match":{
                      "tokens.addressAll":{
                        "query":"",
                        "analyzer":"welsh_split_synonyms_analyzer",
                        "boost":${queryParams.fallback.fallbackAuxBoost},
                        "minimum_should_match":"${queryParams.fallback.fallbackMinimumShouldMatch}"
                      }
                    }
                  },{
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
                      "nisra.nisraAll":{
                        "query":"",
                        "analyzer":"welsh_split_synonyms_analyzer",
                        "boost":${queryParams.fallback.fallbackPafBoost},
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
                      "tokens.addressAll.bigram":{
                        "query":"",
                          "boost":${queryParams.fallback.fallbackAuxBigramBoost},
                          "fuzziness":"${queryParams.fallback.bigramFuzziness}"
                        }
                      }
                    },{
                    "match":{
                      "lpi.nagAll.bigram":{
                        "query":"",
                          "boost":${queryParams.fallback.fallbackLpiBigramBoost},
                          "fuzziness":"${queryParams.fallback.bigramFuzziness}"
                        }
                      }
                    },{
                      "match":{
                        "nisra.nisraAll.bigram":{
                          "query":"",
                           "boost":${queryParams.fallback.fallbackPafBigramBoost},
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
                  "filter": [
                    {
                      "terms": {
                        "classificationCode": ["RD06"]
                      }
                    }
                  ],
                  "boost":0.5
                }
              },
              "from": 0,
              "size": 1,
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
      val args = AddressArgs(
        input = "",
        tokens = tokens,
        filters = filters,
        region = None,
        limit = 1,
        verbose = false,
      )
      val result = Json.parse(SearchBodyBuilderFn(repository.makeQuery(args)).string())

      // Then
      result shouldBe expected
    }

    "return terms filter for 'RD06,RD' when passed filter 'RD06,RD' " in {
      // Given
      val repository = new AddressIndexRepository(config, elasticClientProvider)

      val tokens: Map[String, String] = Map.empty

      val filters: String = "RD06,RD"

      val expected = Json.parse(
        s"""
          {

            "query":{
              "bool":{
                "must":[{
                  "dis_max":{
                  "tie_breaker":0,
                  "queries":[{
                    "match":{
                      "tokens.addressAll":{
                        "query":"",
                        "analyzer":"welsh_split_synonyms_analyzer",
                        "boost":${queryParams.fallback.fallbackAuxBoost},
                        "minimum_should_match":"${queryParams.fallback.fallbackMinimumShouldMatch}"
                      }
                    }
                  },{
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
                      "nisra.nisraAll":{
                        "query":"",
                        "analyzer":"welsh_split_synonyms_analyzer",
                        "boost":${queryParams.fallback.fallbackPafBoost},
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
                      "tokens.addressAll.bigram":{
                        "query":"",
                          "boost":${queryParams.fallback.fallbackAuxBigramBoost},
                          "fuzziness":"${queryParams.fallback.bigramFuzziness}"
                        }
                      }
                    },{
                    "match":{
                      "lpi.nagAll.bigram":{
                        "query":"",
                          "boost":${queryParams.fallback.fallbackLpiBigramBoost},
                          "fuzziness":"${queryParams.fallback.bigramFuzziness}"
                        }
                      }
                    },{
                      "match":{
                        "nisra.nisraAll.bigram":{
                          "query":"",
                           "boost":${queryParams.fallback.fallbackPafBigramBoost},
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
                  "filter": [
                    {
                      "terms": {
                        "classificationCode": ["RD06","RD"]
                      }
                    }
                  ],
                  "boost":0.5
                }
              },
              "from": 0,
              "size": 1,
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
      val args = AddressArgs(
        input = "",
        tokens = tokens,
        filters = filters,
        region = None,
        limit = 1,
        verbose = false,
      )
      val result = Json.parse(SearchBodyBuilderFn(repository.makeQuery(args)).string())

      // Then
      result shouldBe expected
    }

    "return error for terms filter for 'RD*,RD02' when passed filter 'RD*,RD02' " in {
      // Given
      val repository = new AddressIndexRepository(config, elasticClientProvider)

      val tokens: Map[String, String] = Map.empty

      val filters: String = "RD*,RD02"

      val expected = Json.parse(
        s"""
          {

            "query":{
              "bool":{
                "must":[{
                  "dis_max":{
                  "tie_breaker":0,
                  "queries":[{
                    "match":{
                      "tokens.addressAll":{
                        "query":"",
                        "analyzer":"welsh_split_synonyms_analyzer",
                        "boost":${queryParams.fallback.fallbackAuxBoost},
                        "minimum_should_match":"${queryParams.fallback.fallbackMinimumShouldMatch}"
                      }
                    }
                  },{
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
                      "nisra.nisraAll":{
                        "query":"",
                        "analyzer":"welsh_split_synonyms_analyzer",
                        "boost":${queryParams.fallback.fallbackPafBoost},
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
                      "tokens.addressAll.bigram":{
                        "query":"",
                          "boost":${queryParams.fallback.fallbackAuxBigramBoost},
                          "fuzziness":"${queryParams.fallback.bigramFuzziness}"
                        }
                      }
                    },{
                    "match":{
                      "lpi.nagAll.bigram":{
                        "query":"",
                          "boost":${queryParams.fallback.fallbackLpiBigramBoost},
                          "fuzziness":"${queryParams.fallback.bigramFuzziness}"
                        }
                      }
                    },{
                      "match":{
                        "nisra.nisraAll.bigram":{
                          "query":"",
                           "boost":${queryParams.fallback.fallbackPafBigramBoost},
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
                  "filter": [
                    {
                      "terms": {
                        "classificationCode": ["RD*","RD02"]
                      }
                    }
                  ],
                  "boost":0.5
                }
              },
              "from": 0,
              "size": 1,
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
      val args = AddressArgs(
        input = "",
        tokens = tokens,
        filters = filters,
        region = None,
        limit = 1,
        verbose = false,
      )
      val result = Json.parse(SearchBodyBuilderFn(repository.makeQuery(args)).string())

      // Then
      result shouldBe expected
    }

    "generate valid query for search via partial endpoint - term with house number" in {
      // Given
      val repository = new AddressIndexRepository(config, elasticClientProvider)

      val expected = Json.parse(
        """
           {"query":{"function_score":{"query":{"bool":{"must":[{"multi_match":{"query":"7 Gate Re","fields":["mixedPartial"],"type":"phrase","slop":25}}],"should":[{"dis_max":{"queries":[{"match":{"lpi.paoStartNumber":{"query":"7","boost":2,"fuzzy_transpositions":false,"max_expansions":10,"prefix_length":"1"}}},{"match":{"lpi.saoStartNumber":{"query":"7","boost":1,"fuzzy_transpositions":false,"max_expansions":10,"prefix_length":"1"}}},{"match":{"nisra.paoStartNumber":{"query":"7","boost":2,"fuzzy_transpositions":false,"max_expansions":10,"prefix_length":"1"}}},{"match":{"nisra.saoStartNumber":{"query":"7","boost":1,"fuzzy_transpositions":false,"max_expansions":10,"prefix_length":"1"}}}]}},{"dis_max":{"queries":[{"prefix":{"lpi.mixedNagStart":{"value":"7 Gate Re","boost":1.25}}},{"prefix":{"lpi.mixedWelshNagStart":{"value":"7 Gate Re","boost":1.25}}},{"prefix":{"paf.mixedPafStart":{"value":"7 Gate Re","boost":1.25}}},{"prefix":{"paf.mixedWelshPafStart":{"value":"7 Gate Re","boost":1.25}}},{"prefix":{"nisra.mixedNisraStart":{"value":"7 Gate Re","boost":1.25}}}]}}]}},"min_score":1,"boost_mode":"replace","functions":[{"script_score":{"script":{"source":"Math.round(_score/1.8)"}}}]}},"from":0,"size":1,"sort":[{"_score":{"order":"desc"}},{"postcodeStreetTown":{"order":"asc"}},{"lpi.paoStartNumber":{"order":"asc"}},{"lpi.paoStartSuffix.keyword":{"order":"asc"}},{"lpi.secondarySort":{"order":"asc"}},{"nisra.paoStartNumber":{"order":"asc"}},{"nisra.secondarySort":{"order":"asc"}},{"uprn":{"order":"asc"}}],"highlight":{"number_of_fragments":0,"fields":{"mixedPartial":{}}}}
         """
      )

      // When
      val args = PartialArgs(
        input = partialInput,
        filters = partialFilterNone,
        limit = 1
      )
      val result = Json.parse(SearchBodyBuilderFn(repository.makePartialSearch(args, fallback = false)).string())

      // Then
      result shouldBe expected
    }

    "generate valid fallback query for search via partial endpoint - term with house number" in {
      // Given
      val repository = new AddressIndexRepository(config, elasticClientProvider)

      val expected = Json.parse(
        """
           {"query":{"function_score":{"query":{"bool":{"must":[{"multi_match":{"query":"7 Gate Ret","fields":["mixedPartial"],"type":"best_fields"}}],"should":[{"dis_max":{"queries":[{"match":{"lpi.paoStartNumber":{"query":"7","boost":2,"fuzzy_transpositions":false,"max_expansions":10,"prefix_length":"1"}}},{"match":{"lpi.saoStartNumber":{"query":"7","boost":1,"fuzzy_transpositions":false,"max_expansions":10,"prefix_length":"1"}}},{"match":{"nisra.paoStartNumber":{"query":"7","boost":2,"fuzzy_transpositions":false,"max_expansions":10,"prefix_length":"1"}}},{"match":{"nisra.saoStartNumber":{"query":"7","boost":1,"fuzzy_transpositions":false,"max_expansions":10,"prefix_length":"1"}}}]}},{"dis_max":{"queries":[{"prefix":{"lpi.mixedNagStart":{"value":"7 Gate Ret","boost":1.25}}},{"prefix":{"lpi.mixedWelshNagStart":{"value":"7 Gate Ret","boost":1.25}}},{"prefix":{"paf.mixedPafStart":{"value":"7 Gate Ret","boost":1.25}}},{"prefix":{"paf.mixedWelshPafStart":{"value":"7 Gate Ret","boost":1.25}}},{"prefix":{"nisra.mixedNisraStart":{"value":"7 Gate Ret","boost":1.25}}}]}}]}},"min_score":1,"boost_mode":"replace","functions":[{"script_score":{"script":{"source":"Math.round(_score/1.8)"}}}]}},"from":0,"size":1,"sort":[{"_score":{"order":"desc"}},{"postcodeStreetTown":{"order":"asc"}},{"lpi.paoStartNumber":{"order":"asc"}},{"lpi.paoStartSuffix.keyword":{"order":"asc"}},{"lpi.secondarySort":{"order":"asc"}},{"nisra.paoStartNumber":{"order":"asc"}},{"nisra.secondarySort":{"order":"asc"}},{"uprn":{"order":"asc"}}],"highlight":{"number_of_fragments":0,"fields":{"mixedPartial":{}}}}
           """
      )

      // When
      val args = PartialArgs(
        input = partialInputFallback,
        fallback = true,
        filters = partialFilterNone,
        limit = 1
      )
      val result = Json.parse(SearchBodyBuilderFn(repository.makePartialSearch(args, fallback = true)).string())

      // Then
      result shouldBe expected
    }

    "generate valid query for search via partial endpoint - term without house number" in {
      // Given
      val repository = new AddressIndexRepository(config, elasticClientProvider)

      val expected = Json.parse(
        """
           {"query":{"function_score":{"query":{"bool":{"must":[{"multi_match":{"query":"Gate Re","fields":["mixedPartial"],"type":"phrase","slop":25}}],"should":[{"dis_max":{"queries":[{"prefix":{"lpi.mixedNagStart":{"value":"Gate Re","boost":1.25}}},{"prefix":{"lpi.mixedWelshNagStart":{"value":"Gate Re","boost":1.25}}},{"prefix":{"paf.mixedPafStart":{"value":"Gate Re","boost":1.25}}},{"prefix":{"paf.mixedWelshPafStart":{"value":"Gate Re","boost":1.25}}},{"prefix":{"nisra.mixedNisraStart":{"value":"Gate Re","boost":1.25}}}]}}]}},"min_score":1,"boost_mode":"replace","functions":[{"script_score":{"script":{"source":"Math.round(_score/1.8)"}}}]}},"from":0,"size":1,"sort":[{"_score":{"order":"desc"}},{"postcodeStreetTown":{"order":"asc"}},{"lpi.paoStartNumber":{"order":"asc"}},{"lpi.paoStartSuffix.keyword":{"order":"asc"}},{"lpi.secondarySort":{"order":"asc"}},{"nisra.paoStartNumber":{"order":"asc"}},{"nisra.secondarySort":{"order":"asc"}},{"uprn":{"order":"asc"}}],"highlight":{"number_of_fragments":0,"fields":{"mixedPartial":{}}}}
        """
      )

      // When
      val args = PartialArgs(
        input = partialInputWithout,
        filters = partialFilterNone,
        limit = 1
      )
      val result = Json.parse(SearchBodyBuilderFn(repository.makePartialSearch(args, fallback = false)).string())

      // Then
      result shouldBe expected
    }

    "generate valid fallback query for search via partial endpoint - term without house number" in {
      // Given
      val repository = new AddressIndexRepository(config, elasticClientProvider)

      val expected = Json.parse(
        """
           {"query":{"function_score":{"query":{"bool":{"must":[{"multi_match":{"query":"Gate Ret","fields":["mixedPartial"],"type":"best_fields"}}],"should":[{"dis_max":{"queries":[{"prefix":{"lpi.mixedNagStart":{"value":"Gate Ret","boost":1.25}}},{"prefix":{"lpi.mixedWelshNagStart":{"value":"Gate Ret","boost":1.25}}},{"prefix":{"paf.mixedPafStart":{"value":"Gate Ret","boost":1.25}}},{"prefix":{"paf.mixedWelshPafStart":{"value":"Gate Ret","boost":1.25}}},{"prefix":{"nisra.mixedNisraStart":{"value":"Gate Ret","boost":1.25}}}]}}]}},"min_score":1,"boost_mode":"replace","functions":[{"script_score":{"script":{"source":"Math.round(_score/1.8)"}}}]}},"from":0,"size":1,"sort":[{"_score":{"order":"desc"}},{"postcodeStreetTown":{"order":"asc"}},{"lpi.paoStartNumber":{"order":"asc"}},{"lpi.paoStartSuffix.keyword":{"order":"asc"}},{"lpi.secondarySort":{"order":"asc"}},{"nisra.paoStartNumber":{"order":"asc"}},{"nisra.secondarySort":{"order":"asc"}},{"uprn":{"order":"asc"}}],"highlight":{"number_of_fragments":0,"fields":{"mixedPartial":{}}}}
        """
      )

      // When
      val args = PartialArgs(
        input = partialInputWithoutFallback,
        fallback = true,
        filters = partialFilterNone,
        limit = 1
      )
      val result = Json.parse(SearchBodyBuilderFn(repository.makePartialSearch(args, fallback = true)).string())

      // Then
      result shouldBe expected
    }


    "generate valid query for search via partial endpoint - term with house number and exact filter" in {
      // Given
      val repository = new AddressIndexRepository(config, elasticClientProvider)

      val expected = Json.parse(
        """
          {"query":{"function_score":{"query":{"bool":{"must":[{"multi_match":{"query":"7 Gate Re","fields":["mixedPartial"],"type":"phrase","slop":25}}],"should":[{"dis_max":{"queries":[{"match":{"lpi.paoStartNumber":{"query":"7","boost":2,"fuzzy_transpositions":false,"max_expansions":10,"prefix_length":"1"}}},{"match":{"lpi.saoStartNumber":{"query":"7","boost":1,"fuzzy_transpositions":false,"max_expansions":10,"prefix_length":"1"}}},{"match":{"nisra.paoStartNumber":{"query":"7","boost":2,"fuzzy_transpositions":false,"max_expansions":10,"prefix_length":"1"}}},{"match":{"nisra.saoStartNumber":{"query":"7","boost":1,"fuzzy_transpositions":false,"max_expansions":10,"prefix_length":"1"}}}]}},{"dis_max":{"queries":[{"prefix":{"lpi.mixedNagStart":{"value":"7 Gate Re","boost":1.25}}},{"prefix":{"lpi.mixedWelshNagStart":{"value":"7 Gate Re","boost":1.25}}},{"prefix":{"paf.mixedPafStart":{"value":"7 Gate Re","boost":1.25}}},{"prefix":{"paf.mixedWelshPafStart":{"value":"7 Gate Re","boost":1.25}}},{"prefix":{"nisra.mixedNisraStart":{"value":"7 Gate Re","boost":1.25}}}]}}],"filter":[{"terms":{"classificationCode":["RD"]}}]}},"min_score":1,"boost_mode":"replace","functions":[{"script_score":{"script":{"source":"Math.round(_score/1.8)"}}}]}},"from":0,"size":1,"sort":[{"_score":{"order":"desc"}},{"postcodeStreetTown":{"order":"asc"}},{"lpi.paoStartNumber":{"order":"asc"}},{"lpi.paoStartSuffix.keyword":{"order":"asc"}},{"lpi.secondarySort":{"order":"asc"}},{"nisra.paoStartNumber":{"order":"asc"}},{"nisra.secondarySort":{"order":"asc"}},{"uprn":{"order":"asc"}}],"highlight":{"number_of_fragments":0,"fields":{"mixedPartial":{}}}}
        """
      )

      // When
      val args = PartialArgs(
        input = partialInput,
        filters = partialFilterCode,
        limit = 1

      )
      val result = Json.parse(SearchBodyBuilderFn(repository.makePartialSearch(args, fallback = false)).string())

      // Then
      result shouldBe expected
    }


    "generate valid fallback query for search via partial endpoint - term with house number and exact filter" in {
      // Given
      val repository = new AddressIndexRepository(config, elasticClientProvider)

      val expected = Json.parse(
        """
          {"query":{"function_score":{"query":{"bool":{"must":[{"multi_match":{"query":"7 Gate Ret","fields":["mixedPartial"],"type":"best_fields"}}],"should":[{"dis_max":{"queries":[{"match":{"lpi.paoStartNumber":{"query":"7","boost":2,"fuzzy_transpositions":false,"max_expansions":10,"prefix_length":"1"}}},{"match":{"lpi.saoStartNumber":{"query":"7","boost":1,"fuzzy_transpositions":false,"max_expansions":10,"prefix_length":"1"}}},{"match":{"nisra.paoStartNumber":{"query":"7","boost":2,"fuzzy_transpositions":false,"max_expansions":10,"prefix_length":"1"}}},{"match":{"nisra.saoStartNumber":{"query":"7","boost":1,"fuzzy_transpositions":false,"max_expansions":10,"prefix_length":"1"}}}]}},{"dis_max":{"queries":[{"prefix":{"lpi.mixedNagStart":{"value":"7 Gate Ret","boost":1.25}}},{"prefix":{"lpi.mixedWelshNagStart":{"value":"7 Gate Ret","boost":1.25}}},{"prefix":{"paf.mixedPafStart":{"value":"7 Gate Ret","boost":1.25}}},{"prefix":{"paf.mixedWelshPafStart":{"value":"7 Gate Ret","boost":1.25}}},{"prefix":{"nisra.mixedNisraStart":{"value":"7 Gate Ret","boost":1.25}}}]}}],"filter":[{"terms":{"classificationCode":["RD"]}}]}},"min_score":1,"boost_mode":"replace","functions":[{"script_score":{"script":{"source":"Math.round(_score/1.8)"}}}]}},"from":0,"size":1,"sort":[{"_score":{"order":"desc"}},{"postcodeStreetTown":{"order":"asc"}},{"lpi.paoStartNumber":{"order":"asc"}},{"lpi.paoStartSuffix.keyword":{"order":"asc"}},{"lpi.secondarySort":{"order":"asc"}},{"nisra.paoStartNumber":{"order":"asc"}},{"nisra.secondarySort":{"order":"asc"}},{"uprn":{"order":"asc"}}],"highlight":{"number_of_fragments":0,"fields":{"mixedPartial":{}}}}
        """
      )

      // When
      val args = PartialArgs(
        input = partialInputFallback,
        fallback = true,
        filters = partialFilterCode,
        limit = 1
      )
      val result = Json.parse(SearchBodyBuilderFn(repository.makePartialSearch(args, fallback = true)).string())

      // Then
      result shouldBe expected
    }

    "generate valid query for search via partial endpoint - term with house number and prefix filter" in {
      // Given
      val repository = new AddressIndexRepository(config, elasticClientProvider)

      val expected = Json.parse(
        """
           {"query":{"function_score":{"query":{"bool":{"must":[{"multi_match":{"query":"7 Gate Re","fields":["mixedPartial"],"type":"phrase","slop":25}}],"should":[{"dis_max":{"queries":[{"match":{"lpi.paoStartNumber":{"query":"7","boost":2,"fuzzy_transpositions":false,"max_expansions":10,"prefix_length":"1"}}},{"match":{"lpi.saoStartNumber":{"query":"7","boost":1,"fuzzy_transpositions":false,"max_expansions":10,"prefix_length":"1"}}},{"match":{"nisra.paoStartNumber":{"query":"7","boost":2,"fuzzy_transpositions":false,"max_expansions":10,"prefix_length":"1"}}},{"match":{"nisra.saoStartNumber":{"query":"7","boost":1,"fuzzy_transpositions":false,"max_expansions":10,"prefix_length":"1"}}}]}},{"dis_max":{"queries":[{"prefix":{"lpi.mixedNagStart":{"value":"7 Gate Re","boost":1.25}}},{"prefix":{"lpi.mixedWelshNagStart":{"value":"7 Gate Re","boost":1.25}}},{"prefix":{"paf.mixedPafStart":{"value":"7 Gate Re","boost":1.25}}},{"prefix":{"paf.mixedWelshPafStart":{"value":"7 Gate Re","boost":1.25}}},{"prefix":{"nisra.mixedNisraStart":{"value":"7 Gate Re","boost":1.25}}}]}}],"filter":[{"bool":{"should":[{"prefix":{"classificationCode":{"value":"RD"}}},{"prefix":{"classificationCode":{"value":"RH"}}},{"prefix":{"classificationCode":{"value":"RI"}}}],"minimum_should_match":"1"}}]}},"min_score":1,"boost_mode":"replace","functions":[{"script_score":{"script":{"source":"Math.round(_score/1.8)"}}}]}},"from":0,"size":1,"sort":[{"_score":{"order":"desc"}},{"postcodeStreetTown":{"order":"asc"}},{"lpi.paoStartNumber":{"order":"asc"}},{"lpi.paoStartSuffix.keyword":{"order":"asc"}},{"lpi.secondarySort":{"order":"asc"}},{"nisra.paoStartNumber":{"order":"asc"}},{"nisra.secondarySort":{"order":"asc"}},{"uprn":{"order":"asc"}}],"highlight":{"number_of_fragments":0,"fields":{"mixedPartial":{}}}}
          """
      )

      // When
      val args = PartialArgs(
        input = partialInput,
        filters = partialFilterPrefix,
        limit = 1
      )
      val result = Json.parse(SearchBodyBuilderFn(repository.makePartialSearch(args, fallback = false)).string())

      // Then
      result shouldBe expected
    }

    "generate valid fallback query for search via partial endpoint - term with house number and prefix filter" in {
      // Given
      val repository = new AddressIndexRepository(config, elasticClientProvider)

      val expected = Json.parse(
        """
          {"query":{"function_score":{"query":{"bool":{"must":[{"multi_match":{"query":"7 Gate Ret","fields":["mixedPartial"],"type":"best_fields"}}],"should":[{"dis_max":{"queries":[{"match":{"lpi.paoStartNumber":{"query":"7","boost":2,"fuzzy_transpositions":false,"max_expansions":10,"prefix_length":"1"}}},{"match":{"lpi.saoStartNumber":{"query":"7","boost":1,"fuzzy_transpositions":false,"max_expansions":10,"prefix_length":"1"}}},{"match":{"nisra.paoStartNumber":{"query":"7","boost":2,"fuzzy_transpositions":false,"max_expansions":10,"prefix_length":"1"}}},{"match":{"nisra.saoStartNumber":{"query":"7","boost":1,"fuzzy_transpositions":false,"max_expansions":10,"prefix_length":"1"}}}]}},{"dis_max":{"queries":[{"prefix":{"lpi.mixedNagStart":{"value":"7 Gate Ret","boost":1.25}}},{"prefix":{"lpi.mixedWelshNagStart":{"value":"7 Gate Ret","boost":1.25}}},{"prefix":{"paf.mixedPafStart":{"value":"7 Gate Ret","boost":1.25}}},{"prefix":{"paf.mixedWelshPafStart":{"value":"7 Gate Ret","boost":1.25}}},{"prefix":{"nisra.mixedNisraStart":{"value":"7 Gate Ret","boost":1.25}}}]}}],"filter":[{"bool":{"should":[{"prefix":{"classificationCode":{"value":"RD"}}},{"prefix":{"classificationCode":{"value":"RH"}}},{"prefix":{"classificationCode":{"value":"RI"}}}],"minimum_should_match":"1"}}]}},"min_score":1,"boost_mode":"replace","functions":[{"script_score":{"script":{"source":"Math.round(_score/1.8)"}}}]}},"from":0,"size":1,"sort":[{"_score":{"order":"desc"}},{"postcodeStreetTown":{"order":"asc"}},{"lpi.paoStartNumber":{"order":"asc"}},{"lpi.paoStartSuffix.keyword":{"order":"asc"}},{"lpi.secondarySort":{"order":"asc"}},{"nisra.paoStartNumber":{"order":"asc"}},{"nisra.secondarySort":{"order":"asc"}},{"uprn":{"order":"asc"}}],"highlight":{"number_of_fragments":0,"fields":{"mixedPartial":{}}}}
        """
      )

      // When
      val args = PartialArgs(
        input = partialInputFallback,
        fallback = true,
        filters = partialFilterPrefix,
        limit = 1
      )
      val result = Json.parse(SearchBodyBuilderFn(repository.makePartialSearch(args, fallback = true)).string())

      // Then
      result shouldBe expected
    }

    "generate valid query for search via partial endpoint - term without house number and exact filter" in {
      // Given
      val repository = new AddressIndexRepository(config, elasticClientProvider)

      val expected = Json.parse(
        """
           {"query":{"function_score":{"query":{"bool":{"must":[{"multi_match":{"query":"Gate Re","fields":["mixedPartial"],"type":"phrase","slop":25}}],"should":[{"dis_max":{"queries":[{"prefix":{"lpi.mixedNagStart":{"value":"Gate Re","boost":1.25}}},{"prefix":{"lpi.mixedWelshNagStart":{"value":"Gate Re","boost":1.25}}},{"prefix":{"paf.mixedPafStart":{"value":"Gate Re","boost":1.25}}},{"prefix":{"paf.mixedWelshPafStart":{"value":"Gate Re","boost":1.25}}},{"prefix":{"nisra.mixedNisraStart":{"value":"Gate Re","boost":1.25}}}]}}],"filter":[{"terms":{"classificationCode":["RD"]}}]}},"min_score":1,"boost_mode":"replace","functions":[{"script_score":{"script":{"source":"Math.round(_score/1.8)"}}}]}},"from":0,"size":1,"sort":[{"_score":{"order":"desc"}},{"postcodeStreetTown":{"order":"asc"}},{"lpi.paoStartNumber":{"order":"asc"}},{"lpi.paoStartSuffix.keyword":{"order":"asc"}},{"lpi.secondarySort":{"order":"asc"}},{"nisra.paoStartNumber":{"order":"asc"}},{"nisra.secondarySort":{"order":"asc"}},{"uprn":{"order":"asc"}}],"highlight":{"number_of_fragments":0,"fields":{"mixedPartial":{}}}}
        """
      )

      // When
      val args = PartialArgs(
        input = partialInputWithout,
        filters = partialFilterCode,
        limit = 1
      )
      val result = Json.parse(SearchBodyBuilderFn(repository.makePartialSearch(args, fallback = false)).string())

      // Then
      result shouldBe expected
    }

    "generate valid fallback query for search via partial endpoint - term without house number and exact filter" in {
      // Given
      val repository = new AddressIndexRepository(config, elasticClientProvider)

      val expected = Json.parse(
        """
          {"query":{"function_score":{"query":{"bool":{"must":[{"multi_match":{"query":"Gate Ret","fields":["mixedPartial"],"type":"best_fields"}}],"should":[{"dis_max":{"queries":[{"prefix":{"lpi.mixedNagStart":{"value":"Gate Ret","boost":1.25}}},{"prefix":{"lpi.mixedWelshNagStart":{"value":"Gate Ret","boost":1.25}}},{"prefix":{"paf.mixedPafStart":{"value":"Gate Ret","boost":1.25}}},{"prefix":{"paf.mixedWelshPafStart":{"value":"Gate Ret","boost":1.25}}},{"prefix":{"nisra.mixedNisraStart":{"value":"Gate Ret","boost":1.25}}}]}}],"filter":[{"terms":{"classificationCode":["RD"]}}]}},"min_score":1,"boost_mode":"replace","functions":[{"script_score":{"script":{"source":"Math.round(_score/1.8)"}}}]}},"from":0,"size":1,"sort":[{"_score":{"order":"desc"}},{"postcodeStreetTown":{"order":"asc"}},{"lpi.paoStartNumber":{"order":"asc"}},{"lpi.paoStartSuffix.keyword":{"order":"asc"}},{"lpi.secondarySort":{"order":"asc"}},{"nisra.paoStartNumber":{"order":"asc"}},{"nisra.secondarySort":{"order":"asc"}},{"uprn":{"order":"asc"}}],"highlight":{"number_of_fragments":0,"fields":{"mixedPartial":{}}}}
         """
      )

      // When
      val args = PartialArgs(
        input = partialInputWithoutFallback,
        fallback = true,
        filters = partialFilterCode,
        limit = 1
      )
      val result = Json.parse(SearchBodyBuilderFn(repository.makePartialSearch(args, fallback = true)).string())

      // Then
      result shouldBe expected
    }

    "generate valid query for search via partial endpoint - term without house number and prefix filter" in {
      // Given
      val repository = new AddressIndexRepository(config, elasticClientProvider)

      val expected = Json.parse(
        """
           {"query":{"function_score":{"query":{"bool":{"must":[{"multi_match":{"query":"Gate Re","fields":["mixedPartial"],"type":"phrase","slop":25}}],"should":[{"dis_max":{"queries":[{"prefix":{"lpi.mixedNagStart":{"value":"Gate Re","boost":1.25}}},{"prefix":{"lpi.mixedWelshNagStart":{"value":"Gate Re","boost":1.25}}},{"prefix":{"paf.mixedPafStart":{"value":"Gate Re","boost":1.25}}},{"prefix":{"paf.mixedWelshPafStart":{"value":"Gate Re","boost":1.25}}},{"prefix":{"nisra.mixedNisraStart":{"value":"Gate Re","boost":1.25}}}]}}],"filter":[{"bool":{"should":[{"prefix":{"classificationCode":{"value":"RD"}}},{"prefix":{"classificationCode":{"value":"RH"}}},{"prefix":{"classificationCode":{"value":"RI"}}}],"minimum_should_match":"1"}}]}},"min_score":1,"boost_mode":"replace","functions":[{"script_score":{"script":{"source":"Math.round(_score/1.8)"}}}]}},"from":0,"size":1,"sort":[{"_score":{"order":"desc"}},{"postcodeStreetTown":{"order":"asc"}},{"lpi.paoStartNumber":{"order":"asc"}},{"lpi.paoStartSuffix.keyword":{"order":"asc"}},{"lpi.secondarySort":{"order":"asc"}},{"nisra.paoStartNumber":{"order":"asc"}},{"nisra.secondarySort":{"order":"asc"}},{"uprn":{"order":"asc"}}],"highlight":{"number_of_fragments":0,"fields":{"mixedPartial":{}}}}
        """
      )

      // When
      val args = PartialArgs(
        input = partialInputWithout,
        filters = partialFilterPrefix,
        limit = 1,
      )
      val result = Json.parse(SearchBodyBuilderFn(repository.makePartialSearch(args, fallback = false)).string())

      // Then
      result shouldBe expected
    }

    "generate valid fallback query for search via partial endpoint - term without house number and prefix filter" in {
      // Given
      val repository = new AddressIndexRepository(config, elasticClientProvider)

      val expected = Json.parse(
        """
           {"query":{"function_score":{"query":{"bool":{"must":[{"multi_match":{"query":"Gate Ret","fields":["mixedPartial"],"type":"best_fields"}}],"should":[{"dis_max":{"queries":[{"prefix":{"lpi.mixedNagStart":{"value":"Gate Ret","boost":1.25}}},{"prefix":{"lpi.mixedWelshNagStart":{"value":"Gate Ret","boost":1.25}}},{"prefix":{"paf.mixedPafStart":{"value":"Gate Ret","boost":1.25}}},{"prefix":{"paf.mixedWelshPafStart":{"value":"Gate Ret","boost":1.25}}},{"prefix":{"nisra.mixedNisraStart":{"value":"Gate Ret","boost":1.25}}}]}}],"filter":[{"bool":{"should":[{"prefix":{"classificationCode":{"value":"RD"}}},{"prefix":{"classificationCode":{"value":"RH"}}},{"prefix":{"classificationCode":{"value":"RI"}}}],"minimum_should_match":"1"}}]}},"min_score":1,"boost_mode":"replace","functions":[{"script_score":{"script":{"source":"Math.round(_score/1.8)"}}}]}},"from":0,"size":1,"sort":[{"_score":{"order":"desc"}},{"postcodeStreetTown":{"order":"asc"}},{"lpi.paoStartNumber":{"order":"asc"}},{"lpi.paoStartSuffix.keyword":{"order":"asc"}},{"lpi.secondarySort":{"order":"asc"}},{"nisra.paoStartNumber":{"order":"asc"}},{"nisra.secondarySort":{"order":"asc"}},{"uprn":{"order":"asc"}}],"highlight":{"number_of_fragments":0,"fields":{"mixedPartial":{}}}}
         """
      )

      // When
      val args = PartialArgs(
        input = partialInputWithoutFallback,
        fallback = true,
        filters = partialFilterPrefix,
        limit = 1
      )
      val result = Json.parse(SearchBodyBuilderFn(repository.makePartialSearch(args, fallback = true)).string())

      // Then
      result shouldBe expected
    }


    "generate valid query for search by tokens, with a term filter" in {
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

      val filters: String = "RD06"

      val expected = Json.parse (
        s"""
          {"query":{"dis_max":{"tie_breaker":1,"queries":[{"bool":{"should":[{"dis_max":{"tie_breaker":0,"queries":[{"constant_score":{"filter":{"match":{"tokens.subBuildingName":{"query":"h4","minimum_should_match":"-45%"}}},"boost":1}},{"constant_score":{"filter":{"match":{"paf.subBuildingName":{"query":"h4","minimum_should_match":"-45%"}}},"boost":1.5}},{"constant_score":{"filter":{"match":{"nisra.subBuildingName":{"query":"h4","minimum_should_match":"-45%"}}},"boost":1.5}},{"constant_score":{"filter":{"match":{"lpi.saoText":{"query":"h4","minimum_should_match":"-45%"}}},"boost":1.5}},{"dis_max":{"tie_breaker":0.5,"queries":[{"constant_score":{"filter":{"match":{"tokens.subBuildingName":{"query":"15"}}},"boost":1}},{"constant_score":{"filter":{"match":{"lpi.saoStartNumber":{"query":"15"}}},"boost":1}},{"constant_score":{"filter":{"match":{"paf.subBuildingName":{"query":"15"}}},"boost":1}},{"constant_score":{"filter":{"match":{"nisra.subBuildingName":{"query":"15"}}},"boost":1}},{"constant_score":{"filter":{"match":{"lpi.saoText":{"query":"15"}}},"boost":1}},{"constant_score":{"filter":{"match":{"tokens.saoStartSuffix":{"query":"h16"}}},"boost":1}},{"constant_score":{"filter":{"match":{"lpi.saoStartSuffix":{"query":"h16"}}},"boost":1}},{"constant_score":{"filter":{"match":{"paf.subBuildingName":{"query":"h16"}}},"boost":1}},{"constant_score":{"filter":{"match":{"nisra.subBuildingName":{"query":"h16"}}},"boost":1}},{"constant_score":{"filter":{"match":{"lpi.saoText":{"query":"h16"}}},"boost":1}}]}}]}},{"dis_max":{"tie_breaker":0,"queries":[{"constant_score":{"filter":{"match":{"tokens.streetName":{"query":"h7","fuzziness":"1"}}},"boost":2}},{"constant_score":{"filter":{"match":{"paf.thoroughfare":{"query":"h7","fuzziness":"1"}}},"boost":2}},{"constant_score":{"filter":{"match":{"nisra.thoroughfare":{"query":"h7","fuzziness":"1"}}},"boost":2}},{"constant_score":{"filter":{"match":{"paf.welshThoroughfare":{"query":"h7","fuzziness":"1"}}},"boost":2}},{"constant_score":{"filter":{"match":{"paf.dependentThoroughfare":{"query":"h7","fuzziness":"1"}}},"boost":0.5}},{"constant_score":{"filter":{"match":{"paf.welshDependentThoroughfare":{"query":"h7","fuzziness":"1"}}},"boost":0.5}},{"constant_score":{"filter":{"match":{"nisra.dependentThoroughfare":{"query":"h7","fuzziness":"1"}}},"boost":0.5}},{"constant_score":{"filter":{"match":{"nisra.altThoroughfare":{"query":"h7","fuzziness":"1"}}},"boost":0.5}},{"constant_score":{"filter":{"match":{"lpi.streetDescriptor":{"query":"h7","fuzziness":"1"}}},"boost":2}}]}},{"dis_max":{"tie_breaker":0,"queries":[{"constant_score":{"filter":{"match":{"postcode":{"query":"h10"}}},"boost":1}},{"constant_score":{"filter":{"match":{"paf.postcode":{"query":"h10"}}},"boost":1}},{"constant_score":{"filter":{"match":{"nisra.postcode":{"query":"h10"}}},"boost":1}},{"constant_score":{"filter":{"match":{"lpi.postcodeLocator":{"query":"h10"}}},"boost":1}},{"constant_score":{"filter":{"bool":{"must":[{"match":{"postcodeOut":{"query":"h02p","fuzziness":"1"}}},{"match":{"postcodeIn":{"query":"h01p","fuzziness":"2"}}}]}},"boost":0.5}}]}},{"dis_max":{"tie_breaker":0.5,"queries":[{"dis_max":{"tie_breaker":0,"queries":[{"constant_score":{"filter":{"match":{"tokens.buildingName":{"query":"h5","fuzziness":"1"}}},"boost":2.5}},{"constant_score":{"filter":{"match":{"paf.buildingName":{"query":"h5","fuzziness":"1"}}},"boost":2.5}},{"constant_score":{"filter":{"match":{"nisra.buildingName":{"query":"h5","fuzziness":"1"}}},"boost":2.5}},{"constant_score":{"filter":{"match":{"lpi.paoText":{"query":"h5","fuzziness":"1","minimum_should_match":"-45%"}}},"boost":2.5}},{"constant_score":{"filter":{"bool":{"must":[{"match":{"lpi.paoStartNumber":{"query":"13"}}},{"match":{"lpi.paoStartSuffix":{"query":"h11"}}}]}},"boost":3}}]}},{"dis_max":{"tie_breaker":0,"queries":[{"dis_max":{"tie_breaker":0,"boost":0.5,"queries":[{"constant_score":{"filter":{"match":{"tokens.buildingName":{"query":"h5","minimum_should_match":"-45%"}}},"boost":1}},{"constant_score":{"filter":{"match":{"paf.subBuildingName":{"query":"h5","minimum_should_match":"-45%"}}},"boost":1.5}},{"constant_score":{"filter":{"match":{"nisra.subBuildingName":{"query":"h5","minimum_should_match":"-45%"}}},"boost":1.5}},{"constant_score":{"filter":{"match":{"lpi.saoText":{"query":"h5","minimum_should_match":"-45%"}}},"boost":1.5}},{"dis_max":{"tie_breaker":0.5,"queries":[{"constant_score":{"filter":{"match":{"tokens.paoStartNumber":{"query":"13"}}},"boost":1}},{"constant_score":{"filter":{"match":{"lpi.saoStartNumber":{"query":"13"}}},"boost":1}},{"constant_score":{"filter":{"match":{"paf.subBuildingName":{"query":"13"}}},"boost":1}},{"constant_score":{"filter":{"match":{"nisra.subBuildingName":{"query":"13"}}},"boost":1}},{"constant_score":{"filter":{"match":{"lpi.saoText":{"query":"13"}}},"boost":1}},{"constant_score":{"filter":{"match":{"tokens.paoStartSuffix":{"query":"h11"}}},"boost":1}},{"constant_score":{"filter":{"match":{"lpi.saoStartSuffix":{"query":"h11"}}},"boost":1}},{"constant_score":{"filter":{"match":{"paf.subBuildingName":{"query":"h11"}}},"boost":1}},{"constant_score":{"filter":{"match":{"nisra.subBuildingName":{"query":"h11"}}},"boost":1}},{"constant_score":{"filter":{"match":{"lpi.saoText":{"query":"h11"}}},"boost":1}}]}}]}}]}}]}},{"dis_max":{"tie_breaker":0.5,"queries":[{"dis_max":{"tie_breaker":0,"queries":[{"constant_score":{"filter":{"match":{"tokens.organisationName":{"query":"h2","minimum_should_match":"30%"}}},"boost":1}},{"constant_score":{"filter":{"match":{"paf.organisationName":{"query":"h2","minimum_should_match":"30%"}}},"boost":1}},{"constant_score":{"filter":{"match":{"nisra.organisationName":{"query":"h2","minimum_should_match":"30%"}}},"boost":1}},{"constant_score":{"filter":{"match":{"lpi.organisation":{"query":"h2","minimum_should_match":"30%"}}},"boost":1}},{"constant_score":{"filter":{"match":{"lpi.paoText":{"query":"h2","minimum_should_match":"30%"}}},"boost":1}},{"constant_score":{"filter":{"match":{"lpi.legalName":{"query":"h2","minimum_should_match":"30%"}}},"boost":1}},{"constant_score":{"filter":{"match":{"lpi.saoText":{"query":"h2","minimum_should_match":"30%"}}},"boost":0.5}}]}},{"dis_max":{"tie_breaker":0,"queries":[{"constant_score":{"filter":{"match":{"tokens.departmentName":{"query":"h3","minimum_should_match":"30%"}}},"boost":1}},{"constant_score":{"filter":{"match":{"paf.departmentName":{"query":"h3","minimum_should_match":"30%"}}},"boost":1}},{"constant_score":{"filter":{"match":{"lpi.legalName":{"query":"h3","minimum_should_match":"30%"}}},"boost":0.5}}]}}]}},{"dis_max":{"tie_breaker":0.5,"queries":[{"dis_max":{"tie_breaker":0,"queries":[{"constant_score":{"filter":{"match":{"tokens.townName":{"query":"h8","fuzziness":"1"}}},"boost":1}},{"constant_score":{"filter":{"match":{"paf.postTown":{"query":"h8","fuzziness":"1"}}},"boost":1}},{"constant_score":{"filter":{"match":{"paf.welshPostTown":{"query":"h8","fuzziness":"1"}}},"boost":1}},{"constant_score":{"filter":{"match":{"nisra.townName":{"query":"h8","fuzziness":"1"}}},"boost":1}},{"constant_score":{"filter":{"match":{"lpi.townName":{"query":"h8","fuzziness":"1"}}},"boost":1}},{"constant_score":{"filter":{"match":{"paf.dependentLocality":{"query":"h8","fuzziness":"1"}}},"boost":0.5}},{"constant_score":{"filter":{"match":{"paf.welshDependentLocality":{"query":"h8","fuzziness":"1"}}},"boost":0.5}},{"constant_score":{"filter":{"match":{"lpi.locality":{"query":"h8","fuzziness":"1"}}},"boost":0.5}},{"constant_score":{"filter":{"match":{"paf.doubleDependentLocality":{"query":"h8","fuzziness":"1"}}},"boost":0.2}},{"constant_score":{"filter":{"match":{"paf.welshDoubleDependentLocality":{"query":"h8","fuzziness":"1"}}},"boost":0.2}}]}},{"dis_max":{"tie_breaker":0,"queries":[{"constant_score":{"filter":{"match":{"tokens.locality":{"query":"h20","fuzziness":"1"}}},"boost":0.6}},{"constant_score":{"filter":{"match":{"paf.postTown":{"query":"h20","fuzziness":"1"}}},"boost":0.2}},{"constant_score":{"filter":{"match":{"nisra.townName":{"query":"h20","fuzziness":"1"}}},"boost":0.2}},{"constant_score":{"filter":{"match":{"paf.welshPostTown":{"query":"h20","fuzziness":"1"}}},"boost":0.2}},{"constant_score":{"filter":{"match":{"lpi.townName":{"query":"h20","fuzziness":"1"}}},"boost":0.2}},{"constant_score":{"filter":{"match":{"paf.dependentLocality":{"query":"h20","fuzziness":"1"}}},"boost":0.6}},{"constant_score":{"filter":{"match":{"paf.welshDependentLocality":{"query":"h20","fuzziness":"1"}}},"boost":0.6}},{"constant_score":{"filter":{"match":{"nisra.locality":{"query":"h20","fuzziness":"1"}}},"boost":0.6}},{"constant_score":{"filter":{"match":{"lpi.locality":{"query":"h20","fuzziness":"1"}}},"boost":0.6}},{"constant_score":{"filter":{"match":{"paf.doubleDependentLocality":{"query":"h20","fuzziness":"1"}}},"boost":0.3}},{"constant_score":{"filter":{"match":{"paf.welshDoubleDependentLocality":{"query":"h20","fuzziness":"1"}}},"boost":0.3}}]}}]}},{"dis_max":{"tie_breaker":0.5,"queries":[{"constant_score":{"filter":{"match":{"tokens.paoStartNumber":{"query":"12"}}},"boost":0.1}},{"constant_score":{"filter":{"match":{"tokens.paoStartNumber":{"query":"13"}}},"boost":0.1}},{"constant_score":{"filter":{"match":{"lpi.paoStartNumber":{"query":"13"}}},"boost":2}},{"constant_score":{"filter":{"match":{"lpi.paoStartSuffix":{"query":"h11"}}},"boost":2}},{"constant_score":{"filter":{"match":{"lpi.paoEndNumber":{"query":"12"}}},"boost":2}},{"constant_score":{"filter":{"match":{"lpi.paoEndSuffix":{"query":"h14"}}},"boost":2}},{"constant_score":{"filter":{"match":{"lpi.paoStartNumber":{"query":"12"}}},"boost":0.1}},{"constant_score":{"filter":{"match":{"paf.buildingNumber":{"query":"12"}}},"boost":0.1}},{"constant_score":{"filter":{"match":{"paf.buildingNumber":{"query":"13"}}},"boost":0.1}},{"constant_score":{"filter":{"match":{"nisra.paoStartNumber":{"query":"12"}}},"boost":0.1}},{"constant_score":{"filter":{"match":{"nisra.paoStartNumber":{"query":"13"}}},"boost":0.1}}]}},{"dis_max":{"tie_breaker":0.5,"queries":[{"constant_score":{"filter":{"match":{"lpi.saoStartNumber":{"query":"15"}}},"boost":1}},{"constant_score":{"filter":{"match":{"lpi.saoStartSuffix":{"query":"h16"}}},"boost":1}},{"constant_score":{"filter":{"match":{"lpi.saoEndNumber":{"query":"17"}}},"boost":1}},{"constant_score":{"filter":{"match":{"lpi.saoEndSuffix":{"query":"h18"}}},"boost":1}},{"constant_score":{"filter":{"match":{"lpi.saoStartNumber":{"query":"17"}}},"boost":0.1}},{"constant_score":{"filter":{"match":{"token.saoStartSuffix":{"query":"h16"}}},"boost":1}},{"constant_score":{"filter":{"match":{"lpi.saoEndSuffix":{"query":"h18"}}},"boost":1}}]}}],"filter":[{"terms":{"classificationCode":["RD06"]}}],"minimum_should_match":"-40%"}},{"bool":{"must":[{"dis_max":{"tie_breaker":0,"queries":[{"match":{"tokens.addressAll":{"query":"h2 h3 h4 h5 6 h7 h20 h8 h10","analyzer":"welsh_split_synonyms_analyzer","boost":8,"minimum_should_match":"-40%"}}},{"match":{"lpi.nagAll":{"query":"h2 h3 h4 h5 6 h7 h20 h8 h10","analyzer":"welsh_split_synonyms_analyzer","boost":1,"minimum_should_match":"-40%"}}},{"match":{"nisra.nisraAll":{"query":"h2 h3 h4 h5 6 h7 h20 h8 h10","analyzer":"welsh_split_synonyms_analyzer","boost":1,"minimum_should_match":"-40%"}}},{"match":{"paf.pafAll":{"query":"h2 h3 h4 h5 6 h7 h20 h8 h10","analyzer":"welsh_split_synonyms_analyzer","boost":1,"minimum_should_match":"-40%"}}}]}}],"should":[{"dis_max":{"tie_breaker":0,"queries":[{"match":{"tokens.addressAll.bigram":{"query":"h2 h3 h4 h5 6 h7 h20 h8 h10","boost":2,"fuzziness":"0"}}},{"match":{"lpi.nagAll.bigram":{"query":"h2 h3 h4 h5 6 h7 h20 h8 h10","boost":0.4,"fuzziness":"0"}}},{"match":{"nisra.nisraAll.bigram":{"query":"h2 h3 h4 h5 6 h7 h20 h8 h10","boost":0.4,"fuzziness":"0"}}},{"match":{"paf.pafAll.bigram":{"query":"h2 h3 h4 h5 6 h7 h20 h8 h10","boost":0.4,"fuzziness":"0"}}}]}}],"filter":[{"terms":{"classificationCode":["RD06"]}}],"boost":0.5}}]}},"from":0,"size":1,"sort":[{"_score":{"order":"desc"}},{"uprn":{"order":"asc"}}],"track_scores":true}
        """.stripMargin)

      // When
      val args = AddressArgs(
        input = "",
        tokens = tokens,
        filters = filters,
        region = None,
        limit = 1,
        verbose = false,
      )
      val result = Json.parse(SearchBodyBuilderFn(repository.makeQuery(args)).string())

      // Then
      result shouldBe expected
    }

  }

}
