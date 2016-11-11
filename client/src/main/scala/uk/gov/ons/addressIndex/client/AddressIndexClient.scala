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
  
  protected implicit lazy val iClient : WSClient = client
  protected implicit lazy val iHost : AddressIndexServerHost = AddressIndexServerHost(host)

  /**
    * perform an address search query
    *
    * @param req the request
    * @return a list of addresses
    */
  def addressQuery(req : AddressIndexSearchRequest) : Future[_] = {
    AddressQuery
      .toReq
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
      .toReq
      .withQueryString(Seq("format" -> req.format.toString))
      .get
  }
}

object AddressIndexClient {

  sealed abstract class AddressIndexPath(val path : String, val method : String)

  case class AddressIndexServerHost(value : String)

  implicit class AddressIndexPathToWsAugmenter(h : AddressIndexPath)(implicit client : WSClient) {
    def toReq() : WSRequest = {
      client url s"${host.value}${p.path}" withMethod p.path
    }
  }

  object UprnQuery extends AddressIndexPath("", "") {
    def apply(uprn : String) = {
      val initialRoute = "/addresses"
      val fullRoute = s"$initialRoute/$uprn"
      new AddressIndexPath(fullRoute, "GET") {}
    }
  }

  object AddressQuery extends AddressIndexPath("/addresses", "GET")
}