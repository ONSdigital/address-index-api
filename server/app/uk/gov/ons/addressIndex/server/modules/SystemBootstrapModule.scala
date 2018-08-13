package uk.gov.ons.addressIndex.server.modules

import com.google.inject.{AbstractModule, Singleton}
import uk.gov.ons.addressIndex.server.utils.APIThrottler
import uk.gov.ons.addressIndex.server.utils.impl.APIThrottle

/**
  * Application Play Module as EagerSingleton.
  */
@Singleton
class SystemBootstrapModule extends AbstractModule {
  def configure(): Unit = {
    bind(classOf[SystemBootstrap]).asEagerSingleton()
    bind(classOf[APIThrottler]).to(classOf[APIThrottle]).asEagerSingleton()
    bind(classOf[ConfigModule]).to(classOf[AddressIndexConfigModule]).asEagerSingleton()
    bind(classOf[ElasticsearchRepository]).to(classOf[AddressIndexRepository]).asEagerSingleton()
    bind(classOf[ParserModule]).to(classOf[AddressParserModule]).asEagerSingleton()
    bind(classOf[VersionModule]).to(classOf[AddressIndexVersionModule]).asEagerSingleton()
  }
}
