package uk.gov.ons.addressIndex.server.controllers

import javax.inject.{Inject, Singleton}
import uk.gov.ons.addressIndex.server.modules.{AddressActions, AddressParserModule, ElasticsearchRepository}
import play.api.Logger
import play.api.mvc.{Action, AnyContent}
import scala.concurrent.ExecutionContext
import com.sksamuel.elastic4s.ElasticDsl._
import uk.gov.ons.addressIndex.model.AddressScheme._
import uk.gov.ons.addressIndex.model.{BritishStandard7666, PostcodeAddressFile}
import uk.gov.ons.addressIndex.model.server.response._
import uk.gov.ons.addressIndex.parsers.Implicits._

@Singleton
class AddressController @Inject()(
  override val esRepo: ElasticsearchRepository,
  parser: AddressParserModule
)(implicit ec: ExecutionContext) extends AddressIndexController with AddressActions {

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
      val futureResp = format.stringToScheme map {
        case PostcodeAddressFile(_) => pafSearch(tokens)
        case BritishStandard7666(_) => nagSearch(tokens)
      }
      futureResp map(_.map(jsonOk[AddressBySearchResponseContainer])) getOrElse futureJsonBadRequest(UnsupportedFormat)
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
    val futureResp = format.stringToScheme map {
      case PostcodeAddressFile(_) => uprnPafSearch(uprn)
      case BritishStandard7666(_) => uprnNagSearch(uprn)
    }
    futureResp map(_.map(jsonOk[AddressByUprnResponseContainer])) getOrElse futureJsonBadRequest(UnsupportedFormatUprn)
  }
}