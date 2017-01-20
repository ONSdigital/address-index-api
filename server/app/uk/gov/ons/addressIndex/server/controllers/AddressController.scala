package uk.gov.ons.addressIndex.server.controllers

import javax.inject.{Inject, Singleton}

import uk.gov.ons.addressIndex.server.modules.{AddressIndexActions, AddressParserModule, ElasticsearchRepository}
import play.api.Logger
import play.api.mvc.{Action, AnyContent}

import scala.concurrent.{ExecutionContext, Future}
import com.sksamuel.elastic4s.ElasticDsl._
import uk.gov.ons.addressIndex.crfscala.CrfScala.CrfTokenResult
import uk.gov.ons.addressIndex.server.modules.AddressIndexConfigModule
import uk.gov.ons.addressIndex.model.server.response._
import uk.gov.ons.addressIndex.parsers.Implicits._
import uk.gov.ons.addressIndex.model.AddressScheme._

import scala.io.Source
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
  def addressQuery(input: String, format: String, offset: Option[String] = None, limit: Option[String] = None): Action[AnyContent] = Action async { implicit req =>
    logger info s"#addressQuery:\ninput $input , format: $format , offset: ${offset.getOrElse("default")}, limit: ${limit.getOrElse("default")}"
   // get the defaults and maxima for the paging parameters from the config
    val defLimit = conf.config.elasticSearch.defaultLimit
    val defOffset = conf.config.elasticSearch.defaultOffset
    val maxLimit = conf.config.elasticSearch.maximumLimit
    val maxOffset = conf.config.elasticSearch.maximumOffset
// TODO Look at refactoring to use types
    val limval = limit.getOrElse(defLimit.toString())
    val offval = offset.getOrElse(defOffset.toString())
    val limitInvalid = Try(limval.toInt).isFailure
    val offsetInvalid = Try(offval.toInt).isFailure
    val limitInt = Try(limval.toInt).toOption.getOrElse(defLimit)
    val offsetInt = Try(offval.toInt).toOption.getOrElse(defOffset)
// Check the offset and limit parameters before proceeding with the request
    if (limitInvalid){
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
      input.toOption map { actualInput =>
        val tokens = parser tag actualInput
        logger info s"#addressQuery parsed:\n${tokens.map(t => s"value: ${t.value} , label:${t.label}").mkString("\n")}"
        formatQuery[AddressBySearchResponseContainer, Seq[CrfTokenResult]](
          formatStr = format,
          inputForPafFn = AddressQueryInput(tokens, offsetInt, limitInt),
          pafFn = pafSearch,
          inputForNagFn = AddressQueryInput(tokens, offsetInt, limitInt),
          nagFn = nagSearch
        ) getOrElse futureJsonBadRequest(UnsupportedFormat)
      } getOrElse futureJsonBadRequest(EmptySearch)
    }
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

  def bulkQuery(formatInput: String): Action[AnyContent] = Action.async { implicit req =>

    formatInput.stringToScheme().map { format =>

      val rawAddresses: Iterator[String] = Source.fromFile("/").getLines

      val tokenizedAddresses: Iterator[Seq[CrfTokenResult]] = rawAddresses.map(parser.tag)

      val addresses: Future[MultipleSearchResult] = multipleSearch(tokenizedAddresses, format)

      addresses.map(result => Ok(s"${result.successfulAddresses.size}, ${result.failedAddresses.size}"))

    }.getOrElse(futureJsonBadRequest(UnsupportedFormatUprn))
  }
}
