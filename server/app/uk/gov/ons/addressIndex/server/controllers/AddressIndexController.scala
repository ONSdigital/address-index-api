package uk.gov.ons.addressIndex.server.controllers

import javax.inject.Inject
import uk.gov.ons.addressIndex.server.modules.AddressIndexCannedResponse

@Inject
class AddressIndexController
  extends PlayHelperController
  with AddressIndexCannedResponse