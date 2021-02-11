package uk.gov.ons.addressIndex.server.controllers

import com.sksamuel.elastic4s.Show
import com.sksamuel.elastic4s.requests.searches.SearchBodyBuilderFn
import com.sksamuel.elastic4s.requests.searches.SearchRequest
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

  implicit object DebugShow extends Show[SearchRequest] {
    override def show(req: SearchRequest): String = SearchBodyBuilderFn(req).string()
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
                 epoch: Option[String],
                 eboost: Option[String] = None,
                 nboost: Option[String] = None,
                 sboost: Option[String] = None,
                 wboost: Option[String] = None
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

    val eboostVal = {if (eboost.getOrElse("1.0").isEmpty) "1.0" else eboost.getOrElse("1.0")}
    val nboostVal = {if (nboost.getOrElse("1.0").isEmpty) "1.0" else nboost.getOrElse("1.0")}
    val sboostVal = {if (sboost.getOrElse("1.0").isEmpty) "1.0" else sboost.getOrElse("1.0")}
    val wboostVal = {if (wboost.getOrElse("1.0").isEmpty) "1.0" else wboost.getOrElse("1.0")}

    val eboostDouble = Try(eboostVal.toDouble).toOption.getOrElse(1.0D)
    val nboostDouble = Try(nboostVal.toDouble).toOption.getOrElse(1.0D)
    val sboostDouble = Try(sboostVal.toDouble).toOption.getOrElse(1.0D)
    val wboostDouble = Try(wboostVal.toDouble).toOption.getOrElse(1.0D)

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
      eboost = eboostDouble,
      nboost = nboostDouble,
      sboost = sboostDouble,
      wboost = wboostDouble,
      auth = req.headers.get("authorization").getOrElse("Anon")
    )

    val query = esRepo.makeQuery(args)
    val showQuery = DebugShow.show(query)
    Ok(Json.parse(showQuery))
  }

}
