package uk.gov.ons.addressIndex.server.controllers

import javax.inject.{Inject, Singleton}

import uk.gov.ons.addressIndex.server.modules.ElasticsearchRepository
import play.api.Logger
import play.api.mvc.{Action, AnyContent, Result}

import scala.concurrent.{ExecutionContext, Future}
import com.sksamuel.elastic4s.ElasticDsl._
import play.api.libs.json.Json
import uk.gov.ons.addressIndex.model.AddressScheme._
import uk.gov.ons.addressIndex.server.model.response.Implicits._
import uk.gov.ons.addressIndex.model.{BritishStandard7666, PostcodeAddressFile, UnsupportedScheme}
import uk.gov.ons.addressIndex.server.model.response._

import scala.util.matching.Regex

/**
  * Main API
  *
  * @param esRepo injected elastic dao
  * @param ec     execution context
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

    logger info s"#addressQuery parsed: postcode: ${tokens.postcode} , buildingNumber: ${tokens.buildingNumber}"

    format.stringToScheme() match {
      case PostcodeAddressFile(str) => searchPafAddresses(tokens)
      case BritishStandard7666(str) => searchUnsupportedFormatReply
      case UnsupportedScheme(str) => searchUnsupportedFormatReply
    }
  }


  private def searchPafAddresses(tokens: AddressTokens): Future[Result] = {
    esRepo.queryAddress(tokens).map { addresses =>
      Ok(Json.toJson(AddressBySearchResponseContainer(
        AddressResponse(
          tokens,
          addresses = addresses.map(AddressResponseAddress.fromPafAddress),
          limit = 10,
          offset = 0,
          total = addresses.size
        ),
        AddressResponseStatus.ok,
        errors = Seq()
      )))
    }
  }

  private val searchUnsupportedFormatReply: Future[Result] = Future.successful(BadRequest(Json.toJson(
    AddressBySearchResponseContainer(
      AddressResponse(
        AddressTokens.empty,
        addresses = Seq.empty,
        limit = 10,
        offset = 0,
        total = 0
      ),
      AddressResponseStatus.badRequest,
      errors = Seq(AddressResponseError.addressFormatNotSupported)
    )
  )))



  /**
    * UPRN query API
    *
    * @param uprn
    * @param format
    * @return
    */
  def uprnQuery(uprn: String, format: String): Action[AnyContent] = Action async { implicit req =>
    logger info s"#uprnQuery request called with uprn: $uprn , format: $format"
    format.stringToScheme() match {
      case PostcodeAddressFile(str) => searchPafAddressByUprn(uprn)
      case BritishStandard7666(str) => searchByUprnUnsupportedFormatReply
      case UnsupportedScheme(str) => searchByUprnUnsupportedFormatReply
    }
  }


  private def searchPafAddressByUprn(uprn: String): Future[Result] = {
    esRepo.queryUprn(uprn).map {
      case Some(address) => Ok(Json.toJson(
        AddressByUprnResponseContainer(
          address = Some(AddressResponseAddress.fromPafAddress(address)),
          AddressResponseStatus.ok,
          errors = Seq.empty
        )
      ))
      case None => NotFound(Json.toJson(
        AddressByUprnResponseContainer(
          address = None,
          AddressResponseStatus.notFound,
          errors = Seq(AddressResponseError.notFound)
        )
      ))
    }
  }

  private val searchByUprnUnsupportedFormatReply: Future[Result] = Future.successful(BadRequest(Json.toJson(
    AddressByUprnResponseContainer(
      address = None,
      AddressResponseStatus.badRequest,
      errors = Seq(AddressResponseError.addressFormatNotSupported)
    )
  )))
}