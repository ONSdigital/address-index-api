package uk.gov.ons.addressIndex.client

import javax.inject.{Inject, Singleton}
import play.api.libs.ws.WSClient

@Singleton
class AddressIndexClientInstance @Inject()(override val client : WSClient) extends AddressIndexClient {
  override def host : String = "http://localhost:9001"
}