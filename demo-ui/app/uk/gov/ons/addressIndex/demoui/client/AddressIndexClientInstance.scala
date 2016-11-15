package uk.gov.ons.addressIndex.demoui.client

import javax.inject.{Inject, Singleton}
import play.api.libs.ws.WSClient
import uk.gov.ons.addressIndex.client.AddressIndexClient

@Singleton
class AddressIndexClientInstance @Inject()(override val client : WSClient) extends AddressIndexClient {
  override def host : String = "http://localhost:9001"
}