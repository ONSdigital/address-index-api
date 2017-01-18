package uk.gov.ons.addressIndex.server.controllers

import com.sksamuel.elastic4s.ElasticDsl._
import play.api.mvc.{Action, AnyContent}
import javax.inject.{Inject, Singleton}
import uk.gov.ons.addressIndex.server.modules.ElasticsearchRepository
import scala.concurrent.ExecutionContext

@Singleton
class ElasticController @Inject()(esRepo: ElasticsearchRepository)(implicit ec: ExecutionContext)
  extends AddressIndexController {

  /**
    * Test elastic is connected
    *
    * @return
    */
  def clusterHealth(): Action[AnyContent] = Action async { implicit req =>
    esRepo.client execute {
      get cluster health
    } map { resp =>
      Ok(resp.toString)
    }
  }
}