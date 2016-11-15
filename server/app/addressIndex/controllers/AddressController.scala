package addressIndex.controllers

import javax.inject.{Inject, Singleton}
import addressIndex.model.response.PostcodeAddressFileReplyUnit
import addressIndex.modules.ElasticsearchRepository
import play.api.Logger
import play.api.mvc.{Action, AnyContent, Result}
import scala.concurrent.{ExecutionContext, Future}
import com.sksamuel.elastic4s.ElasticDsl._
import play.api.libs.json.Json
import uk.gov.ons.addressIndex.model.AddressScheme._
import uk.gov.ons.addressIndex.model.PostcodeAddressFile
import uk.gov.ons.addressIndex.model.db.index.PostcodeAddressFileAddress
import scala.util.matching.Regex

/**
  * Main API
  *
  * @param esRepo
  * @param ec
  */
@Singleton
class AddressController @Inject()(esRepo : ElasticsearchRepository)(implicit ec : ExecutionContext) extends AddressIndexController {

  val logger = Logger("address-index-server:AddressController")

  /**
    * Test elastic is connected
    * @return
    */
  def elasticTest() : Action[AnyContent] = Action async { implicit req =>
    esRepo.client execute {
      get cluster health
    } map { resp =>
      Ok(resp.toString)
    }
  }

  /**
    * Address query API
    *
    * @param format
    * @param input
    * @return
    */
  def addressQuery(
    input : String,
    format : String
  ) : Action[AnyContent] = Action async {  implicit req =>
    logger info s"#addressQuery called with input $input , format: $format"

    val regex : Regex = "(?:[A-Za-z]\\d ?\\d[A-Za-z]{2})|(?:[A-Za-z][A-Za-z\\d]\\d ?\\d[A-Za-z]{2})|(?:[A-Za-z]{2}\\d{2} ?\\d[A-Za-z]{2})|(?:[A-Za-z]\\d[A-Za-z] ?\\d[A-Za-z]{2})|(?:[A-Za-z]{2}\\d[A-Za-z] ?\\d[A-Za-z]{2})".r
    val postcode : String = regex findFirstIn input getOrElse "Not recognised"
    val buildingNumber : Int = input substring(0, 1) toInt

    logger info s"#addressQuery parsed: postcode: $postcode , buildingNumber: $buildingNumber"

    Future fromTry format.toAddressScheme flatMap {
      case PostcodeAddressFile() => esRepo.queryAddress(buildingNumber, postcode) map addressResponse
      case _ => asyncWrongFormatResponse
    }
  }

  /**
    * UPRN query API
    *
    * @param uprn
    * @param format
    * @return
    */
  def uprnQuery(
    uprn : String,
    format : String
  ) : Action[AnyContent] = Action async { implicit req =>
    logger info s"#uprnQuery called with uprn: $uprn , format: $format"
    Future fromTry format.toAddressScheme flatMap {
      case PostcodeAddressFile() => esRepo queryUprn uprn map addressResponse
      case _ => asyncWrongFormatResponse
    }
  }

  private def addressResponse(addresses : Seq[PostcodeAddressFileAddress]) : Result = {
    logger info "#addressQuery got a response from es"
    Ok(
      Json toJson(
        addresses map PostcodeAddressFileReplyUnit.fromPostcodeAddressFileAddress
      )
    )
  }

  private val asyncWrongFormatResponse : Future[Result] = Future successful Ok("Wrong format")
}