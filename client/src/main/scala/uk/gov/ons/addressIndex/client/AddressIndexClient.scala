package uk.gov.ons.addressIndex.client

import scala.concurrent.{ExecutionContext, Future}
import uk.gov.ons.addressIndex.model.{AddressIndexSearchRequest, AddressIndexUPRNRequest}
import play.api.libs.ws.{WSClient, WSRequest, WSResponse}
import uk.gov.ons.addressIndex.client.AddressIndexClientHelper.{AddressIndexServerHost, AddressQuery, UprnQuery}
import uk.gov.ons.addressIndex.model.server.response.AddressBySearchResponseContainer


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
    addressQueryWSRequest(request).get.map { x =>
      println(x)
      x.json.as[AddressBySearchResponseContainer]
    }
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
      .withQueryString(
        "input" -> request.input,
        "limit" -> request.limit,
        "offset" -> request.offset
      )
  }

  /**
    * perform a `bulk` address search query
    *
    * @param requests the requests
    * @return a list of addresses for each request, in order of the requests
    */
  def addressQueriesBulkMimic(requests: Seq[AddressIndexSearchRequest])
                             (implicit ec: ExecutionContext): Future[Seq[AddressBySearchResponseContainer]] = {
    Future sequence(requests map addressQuery)
  }

  /**
    * perform a uprn query
    *
    * @param request the request
    * @return an address
    */
  def uprnQuery(request: AddressIndexUPRNRequest): Future[WSResponse] = {
    urpnQueryWSRequest(request).get
  }

  /**
    * testable method for uprnQuery
    *
    * @param request
    * @return
    */
  def urpnQueryWSRequest(request: AddressIndexUPRNRequest): WSRequest = {
    UprnQuery(request.uprn.toString)
      .toReq
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
}