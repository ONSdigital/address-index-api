package uk.gov.ons.addressIndex.server.modules

import com.sksamuel.elastic4s.analyzers.{CustomAnalyzerDefinition, StandardTokenizer}
import com.sksamuel.elastic4s.http.HttpClient
import com.sksamuel.elastic4s.http.search.SearchBodyBuilderFn
import com.sksamuel.elastic4s.mappings.MappingDefinition
import com.sksamuel.elastic4s.testkit._
import org.joda.time.DateTime
import org.scalatest.WordSpec
import play.api.libs.json.Json
import uk.gov.ons.addressIndex.model.config.QueryParamsConfig
import uk.gov.ons.addressIndex.model.db.BulkAddressRequestData
import uk.gov.ons.addressIndex.model.db.index._
import uk.gov.ons.addressIndex.parsers.Tokens
import uk.gov.ons.addressIndex.server.model.dao.ElasticClientProvider

import scala.concurrent.ExecutionContext.Implicits.global

class ElasticsearchRepositorySpec extends WordSpec with SearchMatchers with ClassLocalNodeProvider with HttpElasticSugar {

  val testClient: HttpClient = http

  // injections
  val elasticClientProvider: ElasticClientProvider = new ElasticClientProvider {
    override def client: HttpClient = testClient
  }

  val defaultLat = "50.705948"
  val defaultLon = "-3.5091076"

  val defaultEpoch = "_current"

  val config = new AddressIndexConfigModule
  val queryParams: QueryParamsConfig = config.config.elasticSearch.queryParams

  val hybridIndexName: String = config.config.elasticSearch.indexes.hybridIndex + defaultEpoch
  val hybridIndexHistoricalName: String = config.config.elasticSearch.indexes.hybridIndexHistorical + defaultEpoch
  val hybridMappings: String = config.config.elasticSearch.indexes.hybridMapping

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

  val hybridNagCustGeogCode = "E07000041"
  val hybridStartDate = "2013-01-01"
  val hybridEndDate = "2014-01-01"
  val hybridSecondStartDate = "2014-01-02"
  val hybridCurrentEndDate: String = DateTime.now.toString("yyyy-MM-dd")
  //  val hybridCurrentEndDate = "2018-07-18"
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
    "classificationCode" -> hybridFirstClassificationCode
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
    "classificationCode" -> hybridFirstClassificationCode
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

  testClient.execute {
    createIndex(hybridIndexName)
      .mappings(MappingDefinition.apply(hybridMappings))
      .analysis(Some(CustomAnalyzerDefinition("welsh_split_synonyms_analyzer",
        StandardTokenizer("myTokenizer1"))
      ))
  }.await

  testClient.execute {
    createIndex(hybridIndexHistoricalName)
      .mappings(MappingDefinition.apply(hybridMappings))
      .analysis(Some(CustomAnalyzerDefinition("welsh_split_synonyms_analyzer",
        StandardTokenizer("myTokenizer1"))
      ))
  }.await

  testClient.execute {
    bulk(
      indexInto(hybridIndexName / hybridMappings).fields(firstHybridHistEs)
    )
  }.await

  blockUntilCount(1, hybridIndexName)

  testClient.execute {
    bulk(
      indexInto(hybridIndexHistoricalName / hybridMappings).fields(firstHybridEs),
      indexInto(hybridIndexHistoricalName / hybridMappings).fields(secondHybridEs)
    )
  }.await

  blockUntilCount(2, hybridIndexHistoricalName)

  // The following documents are added separately as the blocking action on 5 documents was timing out the test
  testClient.execute {
    bulk(
      indexInto(hybridIndexHistoricalName / hybridMappings).fields(thirdHybridEs),
      indexInto(hybridIndexHistoricalName / hybridMappings).fields(fourthHybridEs),
      indexInto(hybridIndexHistoricalName / hybridMappings).fields(fifthHybridEs)
    )
  }.await

  blockUntilCount(3, hybridIndexHistoricalName)

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
    hybridMixedNag
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

  val expectedRelative = Relative(
    level = hybridRelLevel,
    siblings = hybridRelSibArray,
    parents = hybridRelParArray
  )

  val expectedCrossRef = CrossRef(
    crossReference = hybridCrossRefReference,
    source = hybridCrossRefSource
  )

  val expectedCrossRef2 = CrossRef(
    crossReference = hybridCrossRefReference2,
    source = hybridCrossRefSource2
  )

  val expectedHybrid = HybridAddressFull(
    uprn = hybridFirstUprn.toString,
    parentUprn = hybridFirstParentUprn.toString,
    relatives = Seq(expectedRelative),
    crossRefs = Seq(expectedCrossRef, expectedCrossRef2),
    postcodeIn = hybridFirstPostcodeIn,
    postcodeOut = hybridFirstPostcodeOut,
    lpi = Seq(expectedNag),
    paf = Seq(expectedPaf),
    score = 1.0f,
    classificationCode = hybridFirstClassificationCode
  )

  val expectedDateHybrid = HybridAddressFull(
    uprn = hybridFirstDateUprn.toString,
    parentUprn = hybridFirstParentUprn.toString,
    relatives = Seq(expectedRelative),
    crossRefs = Seq(expectedCrossRef, expectedCrossRef2),
    postcodeIn = hybridFirstPostcodeIn,
    postcodeOut = hybridFirstPostcodeOut,
    lpi = Seq(expectedDateNag),
    paf = Seq(),
    score = 1.0f,
    classificationCode = hybridFirstClassificationCode
  )

  val expectedSecondDateHybrid = HybridAddressFull(
    uprn = hybridSecondDateUprn.toString,
    parentUprn = hybridFirstParentUprn.toString,
    relatives = Seq(expectedRelative),
    crossRefs = Seq(expectedCrossRef, expectedCrossRef2),
    postcodeIn = hybridFirstPostcodeIn,
    postcodeOut = hybridFirstPostcodeOut,
    lpi = Seq(expectedSecondDateNag, expectedThirdDateNag),
    paf = Seq(expectedDatePaf),
    score = 1.0f,
    classificationCode = hybridFirstClassificationCode
  )

