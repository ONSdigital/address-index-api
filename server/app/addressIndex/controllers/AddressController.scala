package addressIndex.controllers

import javax.inject.{Inject, Singleton}
import addressIndex.modules.ElasticsearchRepository
import play.api.Logger
import play.api.mvc.{Action, AnyContent}
import scala.concurrent.{ExecutionContext, Future}
import com.sksamuel.elastic4s.ElasticDsl._

@Singleton
class AddressController @Inject()(esRepo : ElasticsearchRepository)(implicit ec : ExecutionContext) extends AddressIndexController {

  def elasticTest(): Action[AnyContent] = Action async { implicit req =>
    esRepo.client execute {
      get cluster health
    } map { resp =>
      Ok(resp.toString)
    }
  }

  def addressQuery(
    format : String,
    input  : String
  ): Action[AnyContent] = Action async { implicit req =>
    Logger("address-index:AddressController").info("#addressQuery called")
    Future successful Ok
  }

  def uprnQuery(
    uprn   : String,
    format : String
  ): Action[AnyContent] = Action async { implicit req =>
    Logger("address-index:AddressController").info("#uprnQuery called")
    Future successful Ok
  }
}