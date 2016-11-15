package uk.gov.ons.addressIndex.server.controllers

import javax.inject.{Inject, Singleton}
import uk.gov.ons.addressIndex.server.modules.ElasticsearchRepository
import play.api.Logger
import play.api.mvc.{Action, AnyContent}
import scala.concurrent.{ExecutionContext, Future}
import com.sksamuel.elastic4s.ElasticDsl._

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
    format : String,
    input  : String
  ) : Action[AnyContent] = Action async {  implicit req =>
    logger info "#addressQuery called"
    Future successful NotImplemented
  }

  /**
    * UPRN query api
    *
    * @param uprn
    * @param format
    * @return
    */
  def uprnQuery(
    uprn   : String,
    format : String
  ) : Action[AnyContent] = Action async { implicit req =>
    logger info "#uprnQuery called"
    //dummy data
    val map : Map[(String, String), String] = Map(
      "paf" -> "XXX" -> "SomeAddress"
    )
    Future successful Ok(
      map getOrElse((format, uprn), s"UPRN '$uprn' for format '$format' not Found")
    )
  }
}