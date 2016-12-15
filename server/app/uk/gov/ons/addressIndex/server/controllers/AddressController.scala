package uk.gov.ons.addressIndex.server.controllers

import javax.inject.{Inject, Singleton}
import uk.gov.ons.addressIndex.server.modules.{AddressIndexActions, AddressParserModule, ElasticsearchRepository}
import play.api.Logger
import play.api.mvc.{Action, AnyContent}
import scala.concurrent.ExecutionContext
import com.sksamuel.elastic4s.ElasticDsl._
import uk.gov.ons.addressIndex.crfscala.CrfScala.CrfTokenResult
import uk.gov.ons.addressIndex.model.server.response._
import uk.gov.ons.addressIndex.parsers.Implicits._

@Singleton
class AddressController @Inject()(
  override val esRepo: ElasticsearchRepository,
  parser: AddressParserModule
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
  def addressQuery(input: String, format: String): Action[AnyContent] = Action async { implicit req =>
    logger info s"#addressQuery: input $input , format: $format"
    input.toOption map { actualInput =>
      val tokens = parser tag actualInput
      logger info s"#addressQuery parsed: ${tokens.map(t => s"value: ${t.value} , label:${t.label}").mkString("\n")}"
      formatQuery[AddressBySearchResponseContainer, Seq[CrfTokenResult]](
        formatStr = format,
        pafInputForFn = AddressQueryInput(tokens),
        pafFn = pafSearch,
        nagInputForFn = AddressQueryInput(tokens),
        nagFn = nagSearch
      ) getOrElse futureJsonBadRequest(UnsupportedFormat)
    } getOrElse futureJsonBadRequest(EmptySearch)
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
      pafInputForFn = UprnQueryInput(uprn),
      pafFn = uprnPafSearch,
      nagInputForFn = UprnQueryInput(uprn),
      nagFn = uprnNagSearch
    ) getOrElse futureJsonBadRequest(UnsupportedFormatUprn)
  }
}