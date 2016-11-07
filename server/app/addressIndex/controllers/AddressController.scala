package addressIndex.controllers

import javax.inject.{Inject, Singleton}

import addressIndex.modules.ElasticsearchRepository
import play.api.Logger
import play.api.mvc.{Action, AnyContent}

import scala.concurrent.{ExecutionContext, Future}
import com.sksamuel.elastic4s.ElasticDsl._
import uk.gov.ons.addressIndex.model.AddressScheme._
import uk.gov.ons.addressIndex.model.PostcodeAddressFile

/**
  * Main API
  *
  * @param esRepo
  * @param ec
  */
@Singleton
class AddressController @Inject()(esRepo : ElasticsearchRepository)(implicit ec : ExecutionContext) extends AddressIndexController {

  val logger = Logger("address-index:AddressController")

  /**
    * Test elastic is connected
    * @return
    */
  def elasticTest() : Action[AnyContent] = Action async { implicit req =>
    esRepo.client execute {
      get cluster health
    } map { resp =>
      Ok(resp.toString)
    }
  }

  /**
    * Address query API
    *
    * @param format
    * @param input
    * @return
    */
  def addressQuery(
    input  : String,
    format : String
  ) : Action[AnyContent] = Action async {  implicit req =>
    logger info "#addressQuery called"
    Future successful NotImplemented
  }

  /**
    * UPRN query api
    *
    * @param uprn
    * @param formatString
    * @return
    */
  def uprnQuery(
    uprn   : String,
    formatString : String
  ) : Action[AnyContent] = Action async { implicit req =>
    logger info "#uprnQuery called"

    Future.fromTry(formatString.toAddressScheme()).map {
      case PostcodeAddressFile() => Ok("postcode address file")
    }
  }
}