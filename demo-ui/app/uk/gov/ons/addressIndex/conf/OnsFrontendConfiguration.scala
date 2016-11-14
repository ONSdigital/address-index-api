package uk.gov.ons.addressIndex.conf

import javax.inject.Inject

import play.api.Configuration

/**
  * Created by amits on 04/07/2016.
  */
class OnsFrontendConfiguration @Inject()(configuration: Configuration) {
  def onsAddressApiUri = configuration.getString("address.index.api.uri").get
  def onsUploadFileLocation: String =
    configuration.getString("address.index.app.fileLoc").get
  def onsApiCallTimeout: Long =
    configuration.getLong("address.index.api.call.timeout.secs").get
  def onsApiProposeNewAddressUri: String =
    configuration.getString("address.index.proposeNewAddress.uri").get
}
