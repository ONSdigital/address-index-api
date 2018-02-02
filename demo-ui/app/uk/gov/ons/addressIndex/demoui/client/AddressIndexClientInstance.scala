package uk.gov.ons.addressIndex.demoui.client

import javax.inject.{Inject, Singleton}
import play.api.libs.ws.WSClient
import uk.gov.ons.addressIndex.client.AddressIndexClient
import uk.gov.ons.addressIndex.demoui.modules.DemouiConfigModule

@Singleton
class AddressIndexClientInstance @Inject()(override val client : WSClient,
                                           conf : DemouiConfigModule) extends AddressIndexClient {
  //  set config entry to "http://localhost:9001" to run locally
  //  set config entry to "https://addressindexapitest.cfapps.io" to run from cloud
  override def host: String = s"${conf.config.apiURL.host}:${conf.config.apiURL.port}${conf.config.apiURL.gatewayPath}"
}