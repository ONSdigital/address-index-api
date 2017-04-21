package uk.gov.ons.addressIndex.client

import play.api.libs.json.Json
import play.api.libs.ws.{WSClient, WSRequest}
import uk.gov.ons.addressIndex.client.AddressIndexClientHelper.{AddressIndexServerHost, AddressQuery, Bulk, ShowQuery, UprnQuery, VersionQuery}
import uk.gov.ons.addressIndex.model.server.response.{AddressBulkResponseContainer, AddressBySearchResponseContainer, AddressByUprnResponseContainer, AddressResponseVersion}
import uk.gov.ons.addressIndex.model.{AddressIndexSearchRequest, AddressIndexUPRNRequest, BulkBody}

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
      .withHeaders("authorization" -> request.apiKey)
      .withQueryString(
        "input" -> request.input,
        "limit" -> request.limit,
        "offset" -> request.offset
      )
  }

  def bulk(request: BulkBody)(implicit ec: ExecutionContext): Future[AddressBulkResponseContainer] = {
    Bulk
      .toReq
      .withRequestTimeout(Duration.Inf)
      .withHeaders(
        "Content-Type" -> "application/json",
        "authorization" -> request.apiKey
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
    uprnQueryWSRequest(request).withHeaders("authorization" -> request.apiKey).get.map(_.json.as[AddressByUprnResponseContainer])
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

  def showQuery(input: String, apiKey: String)(implicit ec: ExecutionContext): Future[String] = {
    ShowQuery
      .toReq
      .withHeaders("authorization" -> apiKey)
      .withQueryString(
        "input" -> input
      ).get.map(response => Json.prettyPrint(response.json))
  }

  def versionQuery(apiKey: String)(implicit ec: ExecutionContext): Future[AddressResponseVersion] = {
    VersionQuery
      .toReq
      .withHeaders("authorization" -> apiKey)
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
      val initialRoute = "/addresses"
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