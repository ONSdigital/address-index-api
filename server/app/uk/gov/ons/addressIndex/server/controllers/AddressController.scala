package uk.gov.ons.addressIndex.server.controllers

import javax.inject.{Inject, Singleton}

import com.sksamuel.elastic4s.{HitAs, RichSearchHit}
import uk.gov.ons.addressIndex.server.modules.{AddressIndexActions, AddressParserModule, ElasticSearchRepository}
import play.api.Logger
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{Action, AnyContent}

import scala.concurrent.ExecutionContext
import uk.gov.ons.addressIndex.server.modules.AddressIndexConfigModule
import uk.gov.ons.addressIndex.parsers.Implicits._
import uk.gov.ons.addressIndex.server.modules.Model.Pagination

import scala.util.Try
import uk.gov.ons.addressIndex.model.AddressScheme._
import uk.gov.ons.addressIndex.model.db.index.HybridIndex
import uk.gov.ons.addressIndex.server.controllers.Model.HybridResponse


object Model {

  case class HybridResponses(
    responses: Array[HybridResponse]
  )

  object HybridResponses {
    implicit lazy val fmt = Json.format[HybridResponses]
  }

  case class HybridResponse(
    uprn: String,
    lpi: Option[Seq[Map[String, String]]],
    paf: Option[Seq[Map[String, String]]]
  )

  implicit object HybridResponse extends HitAs[HybridResponse] {
    override def as(hit: RichSearchHit): HybridResponse = {
      val map = hit.sourceAsMap
      HybridResponse(
        uprn = map(HybridIndex.Fields.uprn).toString,
        lpi = None,
        paf = None
      )
    }
  }
}


@Singleton
class AddressController @Inject()(
  override val esRepo: ElasticSearchRepository,
  parser: AddressParserModule,
  conf : AddressIndexConfigModule
)(implicit override val ec: ExecutionContext) extends AddressIndexController with AddressIndexActions {

  val logger = Logger("address-index-server:AddressController")

  /**
    *
    * @param uprn
    * @param format
    * @return
    */
  def uprnQuery(uprn: String, format: Option[String]): Action[AnyContent] = Action async { implicit req =>

    logger info s"#addressQuery:\nuprn $uprn , format: ${format.getOrElse("no format supplied")}"

    uprnSearch(
      uprn = uprn,
      format = format flatMap(_.stringToScheme)
    ) map(r => jsonOk(r.toString))
  }

  /**
    *
    * @param input
    * @param format
    * @param offset
    * @param limit
    * @return
    */
  def addressQuery(
    input: String,
    format: Option[String] = None,
    offset: Option[String] = None,
    limit: Option[String] = None
  ): Action[AnyContent] = Action async { implicit req =>

    logger info s"#addressQuery:\n" +
      s"input $input , format: $format , offset: ${offset.getOrElse("default")}, limit: ${limit.getOrElse("default")}"

    //get the defaults and maxima for the paging parameters from the config
    val defLimit = conf.config.elasticSearch.defaultLimit
    val defOffset = conf.config.elasticSearch.defaultOffset
    val maxLimit = conf.config.elasticSearch.maximumLimit
    val maxOffset = conf.config.elasticSearch.maximumOffset

    //TODO REMOVE This
    val limval = limit.getOrElse(defLimit.toString)
    val offval = offset.getOrElse(defOffset.toString)
    val limitInvalid = Try(limval.toInt).isFailure
    val offsetInvalid = Try(offval.toInt).isFailure
    val limitInt = Try(limval.toInt).toOption.getOrElse(defLimit)
    val offsetInt = Try(offval.toInt).toOption.getOrElse(defOffset)

    //Check the offset and limit parameters before proceeding with the request
    if (limitInvalid) {
      futureJsonBadRequest(LimitNotNumeric)
    } else if (limitInt < 1) {
      futureJsonBadRequest(LimitTooSmall)
    } else if (limitInt > maxLimit) {
      futureJsonBadRequest(LimitTooLarge)
    } else if (offsetInvalid) {
      futureJsonBadRequest(OffsetNotNumeric)
    } else if (offsetInt < 0) {
      futureJsonBadRequest(OffsetTooSmall)
    } else if (offsetInt > maxOffset) {
      futureJsonBadRequest(OffsetTooLarge)
    } else {
      //pagination passing
      val pagination = Pagination(
        offset = offsetInt,
        limit = limitInt
      )
      input.toOption map { actualInput =>
        val tokens = parser tag actualInput

        logger info s"#addressQuery parsed:" +
          s"\n${tokens.map(t => s"value: ${t.value} - label: ${t.label}").mkString("\n")}"

        addressSearch(
          input = AddressQueryInput(
            tokens = tokens,
            pagination = pagination
          ),
          format = format flatMap(_.stringToScheme)
        ) map { r =>
          jsonOk(r.as[HybridResponse])
        }
      } getOrElse futureJsonBadRequest(EmptySearch)
    }
  }
}