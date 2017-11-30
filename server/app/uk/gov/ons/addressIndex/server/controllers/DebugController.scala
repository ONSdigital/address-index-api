package uk.gov.ons.addressIndex.server.controllers

import javax.inject.Inject

import cats.Show
import com.sksamuel.elastic4s.searches.SearchDefinition
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, Controller}
import uk.gov.ons.addressIndex.server.modules.{AddressIndexRepository, ElasticsearchRepository, ParserModule}
import com.sksamuel.elastic4s.http.search.{SearchBodyBuilderFn, SearchImplicits}


import scala.concurrent.ExecutionContext

class DebugController@Inject()(
  esRepo: ElasticsearchRepository,
  parser: ParserModule
)(implicit ec: ExecutionContext) extends Controller {

  implicit object DebugShow extends Show[SearchDefinition]{
    override def show(req: SearchDefinition): String = SearchBodyBuilderFn(req).string()
  }


  /**
    * Test elastic is connected
    *
    * @return
    */
  def elasticTest(): Action[AnyContent] = Action async { implicit req =>
    esRepo.queryHealth().map { resp =>
      Ok(resp)
    }
  }

  /**
    * Outputs query that should be generated for a particular input
    * @param input input for which the query should be generated
    * @return query that is ought to be sent to Elastic (for debug purposes)
    */
  def queryDebug(input: String): Action[AnyContent] = Action { implicit req =>
    val tokens = parser.parse(input)
    val query = esRepo.generateQueryAddressRequest(tokens)
    val showQuery = DebugShow.show(query)
    Ok(Json.parse(showQuery))
  }

}
