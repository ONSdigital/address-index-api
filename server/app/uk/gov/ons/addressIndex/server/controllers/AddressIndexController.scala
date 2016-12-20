package uk.gov.ons.addressIndex.server.controllers

import uk.gov.ons.addressIndex.server.modules.AddressIndexCannedResponse

abstract class AddressIndexController
  extends PlayHelperController
  with AddressIndexCannedResponse
