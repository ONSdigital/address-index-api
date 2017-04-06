package uk.gov.ons.addressIndex.server.controllers

import javax.inject.Inject

import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, Controller}
import uk.gov.ons.addressIndex.parsers.Tokens
import uk.gov.ons.addressIndex.server.modules.{ElasticsearchRepository, ParserModule}

import scala.concurrent.ExecutionContext

class DebugController@Inject()(
  esRepo: ElasticsearchRepository,
  parser: ParserModule
)(implicit ec: ExecutionContext) extends Controller {


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
    val tokens = Tokens.postTokenizeTreatment(parser.tag(input))
    val query = esRepo.generateQueryAddressRequest(tokens).toString()
    Ok(Json.parse(query))
  }

}
