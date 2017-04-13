package uk.gov.ons.addressIndex.demoui.modules

import javax.inject.Singleton

import uk.gov.ons.addressIndex.demoui.client.{AddressIndexClientInstance, AddressIndexClientMock}

@Singleton
class DemoUIVersionModuleMock {
  val apiVersion = "a12345"
  val dataVersion = "39"
}
