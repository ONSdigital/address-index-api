package uk.gov.ons.addressIndex.model.server.response.rh

import uk.gov.ons.addressIndex.model.server.response.address.AddressResponseAddressUPRNEQ

/**
  * Contains relevant information to the requested address
  *
  * @param address found address
  * @param addressType the type of address (PAF, WELSHPAF, NAG, WELSHNAG & NISRA)
  * @param historical ES index choice
  * @param epoch AB Epoch
  * @param verbose output verbosity
  */
case class AddressByEQUprnResponse(address: Option[AddressResponseAddressUPRNEQ],
                                   addressType: String,
                                   historical: Boolean,
                                   epoch: String,
                                   verbose: Boolean)




