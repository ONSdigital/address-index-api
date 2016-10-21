package addressIndex.controllers

import javax.inject.{Inject, Singleton}
import addressIndex.modules.ElasticsearchRepository
import play.api.Logger
import play.api.mvc.{Action, AnyContent}
import scala.concurrent.{ExecutionContext, Future}
import com.sksamuel.elastic4s.ElasticDsl._

@Singleton
class AddressController @Inject()(esRepo : ElasticsearchRepository)(implicit ec : ExecutionContext) extends AddressIndexController {

  val logger = Logger("address-index:AddressController")

  def elasticTest() : Action[AnyContent] = Action async { implicit req =>
//
//    esRepo.destroyAddressIndex flatMap { _ =>
//      esRepo.createAddressIndex map { resp =>
//        Ok(resp.toString)
//      }
//    }
    esRepo.client execute {
      get cluster health
    } map { resp =>
      Ok(resp.toString)
    }
  }

  def addressQuery(
    format : String,
    input  : String
  ) : Action[AnyContent] = Action async {  implicit req =>
    logger info "#addressQuery called"
    Future successful NotImplemented
  }

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