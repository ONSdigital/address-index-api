package uk.gov.ons.addressIndex.server.controllers

import javax.inject.{Inject, Singleton}
import uk.gov.ons.addressIndex.server.modules.{AddressIndexActions, AddressParserModule, ElasticsearchRepository}
import play.api.Logger
import play.api.mvc.{Action, AnyContent}
import scala.concurrent.ExecutionContext
import com.sksamuel.elastic4s.ElasticDsl._
import uk.gov.ons.addressIndex.crfscala.CrfScala.CrfTokenResult
import uk.gov.ons.addressIndex.server.modules.AddressIndexConfigModule
import uk.gov.ons.addressIndex.model.server.response._
import uk.gov.ons.addressIndex.parsers.Implicits._
import uk.gov.ons.addressIndex.server.modules.Model.Pagination
import scala.util.Try

@Singleton
class AddressController @Inject()(
  override val esRepo: ElasticsearchRepository,
  parser: AddressParserModule,
  conf : AddressIndexConfigModule
)(implicit override val ec: ExecutionContext) extends AddressIndexController with AddressIndexActions {

  val logger = Logger("address-index-server:AddressController")

  /**
    * Test elastic is connected
    *
    * @return
    */
  def elasticTest(): Action[AnyContent] = Action async { implicit req =>
    esRepo.client execute {
      get cluster health
    } map { resp =>
      Ok(resp.toString)
    }
  }

  /**
    * Address query API
    *
    * @param input  the address query
    * @param format requested format of the query (paf/nag)
    * @return Json response with addresses information
    */
  def addressQuery(
    input: String,
    format: Option[String] = None,
    offset: Option[String] = None,
    limit: Option[String] = None
  ): Action[AnyContent] = Action async { implicit req =>

    logger info s"#addressQuery:\n" +
      s"input $input , format: $format , offset: ${offset.getOrElse("default")}, limit: ${limit.getOrElse("default")}"

   // get the defaults and maxima for the paging parameters from the config
    val defLimit = conf.config.elasticSearch.defaultLimit
    val defOffset = conf.config.elasticSearch.defaultOffset
    val maxLimit = conf.config.elasticSearch.maximumLimit
    val maxOffset = conf.config.elasticSearch.maximumOffset

// TODO Look at refactoring to use types
    val limval = limit.getOrElse(defLimit.toString)
    val offval = offset.getOrElse(defOffset.toString)
    val limitInvalid = Try(limval.toInt).isFailure
    val offsetInvalid = Try(offval.toInt).isFailure
    val limitInt = Try(limval.toInt).toOption.getOrElse(defLimit)
    val offsetInt = Try(offval.toInt).toOption.getOrElse(defOffset)
// Check the offset and limit parameters before proceeding with the request
    if (limitInvalid) {
      futureJsonBadRequest(LimitNotNumeric)
    } else if (limitInt < 1) {
      futureJsonBadRequest(LimitTooSmall)
    } else if (limitInt > maxLimit) {
      futureJsonBadRequest(LimitTooLarge)
    } else if (offsetInvalid){
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
        logger info s"#addressQuery parsed:\n${tokens.map(t => s"value: ${t.value} - label:${t.label}").mkString("\n")}"

        val input = AddressQueryInput(
          tokens = tokens,
          pagination = pagination
        )

        format.map { formatStr =>
          formatQuery[AddressBySearchResponseContainer, Seq[CrfTokenResult]](
            formatStr = formatStr,
            inputForPafFn = input,
            pafFn = pafSearch,
            inputForNagFn = input,
            nagFn = nagSearch
          ).getOrElse(futureJsonBadRequest(UnsupportedFormat))
        } getOrElse {
//          hybridSearch(
//            input = input
//          ).map(
//            _.map(x => jsonOk(x))
//          )
          futureJsonBadRequest(EmptySearch)
        }
      } getOrElse futureJsonBadRequest(EmptySearch)
    }
    //todo rmeove below
    futureJsonBadRequest(EmptySearch)
  }

  /**
    * UPRN query API
    *
    * @param uprn
    * @param format
    * @return
    */
  def uprnQuery(uprn: String, format: String): Action[AnyContent] = Action async { implicit req =>
    logger info s"#uprnQuery: uprn: $uprn , format: $format"
    formatQuery[AddressByUprnResponseContainer, String](
      formatStr = format,
      inputForPafFn = UprnQueryInput(uprn),
      pafFn = uprnPafSearch,
      inputForNagFn = UprnQueryInput(uprn),
      nagFn = uprnNagSearch
    ) getOrElse futureJsonBadRequest(UnsupportedFormatUprn)
  }
}
