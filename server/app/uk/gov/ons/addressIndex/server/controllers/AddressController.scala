package uk.gov.ons.addressIndex.server.controllers

import java.util
import javax.inject.{Inject, Singleton}

import com.sksamuel.elastic4s.{HitAs, RichSearchHit}
import uk.gov.ons.addressIndex.server.modules.{AddressIndexActions, AddressParserModule, ElasticSearchRepository}
import play.api.Logger
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent}

import scala.concurrent.ExecutionContext
import uk.gov.ons.addressIndex.server.modules.AddressIndexConfigModule
import uk.gov.ons.addressIndex.parsers.Implicits._
import uk.gov.ons.addressIndex.server.modules.Model.Pagination

import scala.util.Try
import uk.gov.ons.addressIndex.model.AddressScheme._
import uk.gov.ons.addressIndex.model.db.index.HybridIndex
import uk.gov.ons.addressIndex.server.controllers.Model.HybridResponse
//import uk.gov.ons.addressIndex.server.controllers.Model.HybridResponses

object Model {


//  case class HybridResponses(documents: Seq[HybridResponse])
//
//  implicit object HybridResponses extends HitAs[HybridResponses] {
//
////    implicit lazy val fmt = Json.format[HybridResponses]
//
//    override def as(hit: RichSearchHit): HybridResponses = {
//      HybridResponses(
//        documents = hit.sourceAsMap
//      )
//    }
//  }

  case class HybridResponse(
    uprn: String,
    lpi: Option[Seq[Map[String, String]]],
    paf: Option[Seq[Map[String, String]]]
  )

  implicit object HybridResponse extends HitAs[HybridResponse] {
    import scala.collection.JavaConverters._

    implicit lazy val fmt = Json.format[HybridResponse]

    override def as(hit: RichSearchHit): HybridResponse = {
      val map = hit.sourceAsMap

      def getSeqMap(fieldName: String): Seq[Map[String, String]] = {
        val x = map(fieldName).asInstanceOf[util.ArrayList[java.util.HashMap[String, String]]].asScala
        x.map(_.asScala.toMap)
      }

      HybridResponse(
        uprn = map(HybridIndex.Fields.uprn).toString,
        lpi = Some(getSeqMap(HybridIndex.Fields.lpi)),
        paf = Some(getSeqMap(HybridIndex.Fields.paf))
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
//          println("r.toString" + r.toString)
//          println("r.hits.toString")
//          pprint.pprintln(r.hits.map(_.sourceAsMap))

            r.as[HybridResponse]


//          jsonOk(r.as[HybridResponses].toSeq)
          jsonOk(r.as[HybridResponse])
        }
      } getOrElse futureJsonBadRequest(EmptySearch)
    }
  }
}