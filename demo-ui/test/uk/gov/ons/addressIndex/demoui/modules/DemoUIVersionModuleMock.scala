package uk.gov.ons.addressIndex.demoui.modules

import javax.inject.{Inject, Singleton}
import play.api.libs.ws.WSClient
import uk.gov.ons.addressIndex.demoui.client.AddressIndexClientMock

import scala.concurrent.ExecutionContext

@Singleton
class DemoUIVersionModuleMock @Inject()(apiClient: AddressIndexClientMock, ec: ExecutionContext)

  extends DemoUIAddressIndexVersionModule(apiClient: AddressIndexClientMock)(ec: ExecutionContext) {
  override lazy val apiVersion = "a12345"
  override lazy val dataVersion = "39"
}
