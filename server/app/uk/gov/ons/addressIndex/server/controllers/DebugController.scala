package uk.gov.ons.addressIndex.server.controllers

import cats.Show
import com.sksamuel.elastic4s.http.search.SearchBodyBuilderFn
import com.sksamuel.elastic4s.searches.SearchDefinition
import javax.inject.Inject
import play.api.libs.json.Json
import play.api.mvc._
import uk.gov.ons.addressIndex.server.modules._

import scala.concurrent.ExecutionContext
import scala.util.Try

class DebugController @Inject()(val controllerComponents: ControllerComponents,
                                esRepo: ElasticsearchRepository,
                                parser: ParserModule
                               )(implicit ec: ExecutionContext) extends BaseController {

  implicit object DebugShow extends Show[SearchDefinition] {
    override def show(req: SearchDefinition): String = SearchBodyBuilderFn(req).string()
  }


  /**
    * Test elastic is connected
    *
    * @return
    */
  def elasticTest(): Action[AnyContent] = Action async { implicit req =>
    esRepo.queryHealth().map { resp =>
      val output = "{" + resp.split("\\{")(1).split("\\}")(0) + "}"
      Ok(Json.parse(output))
    }
  }

  /**
    * Outputs query that should be generated for a particular input
    *
    * @param input input for which the query should be generated
    * @return query that is ought to be sent to Elastic (for debug purposes)
    */
  def queryDebug(input: String,
                 classificationfilter: Option[String] = None,
                 rangekm: Option[String] = None,
                 lat: Option[String] = None,
                 lon: Option[String] = None,
                 historical: Option[String] = None,
                 epoch: Option[String]
                ): Action[AnyContent] = Action { implicit req =>
    val tokens = parser.parse(input)

    val filterString = classificationfilter.getOrElse("")
    val rangeString = rangekm.getOrElse("")
    val latString = lat.getOrElse("50.705948")
    val lonString = lon.getOrElse("-3.5091076")

    val startDateVal = ""
    val endDateVal = ""

    val hist = historical.flatMap(x => Try(x.toBoolean).toOption).getOrElse(true)

    val epochVal = epoch.getOrElse("")

    val args = AddressArgs(
      input = "",
      tokens = tokens,
      region = Region.fromStrings(rangeString, latString, lonString),
      epoch = epochVal,
      verbose = false,
      historical = hist,
      filters = filterString,
      filterDateRange = DateRange(startDateVal, endDateVal),
      limit = 0,
      queryParamsConfig = None,
    )

    val query = esRepo.makeQuery(args)
    val showQuery = DebugShow.show(query)
    Ok(Json.parse(showQuery))
  }

}
