package uk.gov.ons.addressIndex.server.controllers

import javax.inject.{Inject, Singleton}

import uk.gov.ons.addressIndex.server.modules.ElasticsearchRepository
import play.api.Logger
import play.api.mvc.{Action, AnyContent, Result}

import scala.concurrent.{ExecutionContext, Future}
import com.sksamuel.elastic4s.ElasticDsl._
import play.api.libs.json.Json
import uk.gov.ons.addressIndex.model.AddressScheme._
import uk.gov.ons.addressIndex.model.db.index.{NationalAddressGazetteerAddresses, PostcodeAddressFileAddresses}
import uk.gov.ons.addressIndex.model.{BritishStandard7666, PostcodeAddressFile}
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

    if (input == "") searchEmptyQueryReply
    else {
      val regex: Regex = ("(?:[A-Za-z]\\d ?\\d[A-Za-z]{2})|(?:[A-Za-z][A-Za-z\\d]\\d ?\\d[A-Za-z]{2})|" +
        "(?:[A-Za-z]{2}\\d{2} ?\\d[A-Za-z]{2})|(?:[A-Za-z]\\d[A-Za-z] ?\\d[A-Za-z]{2})|" +
        "(?:[A-Za-z]{2}\\d[A-Za-z] ?\\d[A-Za-z]{2})").r
      val tokens = AddressTokens(
        uprn = "",
        buildingNumber = input.substring(0, 2),
        postcode = regex.findFirstIn(input).getOrElse("Not recognised")
      )

      logger info s"#addressQuery parsed: postcode: ${tokens.postcode} , buildingNumber: ${tokens.buildingNumber}"

      format.stringToScheme().map {
        case PostcodeAddressFile(_) => searchPafAddresses(tokens)
        case BritishStandard7666(_) => searchNagAddresses(tokens)
      }.getOrElse(searchUnsupportedFormatReply)
    }

  }


  private def searchPafAddresses(tokens: AddressTokens): Future[Result] = {
    esRepo.queryPafAddresses(tokens).map {
      case PostcodeAddressFileAddresses(addresses, maxScore) => Ok(Json.toJson(
        AddressBySearchResponseContainer(
          response = AddressBySearchResponse(
            tokens = tokens,
            addresses = addresses.map(AddressResponseAddress.fromPafAddress(maxScore)),
            limit = 10,
            offset = 0,
            total = addresses.size
          ),
          status = OkAddressResponseStatus
        )
      ))
    }
  }

  private def searchNagAddresses(tokens: AddressTokens): Future[Result] = {
    esRepo.queryNagAddresses(tokens).map {
      case NationalAddressGazetteerAddresses(addresses, maxScore) => Ok(Json.toJson(
        AddressBySearchResponseContainer(
          response = AddressBySearchResponse(
            tokens = tokens,
            addresses = addresses.map(AddressResponseAddress.fromNagAddress(maxScore)),
            limit = 10,
            offset = 0,
            total = addresses.size
          ),
          status = OkAddressResponseStatus
        )
      ))
    }
  }

  private val errorAddressResponse = AddressBySearchResponse(
    AddressTokens.empty,
    addresses = Seq.empty,
    limit = 10,
    offset = 0,
    total = 0
  )

  private val searchUnsupportedFormatReply: Future[Result] = Future.successful(BadRequest(Json.toJson(
    AddressBySearchResponseContainer(
      response = errorAddressResponse,
      status = BadRequestAddressResponseStatus,
      errors = Seq(FormatNotSupportedAddressResponseError)
    )
  )))

  private val searchEmptyQueryReply: Future[Result] = Future.successful(BadRequest(Json.toJson(
    AddressBySearchResponseContainer(
      response = errorAddressResponse,
      status = BadRequestAddressResponseStatus,
      errors = Seq(EmptyQueryAddressResponseError)
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
    format.stringToScheme().map {
      case PostcodeAddressFile(_) => searchPafAddressByUprn(uprn)
      case BritishStandard7666(_) => searchNagAddressByUprn(uprn)
    }.getOrElse(searchByUprnUnsupportedFormatReply)
  }


  private def searchPafAddressByUprn(uprn: String): Future[Result] = {
    esRepo.queryPafUprn(uprn).map {
      case Some(address) => Ok(Json.toJson(
        AddressByUprnResponseContainer(
          response = AddressByUprnResponse(
            address = Some(AddressResponseAddress.fromPafAddress(address))
          ),
          status = OkAddressResponseStatus
        )
      ))
      case None => notFoundReply
    }
  }

  private val notFoundReply = NotFound(Json.toJson(
    AddressByUprnResponseContainer(
      response = AddressByUprnResponse(
        address = None
      ),
      status = NotFoundAddressResponseStatus,
      errors = Seq(NotFoundAddressResponseError)
    )
  ))

  private def searchNagAddressByUprn(uprn: String): Future[Result] = {
    esRepo.queryNagUprn(uprn).map {
      case Some(address) => Ok(Json.toJson(
        AddressByUprnResponseContainer(
          response = AddressByUprnResponse(
            address = Some(AddressResponseAddress.fromNagAddress(address))
          ),
          status = OkAddressResponseStatus
        )
      ))
      case None => notFoundReply
    }
  }

  private val searchByUprnUnsupportedFormatReply: Future[Result] = Future.successful(BadRequest(Json.toJson(
    AddressByUprnResponseContainer(
      response = AddressByUprnResponse(
        address = None
      ),
      status = BadRequestAddressResponseStatus,
      errors = Seq(FormatNotSupportedAddressResponseError)
    )
  )))
}