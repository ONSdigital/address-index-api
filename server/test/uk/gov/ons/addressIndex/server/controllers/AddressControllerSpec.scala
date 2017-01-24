package uk.gov.ons.addressIndex.server.controllers

import javax.inject.Inject
import uk.gov.ons.addressIndex.server.modules.{AddressIndexCannedResponse, AddressParserModule, ElasticSearchRepository}
import com.sksamuel.elastic4s.{ElasticClient, RichSearchResponse}
import org.elasticsearch.common.settings.Settings
import play.api.mvc.Results
import uk.gov.ons.addressIndex.model.db.index.{NationalAddressGazetteer, PostcodeAddressFile}
import org.scalatestplus.play._
import uk.gov.ons.addressIndex.crfscala.CrfScala.CrfTokenResult
import uk.gov.ons.addressIndex.model.AddressScheme
import uk.gov.ons.addressIndex.server.modules.AddressIndexConfigModule
import uk.gov.ons.addressIndex.server.modules.Model.Pagination
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class AddressControllerSpec @Inject()(conf: AddressIndexConfigModule)
  extends PlaySpec with Results with AddressIndexCannedResponse  {

  val validPafAddress = PostcodeAddressFile(
    recordIdentifier = "1",
    changeType = "2",
    proOrder = "3",
    uprn = "4",
    udprn = "5",
    organizationName = "6",
    departmentName = "7",
    subBuildingName = "8",
    buildingName = "9",
    buildingNumber = "10",
    dependentThoroughfare = "11",
    thoroughfare = "12",
    doubleDependentLocality = "13",
    dependentLocality = "14",
    postTown = "15",
    postcode = "B16 8TH",
    postcodeType = "17",
    deliveryPointSuffix = "18",
    welshDependentThoroughfare = "19",
    welshThoroughfare = "20",
    welshDoubleDependentLocality = "21",
    welshDependentLocality = "22",
    welshPostTown = "23",
    poBoxNumber = "24",
    processDate = "25",
    startDate = "26",
    endDate = "27",
    lastUpdateDate = "28",
    entryDate = "29"
  )

  val validNagAddress = NationalAddressGazetteer(
    uprn = "1",
    postcodeLocator = "B16 8TH",
    addressBasePostal = "3",
    latitude = "24",
    longitude = "25",
    easting = "27",
    northing = "28",
    organisation = "22",
    legalName = "23",
    classificationCode = "29",
    usrn = "4",
    lpiKey = "5",
    paoText = "6",
    paoStartNumber = "72",
    paoStartSuffix = "8",
    paoEndNumber = "9",
    paoEndSuffix = "10",
    saoText = "11",
    saoStartNumber = "12",
    saoStartSuffix = "13",
    saoEndNumber = "14",
    saoEndSuffix = "15",
    level = "16",
    officialFlag = "17",
    logicalStatus = "18",
    streetDescriptor = "19",
    townName = "20",
    locality = "21"
  )
//
//  // injected value, change implementations accordingly when needed
//  // mock that will return one address as a result
//  val elasticRepositoryMock = new ElasticSearchRepository {
//
//    override def client(): ElasticClient = ElasticClient.local(Settings.builder().build())
//
//    override def queryAddress(tokens: Seq[CrfTokenResult])
//      (implicit p: Pagination, fmt: Option[AddressScheme]): Future[RichSearchResponse] = {
//      Future.successful(null)
//    }
//
//    override def queryUprn(uprn: String)
//      (implicit fmt: Option[AddressScheme]): Future[RichSearchResponse] = {
//      Future.successful(null)
//    }
//  }
//
//  // mock that won't return any addresses
//  val emptyElasticRepositoryMock = new ElasticSearchRepository {
//
//    override def client(): ElasticClient = ElasticClient.local(Settings.builder().build())
//
//    override def queryAddress(tokens: Seq[CrfTokenResult])
//      (implicit p: Pagination, fmt: Option[AddressScheme]): Future[RichSearchResponse] = {
//      Future.successful(null)
//    }
//
//    override def queryUprn(uprn: String)(implicit fmt: Option[AddressScheme]): Future[RichSearchResponse] = {
//      Future.successful(null)
//    }
////  }
//
//  val parser = new AddressParserModule
//
//  def testController: AddressController = new AddressController(elasticRepositoryMock, parser, conf)

  "Address controller" should {

    "reply with a found PAF address (by address query)" ignore {

    }

    "reply with a found NAG address (by address query)" ignore {

    }

    "reply on a 400 error if address format is not supported (by address query)" ignore {

    }

    "reply on a 400 error if a non-numeric offset parameter is supplied" ignore {

    }

    "reply on a 400 error if a non-numeric limit parameter is supplied" ignore {

    }

    "reply on a 400 error if a negative offset parameter is supplied" ignore {

    }

    "reply on a 400 error if a negative or zero limit parameter is supplied" ignore {

    }

    "reply on a 400 error if an offset parameter greater than the maximum allowed is supplied" ignore {

    }

    "reply on a 400 error if a limit parameter larger than the maximum allowed is supplied" ignore {

    }

    "reply on a 400 error if query is empty (by address query)" ignore {

    }

    "reply on a found PAF address (by uprn)" ignore {

    }

    "reply on a found NAG address (by uprn)" ignore {

    }

    "reply a 404 error if address was not found (by uprn)" ignore {

    }

    "reply a 400 error if address format is not supported (by uprn)" ignore {

    }

  }
}
