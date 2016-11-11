package uk.gov.ons.addressIndex.client

import scala.concurrent.Future
import uk.gov.ons.addressIndex.client.AddressIndexClient.{AddressIndexServerHost, UprnQuery}
import uk.gov.ons.addressIndex.model.{AddressIndexSearchRequest, AddressIndexUPRNRequest, AddressScheme}
import play.api.libs.ws.{WSClient, WSRequest}

trait AddressIndexClient {

  /**
    * @return a standard web service client
    */
  def client: WSClient

  /**
    * @return the host address of the address index server
    */
  def host: String

  protected val applicationType : String = "application/json"
  protected implicit lazy val iClient : WSClient = client
  protected implicit lazy val iHost : AddressIndexServerHost = host

  /**
    * perform an address search query
    *
    * @param req the request
    * @return a list of addresses
    */
  def addressQuery(req : AddressIndexSearchRequest) : Future[_] = {
    AddressQuery
      .withQueryString(
        Seq(
          "input" -> req.input,
          "format" -> req.format.toString
        )
      )
      .get
  }

  /**
    * perform a uprn query
    *
    * @param req the request
    * @return an address
    */
  def uprnQuery(req : AddressIndexUPRNRequest) : Future[_] = {
    UprnQuery(req.uprn.toString)
      .withQueryString(Seq("format" -> req.format.toString))
      .get
  }
}

object AddressIndexClient {

  implicit def str2host(h : String) : AddressIndexServerHost = AddressIndexServerHost(h)

  implicit def path2req(p : AddressIndexPath)(implicit client : WSClient, host : AddressIndexServerHost) : WSRequest =
    client url s"${host.value}${p.path}" withMethod p.path

  sealed abstract class AddressIndexPath(val path : String, val method : String)

  case class AddressIndexServerHost(value : String)

  object UprnQuery extends AddressIndexPath("", "") {
    def apply(uprn : String) = {
      val initialRoute = "/addresses"
      val fullRoute = s"$initialRoute/$uprn"
      new AddressIndexPath(fullRoute, "GET") {}
    }
  }

  object AddressQuery extends AddressIndexPath("/addresses", "GET")
}