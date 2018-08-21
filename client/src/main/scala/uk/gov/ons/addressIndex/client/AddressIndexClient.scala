package uk.gov.ons.addressIndex.client

import play.api.libs.json.Json
import play.api.libs.ws.{WSClient, WSRequest}
import uk.gov.ons.addressIndex.client.AddressIndexClientHelper.{AddressIndexServerHost, AddressQuery, Bulk, PostcodeQuery, ShowQuery, UprnQuery, VersionQuery}
import uk.gov.ons.addressIndex.model.server.response.address.{AddressBySearchResponseContainer, AddressResponseVersion}
import uk.gov.ons.addressIndex.model.server.response.postcode.AddressByPostcodeResponseContainer
import uk.gov.ons.addressIndex.model.server.response.uprn.AddressByUprnResponseContainer
import uk.gov.ons.addressIndex.model.server.response.bulk.AddressBulkResponseContainer
import uk.gov.ons.addressIndex.model.{AddressIndexPostcodeRequest, AddressIndexSearchRequest, AddressIndexUPRNRequest, BulkBody}

import scala.language.implicitConversions
import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}

trait AddressIndexClient {

  /**
    * @return a standard web service client
    */
  def client: WSClient

  /**
    * @return the host address of the address index server
    */
  def host: String

  protected implicit lazy val iClient: WSClient = client
  protected implicit lazy val iHost: AddressIndexServerHost = host

   /**
    * perform an address search query
    *
    * @param request the request
    * @return a list of addresses
    */
  def addressQuery(request: AddressIndexSearchRequest)
    (implicit ec: ExecutionContext): Future[AddressBySearchResponseContainer] = {
    addressQueryWSRequest(request)
      .get
      .map(_.json.as[AddressBySearchResponseContainer])
  }

  /**
    * testable method for addressQuery
    *
    * @param request
    * @return
    */
  def addressQueryWSRequest(request: AddressIndexSearchRequest): WSRequest = {
    AddressQuery
      .toReq
      .withHttpHeaders("authorization" -> request.apiKey)
      .withQueryStringParameters(
        "input" -> request.input,
        "classificationfilter" -> request.filter,
        "historical" -> request.historical.toString,
        "matchthreshold" -> request.matchthreshold.toString,
        "rangekm" -> request.rangekm,
        "lat" -> request.lat,
        "lon" -> request.lon,
        "limit" -> request.limit,
        "offset" -> request.offset
      )
  }


  /**
    * perform a postcode search query
    *
    * @param request the request
    * @return a list of addresses
    */
  def postcodeQuery(request: AddressIndexPostcodeRequest)
    (implicit ec: ExecutionContext): Future[AddressByPostcodeResponseContainer] = {
      postcodeQueryWSRequest(request)
        .get
        .map(_.json.as[AddressByPostcodeResponseContainer])
  }

  /**
    * testable method for postcodeQuery
    *
    * @param request the request
    * @return
    */
  def postcodeQueryWSRequest(request: AddressIndexPostcodeRequest): WSRequest = {
    PostcodeQuery(request.postcode.toString)
      .toReq
      .withHttpHeaders("authorization" -> request.apiKey)
      .withQueryStringParameters(
        //"postcode" -> request.postcode,
        "classificationfilter" -> request.filter,
        "historical" -> request.historical.toString,
        "limit" -> request.limit,
        "offset" -> request.offset
      )
  }

  def bulk(request: BulkBody, apiKey: String)(implicit ec: ExecutionContext): Future[AddressBulkResponseContainer] = {
    Bulk
      .toReq
      .withRequestTimeout(Duration.Inf)
      .withHttpHeaders(
        "Content-Type" -> "application/json",
        "authorization" -> apiKey
      )
      .post(
        Json.toJson(
          request
        )
      )
      .map(_.json.as[AddressBulkResponseContainer])
  }

  /**
    * perform a uprn query
    *
    * @param request the request
    * @return an address
    */
  def uprnQuery(request: AddressIndexUPRNRequest)
                  (implicit ec: ExecutionContext): Future[AddressByUprnResponseContainer] = {
    uprnQueryWSRequest(request).withHttpHeaders("authorization" -> request.apiKey).get.map(_.json.as[AddressByUprnResponseContainer])
  }

  /**
    * testable method for uprnQuery
    *
    * @param request
    * @return
    */
  def uprnQueryWSRequest(request: AddressIndexUPRNRequest): WSRequest = {
    UprnQuery(request.uprn.toString)
      .toReq
  }

  def showQuery(input: String, filter: String, apiKey: String)(implicit ec: ExecutionContext): Future[String] = {
    ShowQuery
      .toReq
      .withHttpHeaders("authorization" -> apiKey)
      .withQueryStringParameters(
        "input" -> input,
        "classificationfilter" -> filter
      ).get.map(response => Json.prettyPrint(response.json))
  }

  def versionQuery()(implicit ec: ExecutionContext): Future[AddressResponseVersion] = {
    VersionQuery
      .toReq()
      .get.map(_.json.as[AddressResponseVersion])
  }

}

object AddressIndexClientHelper {

  implicit def str2host(h: String): AddressIndexServerHost = AddressIndexServerHost(h)

  sealed abstract class AddressIndexPath(val path: String, val method: String)

  implicit class AddressIndexPathToWsAugmenter(p: AddressIndexPath)
    (implicit client: WSClient, host: AddressIndexServerHost) {
    def toReq(): WSRequest = {
      client url s"${host.value}${p.path}" withMethod p.path
    }
  }

  case class AddressIndexServerHost(value: String)

  object UprnQuery extends AddressIndexPath(
    path = "",
    method = ""
  ) {
    def apply(uprn: String) = {
      val initialRoute = "/addresses/uprn"
      val fullRoute = s"$initialRoute/$uprn"
      new AddressIndexPath(
        path = fullRoute,
        method = "GET"
      ) {}
    }
  }

  object AddressQuery extends AddressIndexPath(
    path = "/addresses",
    method = "GET"
  )

//  object PostcodeQuery extends AddressIndexPath(
//    path = "/addresses/postcode",
//    method = "GET"
//  )

  object PostcodeQuery extends AddressIndexPath(
    path = "",
    method = ""
  ) {
    def apply(postcode: String) = {
      val initialRoute = "/addresses/postcode"
      val fullRoute = s"$initialRoute/$postcode"
      new AddressIndexPath(
        path = fullRoute,
        method = "GET"
      ) {}
    }
  }

  object VersionQuery extends AddressIndexPath(
    path = "/version",
    method = "GET"
  )

  object Bulk extends AddressIndexPath(
    path = "/bulk",
    method = "post"
  )

  object ShowQuery extends AddressIndexPath(
    path = "/query-debug",
    method = "GET"
  )
}
