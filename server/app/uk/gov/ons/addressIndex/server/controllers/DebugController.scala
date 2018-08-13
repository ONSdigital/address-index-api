package uk.gov.ons.addressIndex.server.controllers

import javax.inject.Inject

import cats.Show
import com.sksamuel.elastic4s.searches.SearchDefinition
import play.api.libs.json.Json
import play.api.mvc._
import uk.gov.ons.addressIndex.server.modules.{ElasticsearchRepository, ParserModule}
import com.sksamuel.elastic4s.http.search.SearchBodyBuilderFn

import scala.concurrent.ExecutionContext
import scala.util.Try

class DebugController@Inject()(val controllerComponents: ControllerComponents,
  esRepo: ElasticsearchRepository,
  parser: ParserModule
)(implicit ec: ExecutionContext) extends BaseController {

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
  def queryDebug(input: String, classificationfilter: Option[String] = None, rangekm: Option[String] = None, lat: Option[String] = None, lon: Option[String] = None, historical: Option[String] = None): Action[AnyContent] = Action { implicit req =>
    val tokens = parser.parse(input)

    val filterString = classificationfilter.getOrElse("")
    val rangeString = rangekm.getOrElse("")
    val latString = lat.getOrElse("50.705948")
    val lonString = lon.getOrElse("-3.5091076")

    val hist = historical match {
      case Some(x) => Try(x.toBoolean).getOrElse(true)
      case None => true
    }

    val query = esRepo.generateQueryAddressRequest(tokens,filterString,rangeString,latString,lonString, None, hist)
    val showQuery = DebugShow.show(query)
    Ok(Json.parse(showQuery))
  }

}
