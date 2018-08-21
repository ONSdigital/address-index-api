package uk.gov.ons.addressIndex.server.modules

import uk.gov.ons.addressIndex.model.config.AddressIndexConfig

trait ConfigModule {
  def config: AddressIndexConfig
}
