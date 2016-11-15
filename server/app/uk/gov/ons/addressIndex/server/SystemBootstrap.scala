package uk.gov.ons.addressIndex.server

import javax.inject.{Inject, Singleton}
import com.google.inject.ImplementedBy
import play.api.Logger
import uk.gov.ons.addressIndex.server.modules.{AddressIndexConfigModule, ElasticsearchRepository}

@ImplementedBy(classOf[SystemBootstrap])
trait Bootstrap {
  /**
    * Defines what should happen at application start.
    */
  def applicationStart() : Unit
  applicationStart()
}

/**
  * Eager singleton.
  */
@Singleton
class SystemBootstrap @Inject()(conf : AddressIndexConfigModule, esRepo : ElasticsearchRepository) extends Bootstrap {

  val logger = Logger("address-index:SystemBootstrap")

  def applicationStart() : Unit = {
    logger info "`SystemBootstrap` complete"
  }
}