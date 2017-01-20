package uk.gov.ons.addressIndex.client

import scala.concurrent.{ExecutionContext, Future}
import uk.gov.ons.addressIndex.model.{AddressIndexSearchRequest, AddressIndexUPRNRequest, AddressScheme}
import play.api.libs.ws.{WSClient, WSRequest, WSResponse}
import uk.gov.ons.addressIndex.client.AddressIndexClientHelper.{AddressIndexServerHost, AddressQuery, UprnQuery}
import uk.gov.ons.addressIndex.model.server.response.Container

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
    (implicit ec: ExecutionContext): Future[Container] = {
    AddressQuery
      .toReq
      .formatOptionalQueryString(request.format)
      .withQueryString(
        "input" -> request.input,
        "limit" -> request.limit,
        "offset" -> request.offset
      )
      .get
      .map(_.json.as[Container])
  }

  /**
    * perform a `bulk` address search query
    *
    * @param requests the requests
    * @return a list of addresses for each request, in order of the requests
    */
  def addressQueriesBulkMimic(requests: Seq[AddressIndexSearchRequest])
    (implicit ec: ExecutionContext): Future[Seq[Container]] = {
    Future sequence(requests map addressQuery)
  }

  /**
    * perform a uprn query
    *
    * @param request the request
    * @return an address
    */
  def uprnQuery(request: AddressIndexUPRNRequest): Future[WSResponse] = {
    UprnQuery(request.uprn.toString)
      .toReq
      .formatOptionalQueryString(request.format)
      .get
  }

  implicit class AugOptFormat(req: WSRequest) {
    def formatOptionalQueryString(format: Option[AddressScheme]): WSRequest = {
      format.map(fmt => req.withQueryString("format" -> fmt.toString)).getOrElse(req)
    }
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