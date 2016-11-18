package uk.gov.ons.addressIndex.server.controllers

import javax.inject.{Inject, Singleton}

import uk.gov.ons.addressIndex.server.modules.ElasticsearchRepository
import play.api.Logger
import play.api.mvc.{Action, AnyContent, Result}
import scala.concurrent.{ExecutionContext, Future}
import com.sksamuel.elastic4s.ElasticDsl._
import play.api.libs.json.Json
import uk.gov.ons.addressIndex.model.AddressScheme._
import uk.gov.ons.addressIndex.server.model.response.implicits._
import uk.gov.ons.addressIndex.model.PostcodeAddressFile
import uk.gov.ons.addressIndex.model.db.index.PostcodeAddressFileAddress
import uk.gov.ons.addressIndex.server.model.response._

import scala.util.matching.Regex

/**
  * Main API
  *
  * @param esRepo injected elastic dao
  * @param ec execution context
  */
@Singleton
class AddressController @Inject()(esRepo: ElasticsearchRepository)(implicit ec: ExecutionContext) extends AddressIndexController {

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
    * @return
    */
  def addressQuery(input: String, format: String): Action[AnyContent] = Action async { implicit req =>
    logger info s"#addressQuery called with input $input , format: $format"

    val regex: Regex = ("(?:[A-Za-z]\\d ?\\d[A-Za-z]{2})|(?:[A-Za-z][A-Za-z\\d]\\d ?\\d[A-Za-z]{2})|" +
      "(?:[A-Za-z]{2}\\d{2} ?\\d[A-Za-z]{2})|(?:[A-Za-z]\\d[A-Za-z] ?\\d[A-Za-z]{2})|" +
      "(?:[A-Za-z]{2}\\d[A-Za-z] ?\\d[A-Za-z]{2})").r
    val tokens = AddressTokens(
      uprn = "",
      buildingNumber = input.substring(0, 2),
      postcode = regex.findFirstIn(input).getOrElse("Not recognised")
    )
    println(tokens)

    logger info s"#addressQuery parsed: postcode: ${tokens.postcode} , buildingNumber: ${tokens.buildingNumber}"

    Future.fromTry(format.toAddressScheme()).flatMap {
      case PostcodeAddressFile(str) => esRepo.queryAddress(tokens).map {
        addresses => addressResponse(tokens, addresses)
      }
      case _ => asyncWrongFormatResponse
    }
  }

  private def addressResponse(tokens: AddressTokens, addresses: Seq[PostcodeAddressFileAddress]): Result = {
    val response = AddressBySearchResponseContainer(
      AddressResponse(
        tokens,
        addresses = addresses.map(AddressResponseAddress.fromPafAddress),
        limit = 10,
        offset = 0,
        total = addresses.size
      ),
      AddressResponseStatus(
        code = 200,
        message = "Ok"
      ),
      errors = Seq()
    )
    Ok(Json.toJson(response))
  }

  /**
    * UPRN query API
    *
    * @param uprn
    * @param format
    * @return
    */
  def uprnQuery(uprn: String, format: String): Action[AnyContent] = Action async { implicit req =>
    logger info s"#uprnQuery request called with uprn: $uprn , format: $format"
    Future.fromTry(format.toAddressScheme()).flatMap {
      case PostcodeAddressFile(str) => esRepo.queryUprn(uprn).map {
        case Some(address) => Ok(Json.toJson(
          AddressByUprnResponseContainer(
           address = AddressResponseAddress.fromPafAddress(address),
            AddressResponseStatus(
              code = 200,
              message = "Ok"
            ),
            errors = Seq()
          )
        ))
        case None => NotFound("Not found")
      }
      case _ => asyncWrongFormatResponse
    }
  }


  private val asyncWrongFormatResponse: Future[Result] = Future.successful(Ok("Wrong format"))
}