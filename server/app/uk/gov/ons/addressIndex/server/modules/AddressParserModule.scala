package uk.gov.ons.addressIndex.server.modules

import javax.inject.Singleton
import uk.gov.ons.addressIndex.crfscala.CrfScala.CrfTokenResult
import uk.gov.ons.addressIndex.parsers.AddressParser

@Singleton
class AddressParserModule {

  /**
    *
    * @param input
    * @return
    */
  def tag(input: String): Seq[CrfTokenResult] = {
    AddressParser tag input
  }
}