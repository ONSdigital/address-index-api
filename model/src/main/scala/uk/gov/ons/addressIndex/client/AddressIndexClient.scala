package uk.gov.ons.addressIndex.client

import scala.concurrent.Future
import uk.gov.ons.addressIndex.client.AddressIndexClient.{AddressIndexServerHost, UprnQuery}
import uk.gov.ons.addressIndex.model.{AddressIndexSearchRequest, AddressIndexUPRNRequest}
import play.api.libs.ws.{WSClient, WSRequest, WSResponse}
import AddressIndexClient._

/**
  *
  */
trait AddressIndexClient {

  /**
    * @return a standard web service client
    */
  def client : WSClient

  /**
    * @return the host address of the address index server
    */
  def host : String

  protected implicit lazy val iClient : WSClient = client
  protected implicit lazy val iHost : AddressIndexServerHost = host

  /**
    * perform an address search query
    *
    * @param request the request
    * @return a list of addresses
    */
  def addressQuery(request : AddressIndexSearchRequest) : Future[WSResponse] = {
    AddressQuery
      .withQueryString(
        "input" -> request.input,
        "format" -> request.format.toString
      )
      .get
  }

  /**
    * perform a uprn query
    *
    * @param request the request
    * @return an address
    */
  def uprnQuery(request : AddressIndexUPRNRequest) : Future[WSResponse] = {
    UprnQuery(request.uprn.toString)
      .withQueryString(
        "format" -> request.format.toString
      )
      .get
  }
}

object AddressIndexClient {

  implicit def str2host(h : String) : AddressIndexServerHost = AddressIndexServerHost(h)

  implicit def path2req(p : AddressIndexPath)(implicit client : WSClient, host : AddressIndexServerHost) : WSRequest =
    client url s"${host.value}${p.path}" withMethod p.method

  sealed abstract class AddressIndexPath(val path : String, val method : String)

  case class AddressIndexServerHost(value : String)

  object UprnQuery extends AddressIndexPath(
    path = "",
    method = ""
  ) {
    def apply(uprn : String) = {
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