  val expectedThirdDateHybrid = HybridAddressFull(
    uprn = hybridThirdDateUprn.toString,
    parentUprn = hybridFirstParentUprn.toString,
    relatives = Seq(expectedRelative),
    crossRefs = Seq(expectedCrossRef, expectedCrossRef2),
    postcodeIn = hybridFirstPostcodeIn,
    postcodeOut = hybridFirstPostcodeOut,
    lpi = Seq(),
    paf = Seq(expectedSecondDatePaf),
    score = 1.0f,
    classificationCode = hybridFirstClassificationCode
  )

  val expectedHybridHist: HybridAddressFull = expectedHybrid.copy(uprn = hybridFirstUprnHist.toString)

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
      val result = Json.parse(SearchBodyBuilderFn(repository.generateQueryUprnRequest(hybridFirstUprn.toString, epoch = "")).string())

      // Then
      result shouldBe expected
    }

    "find HYBRID address by UPRN between date range" in {

      // Given
      val repository = new AddressIndexRepository(config, elasticClientProvider)
      val expected = Some(expectedDateHybrid)

      // When
      val result = repository.queryUprn(hybridFirstDateUprn.toString, "2013-01-01", "2014-01-01").await

      // Then
      result.get.lpi.head shouldBe expectedDateNag
      result.get.paf shouldBe Seq()
      result shouldBe expected
    }

    "find no HYBRID address by UPRN when not between date range" in {

      // Given
      val repository = new AddressIndexRepository(config, elasticClientProvider)
      val expected = None

      // When
      val result = repository.queryUprn(hybridFirstDateUprn.toString, "2013-01-01", "2013-12-31").await

      // Then
      result shouldBe expected
    }

    "find HYBRID address by UPRN between date range with PAF and multiple NAG" in {

      // Given
      val repository = new AddressIndexRepository(config, elasticClientProvider)
      // A fuller Address with a PAF and multiple NAG's one of which is historical
      val expected = Some(expectedSecondDateHybrid)

      // When
      val result = repository.queryUprn(hybridSecondDateUprn.toString, "2014-01-02", hybridCurrentEndDate).await

      // Then
      result.get.lpi.head shouldBe expectedSecondDateNag
      result.get.paf.head shouldBe expectedDatePaf
      result shouldBe expected
    }

    "find HYBRID address by UPRN between date range with PAF and no NAG" in {

      // Given
      val repository = new AddressIndexRepository(config, elasticClientProvider)

      // Forces it to search on PAF dates
      val expected = Some(expectedThirdDateHybrid)

      // When
      // Using 2015-01-01 start date should be found since the query uses 'gte' but it isn't. Elastic4s issue?
      val result = repository.queryUprn(hybridThirdDateUprn.toString, "2014-12-31", hybridCurrentEndDate).await

      // Then
      result.get.lpi shouldBe Seq()
      result.get.paf.head shouldBe expectedSecondDatePaf
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

    "generate valid query from partial address" in {

      // Given
      val repository = new AddressIndexRepository(config, elasticClientProvider)
      val expected = Json.parse(
        s"""
          {
           	"version": true,
           	"query": {
           		"bool": {
           			"must": [{
           				"multi_match": {
           					"query": "h4",
           					"fields": ["lpi.nagAll.partial","paf.mixedPaf.partial","paf.mixedWelshPaf.partial"],
           					"type": "phrase",
                    "slop":4
           				}
           			}],
           			"should": [{
                     "dis_max": {
                       "queries": [
                         {
                           "match": {
                             "lpi.paoStartNumber": {
                               "query": "4",
                               "boost": 0.5,
                               "fuzzy_transpositions": false,
                               "max_expansions": 10,
                               "prefix_length": "1"
                             }
                           }
                         },
                         {
                           "match": {
                             "lpi.saoStartNumber": {
                               "query": "4",
                               "boost": 0.2,
                               "fuzzy_transpositions": false,
                               "max_expansions": 10,
                               "prefix_length": "1"
                             }
                           }
                         }
                       ]
                     }
                   }],
           			"filter": [{
           				"prefix": {
           					"classificationCode": {
           						"value": "R"
           					}
           				}
           			},
           			{
           				"bool": {
           					"must_not": [{
           						"term": {
           							"lpi.addressBasePostal": {
           								"value": "N"
           							}
           						}
           					}]
           				}
           			}]
           		}
           	}
          }
         """.stripMargin
      )

      // When
      val result = Json.parse(SearchBodyBuilderFn(repository.generateQueryPartialAddressRequest("h4", "residential", "", "", epoch = "")).string())

      // Then
      result shouldBe expected
    }

    "generate valid fallback query from partial address" in {

      // Given
      val repository = new AddressIndexRepository(config, elasticClientProvider)
      val expected = Json.parse(
        s"""
          {
           	"version": true,
           	"query": {
           		"bool": {
           			"must": [{
           				"multi_match": {
           					"query": "h4",
           					"fields": ["lpi.nagAll.partial","paf.mixedPaf.partial","paf.mixedWelshPaf.partial"],
           					"type": "best_fields"
           				}
           			}],
                      			"should": [{
                                "dis_max": {
                                  "queries": [
                                    {
                                      "match": {
                                        "lpi.paoStartNumber": {
                                          "query": "4",
                                          "boost": 0.5,
                                          "fuzzy_transpositions": false,
                                          "max_expansions": 10,
                                          "prefix_length": "1"
                                        }
                                      }
                                    },
                                    {
                                      "match": {
                                        "lpi.saoStartNumber": {
                                          "query": "4",
                                          "boost": 0.2,
                                          "fuzzy_transpositions": false,
                                          "max_expansions": 10,
                                          "prefix_length": "1"
                                        }
                                      }
                                    }
                                  ]
                                }
                              }],
           			"filter": [{
           				"prefix": {
           					"classificationCode": {
           						"value": "R"
           					}
           				}
           			},
           			{
           				"bool": {
           					"must_not": [{
           						"term": {
           							"lpi.addressBasePostal": {
           								"value": "N"
           							}
           						}
           					}]
           				}
           			}]
           		}
           	}
          }
         """.stripMargin
      )

      // When
      val result = Json.parse(SearchBodyBuilderFn(repository.generateQueryPartialAddressRequest("h4", "residential", "", "", fallback = true, epoch = "")).string())

      // Then
      result shouldBe expected
    }

    "generate valid query from partial address with date" in {

      // Given
      val repository = new AddressIndexRepository(config, elasticClientProvider)
      val expected = Json.parse(
        s"""
          {
           	"version": true,
           	"query": {
           		"bool": {
           			"must": [{
           				"multi_match": {
           					"query": "h4",
           					"fields": ["lpi.nagAll.partial","paf.mixedPaf.partial","paf.mixedWelshPaf.partial"],
           					"type": "phrase",
                    "slop":4
           				}
           			}],
                      			"should": [{
                                "dis_max": {
                                  "queries": [
                                    {
                                      "match": {
                                        "lpi.paoStartNumber": {
                                          "query": "4",
                                          "boost": 0.5,
                                          "fuzzy_transpositions": false,
                                          "max_expansions": 10,
                                          "prefix_length": "1"
                                        }
                                      }
                                    },
                                    {
                                      "match": {
                                        "lpi.saoStartNumber": {
                                          "query": "4",
                                          "boost": 0.2,
                                          "fuzzy_transpositions": false,
                                          "max_expansions": 10,
                                          "prefix_length": "1"
                                        }
                                      }
                                    }
                                  ]
                                }
                              }],
           			"filter": [{
           				"prefix": {
           					"classificationCode": {
           						"value": "R"
           					}
           				}
           			},
           			{
           				"bool": {
           					"must_not": [{
           						"term": {
           							"lpi.addressBasePostal": {
           								"value": "N"
           							}
           						}
           					}]
           				}
           			},
           			{
           				"bool": {
           					"should": [{
           						"bool": {
           							"must": [{
           								"range": {
           									"paf.startDate": {
           										"gte": "2013-01-01",
           										"format": "yyyy-MM-dd"
           									}
           								}
           							},
           							{
           								"range": {
           									"paf.endDate": {
           										"lte": "2013-12-31",
           										"format": "yyyy-MM-dd"
           									}
           								}
           							}]
           						}
           					},
           					{
           						"bool": {
           							"must": [{
           								"range": {
           									"lpi.lpiStartDate": {
           										"gte": "2013-01-01",
           										"format": "yyyy-MM-dd"
           									}
           								}
           							},
           							{
           								"range": {
           									"lpi.lpiEndDate": {
           										"lte": "2013-12-31",
           										"format": "yyyy-MM-dd"
           									}
           								}
           							}]
           						}
           					}]
           				}
           			}]
           		}
           	}
          }
         """.stripMargin
      )

      // When
      val result = Json.parse(SearchBodyBuilderFn(repository.generateQueryPartialAddressRequest("h4", "residential", "2013-01-01", "2013-12-31", epoch = "")).string())

      // Then
      result shouldBe expected
    }

    "generate valid fallback query from partial address with date" in {

      // Given
      val repository = new AddressIndexRepository(config, elasticClientProvider)
      val expected = Json.parse(
        s"""
          {
           	"version": true,
           	"query": {
           		"bool": {
           			"must": [{
           				"multi_match": {
           					"query": "h4",
           					"fields": ["lpi.nagAll.partial","paf.mixedPaf.partial","paf.mixedWelshPaf.partial"],
           					"type": "best_fields"
           				}
           			}],
                      			"should": [{
                                "dis_max": {
                                  "queries": [
                                    {
                                      "match": {
                                        "lpi.paoStartNumber": {
                                          "query": "4",
                                          "boost": 0.5,
                                          "fuzzy_transpositions": false,
                                          "max_expansions": 10,
                                          "prefix_length": "1"
                                        }
                                      }
                                    },
                                    {
                                      "match": {
                                        "lpi.saoStartNumber": {
                                          "query": "4",
                                          "boost": 0.2,
                                          "fuzzy_transpositions": false,
                                          "max_expansions": 10,
                                          "prefix_length": "1"
                                        }
                                      }
                                    }
                                  ]
                                }
                              }],
           			"filter": [{
           				"prefix": {
           					"classificationCode": {
           						"value": "R"
           					}
           				}
           			},
           			{
           				"bool": {
           					"must_not": [{
           						"term": {
           							"lpi.addressBasePostal": {
           								"value": "N"
           							}
           						}
           					}]
           				}
           			},
           			{
           				"bool": {
           					"should": [{
           						"bool": {
           							"must": [{
           								"range": {
           									"paf.startDate": {
           										"gte": "2013-01-01",
           										"format": "yyyy-MM-dd"
           									}
           								}
           							},
           							{
           								"range": {
           									"paf.endDate": {
           										"lte": "2013-12-31",
           										"format": "yyyy-MM-dd"
           									}
           								}
           							}]
           						}
           					},
           					{
           						"bool": {
           							"must": [{
           								"range": {
           									"lpi.lpiStartDate": {
           										"gte": "2013-01-01",
           										"format": "yyyy-MM-dd"
           									}
           								}
           							},
           							{
           								"range": {
           									"lpi.lpiEndDate": {
           										"lte": "2013-12-31",
           										"format": "yyyy-MM-dd"
           									}
           								}
           							}]
           						}
           					}]
           				}
           			}]
           		}
           	}
          }
         """.stripMargin
      )

      // When
      val result = Json.parse(SearchBodyBuilderFn(repository.generateQueryPartialAddressRequest("h4", "residential", "2013-01-01", "2013-12-31", fallback = true, epoch = "")).string())

      // Then
      result shouldBe expected
    }

    "find HYBRID address by UPRN in non-historical index" in {

      // Given
      val repository = new AddressIndexRepository(config, elasticClientProvider)
      val expected = Some(expectedHybridHist)

      // When
      val result = repository.queryUprn(hybridFirstUprnHist.toString, historical = false).await

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
          {
           	"version": true,
           	"query": {
           		"bool": {
           			"must": [{
           				"term": {
           					"lpi.postcodeLocator": {
           						"value": " H4"
           					}
           				}
           			}],
           			"filter": [{
           				"prefix": {
           					"classificationCode": {
           						"value": "R"
           					}
           				}
           			},
           			{
           				"bool": {
           					"must_not": [{
           						"term": {
           							"lpi.addressBasePostal": {
           								"value": "N"
           							}
           						}
           					}]
           				}
           			}]
           		}
           	},
           	"sort": [{
           		"lpi.streetDescriptor.keyword": {
           			"order": "asc"
           		}
           	},
           	{
           		"lpi.paoStartNumber": {
           			"order": "asc"
           		}
           	},
           	{
           		"lpi.paoStartSuffix.keyword": {
           			"order": "asc"
           		}
           	},
           	{
           		"uprn": {
           			"order": "asc"
           		}
           	}]
          }
         """.stripMargin
      )

      // When
      val result = Json.parse(SearchBodyBuilderFn(repository.generateQueryPostcodeRequest("h4", "residential", "", "", epoch = "")).string())

      // Then
      result shouldBe expected
    }

    "find HYBRID address by postcode with date" in {

      // Given
      val repository = new AddressIndexRepository(config, elasticClientProvider)
      val expected = Json.parse(
        s"""
          {
           	"version": true,
           	"query": {
           		"bool": {
           			"must": [{
           				"term": {
           					"lpi.postcodeLocator": {
           						"value": " H4"
           					}
           				}
           			}],
           			"filter": [{
           				"prefix": {
           					"classificationCode": {
           						"value": "R"
           					}
           				}
           			},
                {
                 "bool": {
                   "must_not": [{
                     "term": {
                       "lpi.addressBasePostal": {
                         "value": "N"
                       }
                     }
                   }]
                 }
                },
                {
                 "bool": {
                   "should": [{
                     "bool": {
                       "must": [{
                         "range": {
                           "paf.startDate": {
                             "gte": "2013-01-01",
                             "format": "yyyy-MM-dd"
                           }
                         }
                       },
                       {
                         "range": {
                           "paf.endDate": {
                             "lte": "2013-12-31",
                             "format": "yyyy-MM-dd"
                           }
                         }
                       }]
                     }
                   },
                   {
                     "bool": {
                       "must": [{
                         "range": {
                           "lpi.lpiStartDate": {
                             "gte": "2013-01-01",
                             "format": "yyyy-MM-dd"
                           }
                         }
                       },
                       {
                         "range": {
                           "lpi.lpiEndDate": {
                             "lte": "2013-12-31",
                             "format": "yyyy-MM-dd"
                           }
                         }
                       }]
                     }
                   }]
                 }
                }]
           		}
           	},
           	"sort": [{
           		"lpi.streetDescriptor.keyword": {
           			"order": "asc"
           		}
           	},
           	{
           		"lpi.paoStartNumber": {
           			"order": "asc"
           		}
           	},
           	{
           		"lpi.paoStartSuffix.keyword": {
           			"order": "asc"
           		}
           	},
           	{
           		"uprn": {
           			"order": "asc"
           		}
           	}]
          }
         """.stripMargin
      )

      // When
      val result = Json.parse(SearchBodyBuilderFn(repository.generateQueryPostcodeRequest("h4", "residential", "2013-01-01", "2013-12-31", epoch = "")).string())

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
      val HybridAddressCollection(results, maxScore, total) = repository.queryAddresses(tokens, 0, 10, "", "", defaultLat, defaultLon, epoch = "").await

      // Then
      results.length should be > 0 // it MAY return more than 1 addresses, but the top one should remain the same
      total should be > 0l

      val resultHybrid = results.head
      resultHybrid shouldBe expected.copy(score = resultHybrid.score)

      // Score is random, but should always be positive
      resultHybrid.score should be > 0f
      maxScore should be > 0d
    }

    "generate valid query to find HYBRID addresses by building number and postcode by date and range" in {

      // Given
      val repository = new AddressIndexRepository(config, elasticClientProvider)
      val tokens: Map[String, String] = Map(
        Tokens.buildingNumber -> hybridNagPaoStartNumber.toString,
        Tokens.postcode -> hybridNagPostcodeLocator
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
           											"paf.postcode": {
           												"query": "h10"
           											}
           										}
           									},
           									"boost": 1
           								}
           							},
           							{
           								"constant_score": {
           									"filter": {
           										"match": {
           											"lpi.postcodeLocator": {
           												"query": "h10"
           											}
           										}
           									},
           									"boost": 1
           								}
           							}]
           						}
           					}],
           					"filter": [{
           						"geo_distance": {
           							"distance": "10km",
           							"lpi.location": [$defaultLon,
           							$defaultLat]
           						}
           					},
           					{
           						"bool": {
           							"should": [{
           								"bool": {
           									"must": [{
           										"range": {
           											"paf.startDate": {
           												"gte": "$hybridStartDate",
           												"format": "yyyy-MM-dd"
           											}
           										}
           									},
           									{
           										"range": {
           											"paf.endDate": {
           												"lte": "$hybridEndDate",
           												"format": "yyyy-MM-dd"
           											}
           										}
           									}]
           								}
           							},
           							{
           								"bool": {
           									"must": [{
           										"range": {
           											"lpi.lpiStartDate": {
           												"gte": "$hybridStartDate",
           												"format": "yyyy-MM-dd"
           											}
           										}
           									},
           									{
           										"range": {
           											"lpi.lpiEndDate": {
           												"lte": "$hybridEndDate",
           												"format": "yyyy-MM-dd"
           											}
           										}
           									}]
           								}
           							}]
           						}
           					}],
           					"minimum_should_match": "-40%"
           				}
           			},
           			{
           				"bool": {
           					"must": [{
           						"dis_max": {
           							"tie_breaker": 0,
           							"queries": [{
           								"match": {
           									"lpi.nagAll": {
           										"query": "13 h10",
           										"analyzer": "welsh_split_synonyms_analyzer",
           										"boost": 1,
           										"minimum_should_match": "-40%"
           									}
           								}
           							},
           							{
           								"match": {
           									"paf.pafAll": {
           										"query": "13 h10",
           										"analyzer": "welsh_split_synonyms_analyzer",
           										"boost": 1,
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
           										"query": "13 h10",
           										"boost": 0.2,
           										"fuzziness": "0"
           									}
           								}
           							},
           							{
           								"match": {
           									"paf.pafAll.bigram": {
           										"query": "13 h10",
           										"boost": 0.2,
           										"fuzziness": "0"
           									}
           								}
           							}]
           						}
           					}],
           					"filter": [{
           						"geo_distance": {
           							"distance": "10km",
           							"lpi.location": [-3.5091076,
           							50.705948]
           						}
           					},
           					{
           						"bool": {
           							"should": [{
           								"bool": {
           									"must": [{
           										"range": {
           											"paf.startDate": {
           												"gte": "$hybridStartDate",
           												"format": "yyyy-MM-dd"
           											}
           										}
           									},
           									{
           										"range": {
           											"paf.endDate": {
           												"lte": "$hybridEndDate",
           												"format": "yyyy-MM-dd"
           											}
           										}
           									}]
           								}
           							},
           							{
           								"bool": {
           									"must": [{
           										"range": {
           											"lpi.lpiStartDate": {
           												"gte": "$hybridStartDate",
           												"format": "yyyy-MM-dd"
           											}
           										}
           									},
           									{
           										"range": {
           											"lpi.lpiEndDate": {
           												"lte": "$hybridEndDate",
           												"format": "yyyy-MM-dd"
           											}
           										}
           									}]
           								}
           							}]
           						}
           					}],
           					"boost": 0.075
           				}
           			}]
           		}
           	},
           	"sort": [{
           		"_score": {
           			"order": "desc"
           		}
           	},
           	{
           		"uprn": {
           			"order": "asc"
           		}
           	}],
           	"track_scores": true
          }
          """.stripMargin
      )

      // When
      val result = Json.parse(SearchBodyBuilderFn(repository.generateQueryAddressRequest(tokens, "", "10", defaultLat, defaultLon, hybridStartDate, hybridEndDate, epoch = "")).string())

      // Then
      result shouldBe expected
    }

    "have score of `0` if no addresses found" in {
      // Given
      val repository = new AddressIndexRepository(config, elasticClientProvider)
      val tokens = Map(
        Tokens.buildingNumber -> "9999"
      )

      // When
      val HybridAddressCollection(results, maxScore, total) = repository.queryAddresses(tokens, 0, 10, "", "", defaultLat, defaultLon, epoch = "").await

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
      val result = Json.parse(SearchBodyBuilderFn(repository.generateQueryAddressRequest(tokens, "", "", defaultLat, defaultLon, "", "", epoch = "")).string())

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
      val result = Json.parse(SearchBodyBuilderFn(repository.generateQueryAddressRequest(tokens, "", "", defaultLat, defaultLon, "", "", epoch = "")).string())

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

    "bulk search addresses by date" in {
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
      val results = repository.queryBulk(inputs, limit = 1, "2013-01-01", "2013-12-31", matchThreshold = 5F).await
      val addresses = results.collect {
        case Right(address) => address
      }.flatten

      // Then
      results.length shouldBe 2
      addresses.length shouldBe 2

      addresses.head.uprn shouldBe hybridSecondDateUprn.toString
      addresses.last.uprn shouldBe HybridAddressFull.empty.uprn
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
      val results = repository.queryBulk(inputs, limit = 1, matchThreshold = 5F).await
      val addresses = results.collect {
        case Right(address) => address
      }.flatten

      // Then
      results.length shouldBe 2
      addresses.length shouldBe 2

      addresses.head.uprn shouldBe HybridAddressFull.empty.uprn
      addresses(1).uprn shouldBe HybridAddressFull.empty.uprn
    }

    "return prefix filter for 'R' when passed filter 'residential' " in {
      // Given
      val repository = new AddressIndexRepository(config, elasticClientProvider)

      val tokens: Map[String, String] = Map.empty

      val filters: String = "residential"

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
                  "filter": [
                    {
                      "prefix": {
                        "classificationCode": {
                          "value": "R"
                        }
                      }
                    }
                  ],
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
      val result = Json.parse(SearchBodyBuilderFn(repository.generateQueryAddressRequest(tokens, filters, "", defaultLat, defaultLon, "", "", epoch = "")).string())

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
                  "filter": [
                    {
                      "prefix": {
                        "classificationCode": {
                          "value": "C"
                        }
                      }
                    }
                  ],
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
      val result = Json.parse(SearchBodyBuilderFn(repository.generateQueryAddressRequest(tokens, filters, "", defaultLat, defaultLon, "", "", epoch = "")).string())

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
                  "filter": [
                    {
                      "terms": {
                        "classificationCode": ["RD06"]
                      }
                    }
                  ],
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
      val result = Json.parse(SearchBodyBuilderFn(repository.generateQueryAddressRequest(tokens, filters, "", defaultLat, defaultLon, "", "", epoch = "")).string())

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
                  "filter": [
                    {
                      "terms": {
                        "classificationCode": ["RD06","RD"]
                      }
                    }
                  ],
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
      val result = Json.parse(SearchBodyBuilderFn(repository.generateQueryAddressRequest(tokens, filters, "", defaultLat, defaultLon, "", "", epoch = "")).string())

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
                  "filter": [
                    {
                      "terms": {
                        "classificationCode": ["RD*","RD02"]
                      }
                    }
                  ],
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
      val result = Json.parse(SearchBodyBuilderFn(repository.generateQueryAddressRequest(tokens, filters, "", defaultLat, defaultLon, "", "", epoch = "")).string())

      // Then
      result shouldBe expected
    }

    "generate valid query for search via partial endpoint - term with house number" in {
      // Given
      val repository = new AddressIndexRepository(config, elasticClientProvider)

      val expected = Json.parse(
        """
        {
          "version":true,
          "query" : {
            "bool" : {
              "must" : [{
                "multi_match":{
                  "query":"7 Gate Re",
                  "fields":["lpi.nagAll.partial","paf.mixedPaf.partial","paf.mixedWelshPaf.partial"],
                  "type":"phrase",
                  "slop":4
                }
              }],
           			"should": [{
                      "dis_max": {
                       "queries": [
                         {
                           "match": {
                             "lpi.paoStartNumber": {
                               "query": "7",
                               "boost": 0.5,
                               "fuzzy_transpositions": false,
                               "max_expansions": 10,
                               "prefix_length": "1"
                             }
                           }
                         },
                         {
                           "match": {
                             "lpi.saoStartNumber": {
                               "query": "7",
                               "boost": 0.2,
                               "fuzzy_transpositions": false,
                               "max_expansions": 10,
                               "prefix_length": "1"
                             }
                           }
                         }
                       ]
                     }
                   }],
              "filter":[{
                "bool":{
                  "must_not":[{
                    "term":{
                      "lpi.addressBasePostal":{
                        "value":"N"
                      }
                    }
                  }]
                }
              }]
            }
          }
        }
        """
      )

      // When
      val result = Json.parse(SearchBodyBuilderFn(repository.generateQueryPartialAddressRequest(partialInput, partialFilterNone, "", "", epoch = "")).string())

      // Then
      result shouldBe expected
    }

    "generate valid fallback query for search via partial endpoint - term with house number" in {
      // Given
      val repository = new AddressIndexRepository(config, elasticClientProvider)

      val expected = Json.parse(
        """
        {
          "version":true,
          "query" : {
            "bool" : {
              "must" : [{
                "multi_match":{
                  "query":"7 Gate Ret",
                  "fields":["lpi.nagAll.partial","paf.mixedPaf.partial","paf.mixedWelshPaf.partial"],
                  "type":"best_fields"
                }
              }],
           			"should": [{
                      "dis_max": {
                       "queries": [
                         {
                           "match": {
                              "lpi.paoStartNumber": {
                              "query": "7",
                               "boost": 0.5,
                               "fuzzy_transpositions": false,
                               "max_expansions": 10,
                               "prefix_length": "1"
                             }
                           }
                         },
                         {
                           "match": {
                             "lpi.saoStartNumber": {
                               "query": "7",
                               "boost": 0.2,
                               "fuzzy_transpositions": false,
                               "max_expansions": 10,
                               "prefix_length": "1"
                             }
                           }
                         }
                       ]
                     }
                   }],
              "filter":[{
                "bool":{
                  "must_not":[{
                    "term":{
                      "lpi.addressBasePostal":{
                        "value":"N"
                      }
                    }
                  }]
                }
              }]
            }
          }
        }
        """
      )

      // When
      val result = Json.parse(SearchBodyBuilderFn(repository.generateQueryPartialAddressRequest(partialInputFallback, partialFilterNone, "", "", fallback = true, epoch = "")).string())

      // Then
      result shouldBe expected
    }

    "generate valid query for search via partial endpoint - term without house number" in {
      // Given
      val repository = new AddressIndexRepository(config, elasticClientProvider)

      val expected = Json.parse(
        """
        {
          "version":true,
          "query" : {
            "bool" : {
              "must" : [{
                "multi_match":{
                  "query":"Gate Re",
                  "fields":["lpi.nagAll.partial","paf.mixedPaf.partial","paf.mixedWelshPaf.partial"],
                  "type":"phrase",
                  "slop":4
                }
              }],
              "filter":[{
                "bool":{
                  "must_not":[{
                    "term":{
                      "lpi.addressBasePostal":{
                        "value":"N"
                      }
                    }
                  }]
                }
              }]
            }
          }
        }
        """
      )

      // When
      val result = Json.parse(SearchBodyBuilderFn(repository.generateQueryPartialAddressRequest(partialInputWithout, partialFilterNone, "", "", epoch = "")).string())

      // Then
      result shouldBe expected
    }

    "generate valid fallback query for search via partial endpoint - term without house number" in {
      // Given
      val repository = new AddressIndexRepository(config, elasticClientProvider)

      val expected = Json.parse(
        """
        {
          "version":true,
          "query" : {
            "bool" : {
              "must" : [{
                "multi_match":{
                  "query":"Gate Ret",
                  "fields":["lpi.nagAll.partial","paf.mixedPaf.partial","paf.mixedWelshPaf.partial"],
                  "type":"best_fields"
                }
              }],
              "filter":[{
                "bool":{
                  "must_not":[{
                    "term":{
                      "lpi.addressBasePostal":{
                        "value":"N"
                      }
                    }
                  }]
                }
              }]
            }
          }
        }
        """
      )

      // When
      val result = Json.parse(SearchBodyBuilderFn(repository.generateQueryPartialAddressRequest(partialInputWithoutFallback, partialFilterNone, "", "", fallback = true, epoch = "")).string())

      // Then
      result shouldBe expected
    }


    "generate valid query for search via partial endpoint - term with house number and exact filter" in {
      // Given
      val repository = new AddressIndexRepository(config, elasticClientProvider)

      val expected = Json.parse(
        """
        {
          "version":true,
          "query" : {
            "bool" : {
              "must" : [{
                "multi_match":{
                  "query":"7 Gate Re",
                  "fields":["lpi.nagAll.partial","paf.mixedPaf.partial","paf.mixedWelshPaf.partial"],
                  "type":"phrase",
                  "slop":4
                }
              }],
           			"should": [{
                      "dis_max": {
                       "queries": [
                         {
                           "match": {
                             "lpi.paoStartNumber": {
                               "query": "7",
                               "boost": 0.5,
                               "fuzzy_transpositions": false,
                               "max_expansions": 10,
                               "prefix_length": "1"
                             }
                           }
                         },
                         {
                           "match": {
                             "lpi.saoStartNumber": {
                               "query": "7",
                               "boost": 0.2,
                               "fuzzy_transpositions": false,
                               "max_expansions": 10,
                               "prefix_length": "1"
                             }
                           }
                         }
                       ]
                     }
                   }],
              "filter":[{
                "terms":{
                  "classificationCode": ["RD"]
                }
              },{
                "bool":{
                  "must_not":[{
                    "term":{
                      "lpi.addressBasePostal":{
                        "value":"N"
                      }
                    }
                  }]
                }
              }]
            }
          }
        }

        """
      )

      // When
      val result = Json.parse(SearchBodyBuilderFn(repository.generateQueryPartialAddressRequest(partialInput, partialFilterCode, "", "", epoch = "")).string())

      // Then
      result shouldBe expected
    }


    "generate valid fallback query for search via partial endpoint - term with house number and exact filter" in {
      // Given
      val repository = new AddressIndexRepository(config, elasticClientProvider)

      val expected = Json.parse(
        """
        {
          "version":true,
          "query" : {
            "bool" : {
              "must" : [{
                "multi_match":{
                  "query":"7 Gate Ret",
                  "fields":["lpi.nagAll.partial","paf.mixedPaf.partial","paf.mixedWelshPaf.partial"],
                  "type":"best_fields"
                }
              }],
           			"should": [{
                      "dis_max": {
                       "queries": [
                         {
                           "match": {
                             "lpi.paoStartNumber": {
                               "query": "7",
                               "boost": 0.5,
                               "fuzzy_transpositions": false,
                               "max_expansions": 10,
                               "prefix_length": "1"
                             }
                           }
                         },
                         {
                           "match": {
                             "lpi.saoStartNumber": {
                               "query": "7",
                               "boost": 0.2,
                               "fuzzy_transpositions": false,
                               "max_expansions": 10,
                               "prefix_length": "1"
                             }
                           }
                         }
                       ]
                     }
                   }],
              "filter":[{
                "terms":{
                  "classificationCode": ["RD"]
                }
              },{
                "bool":{
                  "must_not":[{
                    "term":{
                      "lpi.addressBasePostal":{
                        "value":"N"
                      }
                    }
                  }]
                }
              }]
            }
          }
        }

        """
      )

      // When
      val result = Json.parse(SearchBodyBuilderFn(repository.generateQueryPartialAddressRequest(partialInputFallback, partialFilterCode, "", "", fallback = true, epoch = "")).string())

      // Then
      result shouldBe expected
    }

    "generate valid query for search via partial endpoint - term with house number and prefix filter" in {
      // Given
      val repository = new AddressIndexRepository(config, elasticClientProvider)

      val expected = Json.parse(
        """
        {
          "version":true,
          "query" : {
            "bool" : {
              "must" : [{
                "multi_match":{
                  "query":"7 Gate Re",
                  "fields":["lpi.nagAll.partial","paf.mixedPaf.partial","paf.mixedWelshPaf.partial"],
                  "type":"phrase",
                  "slop":4
                }
              }],
           			"should": [{
                      "dis_max": {
                       "queries": [
                         {
                           "match": {
                             "lpi.paoStartNumber": {
                               "query": "7",
                               "boost": 0.5,
                               "fuzzy_transpositions": false,
                               "max_expansions": 10,
                               "prefix_length": "1"
                             }
                           }
                         },
                         {
                           "match": {
                             "lpi.saoStartNumber": {
                               "query": "7",
                               "boost": 0.2,
                               "fuzzy_transpositions": false,
                               "max_expansions": 10,
                               "prefix_length": "1"
                             }
                           }
                         }
                       ]
                     }
                   }],
              "filter":[{
                "prefix":{
                  "classificationCode":{
                    "value":"R"
                  }
                }
              },{
                "bool":{
                  "must_not":[{
                    "term":{
                      "lpi.addressBasePostal":{
                        "value":"N"
                      }
                    }
                  }]
                }
              }]
            }
          }
        }

        """
      )

      // When
      val result = Json.parse(SearchBodyBuilderFn(repository.generateQueryPartialAddressRequest(partialInput, partialFilterPrefix, "", "", epoch = "")).string())

      // Then
      result shouldBe expected
    }

    "generate valid fallback query for search via partial endpoint - term with house number and prefix filter" in {
      // Given
      val repository = new AddressIndexRepository(config, elasticClientProvider)

      val expected = Json.parse(
        """
        {
          "version":true,
          "query" : {
            "bool" : {
              "must" : [{
                "multi_match":{
                  "query":"7 Gate Ret",
                  "fields":["lpi.nagAll.partial","paf.mixedPaf.partial","paf.mixedWelshPaf.partial"],
                  "type":"best_fields"
                }
              }],
          			"should": [{
                      "dis_max": {
                       "queries": [
                         {
                           "match": {
                             "lpi.paoStartNumber": {
                               "query": "7",
                               "boost": 0.5,
                               "fuzzy_transpositions": false,
                               "max_expansions": 10,
                               "prefix_length": "1"
                             }
                           }
                         },
                         {
                           "match": {
                             "lpi.saoStartNumber": {
                               "query": "7",
                               "boost": 0.2,
                               "fuzzy_transpositions": false,
                               "max_expansions": 10,
                               "prefix_length": "1"
                             }
                           }
                         }
                       ]
                     }
                   }],
              "filter":[{
                "prefix":{
                  "classificationCode":{
                    "value":"R"
                  }
                }
              },{
                "bool":{
                  "must_not":[{
                    "term":{
                      "lpi.addressBasePostal":{
                        "value":"N"
                      }
                    }
                  }]
                }
              }]
            }
          }
        }

        """
      )

      // When
      val result = Json.parse(SearchBodyBuilderFn(repository.generateQueryPartialAddressRequest(partialInputFallback, partialFilterPrefix, "", "", fallback = true, epoch = "")).string())

      // Then
      result shouldBe expected
    }

    "generate valid query for search via partial endpoint - term without house number and exact filter" in {
      // Given
      val repository = new AddressIndexRepository(config, elasticClientProvider)

      val expected = Json.parse(
        """
        {
          "version":true,
          "query" : {
            "bool" : {
              "must" : [{
                "multi_match":{
                  "query":"Gate Re",
                  "fields":["lpi.nagAll.partial","paf.mixedPaf.partial","paf.mixedWelshPaf.partial"],
                  "type":"phrase",
                  "slop":4
                }
              }],
              "filter":[{
                "terms":{
                  "classificationCode":["RD"]
                }
              },{
                "bool":{
                  "must_not":[{
                    "term":{
                      "lpi.addressBasePostal":{
                        "value":"N"
                      }
                    }
                  }]
                }
              }]
            }
          }
        }

        """
      )

      // When
      val result = Json.parse(SearchBodyBuilderFn(repository.generateQueryPartialAddressRequest(partialInputWithout, partialFilterCode, "", "", epoch = "")).string())

      // Then
      result shouldBe expected
    }

    "generate valid fallback query for search via partial endpoint - term without house number and exact filter" in {
      // Given
      val repository = new AddressIndexRepository(config, elasticClientProvider)

      val expected = Json.parse(
        """
        {
          "version":true,
          "query" : {
            "bool" : {
              "must" : [{
                "multi_match":{
                  "query":"Gate Ret",
                  "fields":["lpi.nagAll.partial","paf.mixedPaf.partial","paf.mixedWelshPaf.partial"],
                  "type":"best_fields"
                }
              }],
              "filter":[{
                "terms":{
                  "classificationCode": ["RD"]
                }
              },{
                "bool":{
                  "must_not":[{
                    "term":{
                      "lpi.addressBasePostal":{
                        "value":"N"
                      }
                    }
                  }]
                }
              }]
            }
          }
        }

        """
      )

      // When
      val result = Json.parse(SearchBodyBuilderFn(repository.generateQueryPartialAddressRequest(partialInputWithoutFallback, partialFilterCode, "", "", fallback = true, epoch = "")).string())

      // Then
      result shouldBe expected
    }

    "generate valid query for search via partial endpoint - term without house number and prefix filter" in {
      // Given
      val repository = new AddressIndexRepository(config, elasticClientProvider)

      val expected = Json.parse(
        """
        {
          "version":true,
          "query" : {
            "bool" : {
              "must" : [{
                "multi_match":{
                  "query":"Gate Re",
                  "fields":["lpi.nagAll.partial","paf.mixedPaf.partial","paf.mixedWelshPaf.partial"],
                  "type":"phrase",
                  "slop":4
                }
              }],
              "filter":[{
                "prefix":{
                  "classificationCode":{
                    "value":"R"
                  }
                }
              },{
                "bool":{
                  "must_not":[{
                    "term":{
                      "lpi.addressBasePostal":{
                        "value":"N"
                      }
                    }
                  }]
                }
              }]
            }
          }
        }

        """
      )

      // When
      val result = Json.parse(SearchBodyBuilderFn(repository.generateQueryPartialAddressRequest(partialInputWithout, partialFilterPrefix, "", "", epoch = "")).string())

      // Then
      result shouldBe expected
    }

    "generate valid fallback query for search via partial endpoint - term without house number and prefix filter" in {
      // Given
      val repository = new AddressIndexRepository(config, elasticClientProvider)

      val expected = Json.parse(
        """
        {
          "version":true,
          "query" : {
            "bool" : {
              "must" : [{
                "multi_match":{
                  "query":"Gate Ret",
                  "fields":["lpi.nagAll.partial","paf.mixedPaf.partial","paf.mixedWelshPaf.partial"],
                  "type":"best_fields"
                }
              }],
              "filter":[{
                "prefix":{
                  "classificationCode":{
                    "value":"R"
                  }
                }
              },{
                "bool":{
                  "must_not":[{
                    "term":{
                      "lpi.addressBasePostal":{
                        "value":"N"
                      }
                    }
                  }]
                }
              }]
            }
          }
        }

        """
      )

      // When
      val result = Json.parse(SearchBodyBuilderFn(repository.generateQueryPartialAddressRequest(partialInputWithoutFallback, partialFilterPrefix, "", "", fallback = true, epoch = "")).string())

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
                    "filter": [
                      {
                        "terms": {
                          "classificationCode": ["RD06"]
                        }
                      }
                    ],
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
                    "filter": [
                      {
                        "terms": {
                          "classificationCode": ["RD06"]
                        }
                      }
                    ],
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
      val result = Json.parse(SearchBodyBuilderFn(repository.generateQueryAddressRequest(tokens, filters, "", defaultLat, defaultLon, "", "", epoch = "")).string())

      // Then
      result shouldBe expected
    }


  }

}
