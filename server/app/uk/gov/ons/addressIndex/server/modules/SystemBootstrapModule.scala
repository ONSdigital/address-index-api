package uk.gov.ons.addressIndex.server.modules

import com.google.inject.{AbstractModule, Singleton}
import uk.gov.ons.addressIndex.server.utils.Overload
import uk.gov.ons.addressIndex.server.utils.impl.OverloadProtector

/**
  * Application Play Module as EagerSingleton.
  */
@Singleton
class SystemBootstrapModule extends AbstractModule {
  def configure(): Unit = {
    bind(classOf[SystemBootstrap]).asEagerSingleton()
    bind(classOf[Overload]).to(classOf[OverloadProtector]).asEagerSingleton()
    bind(classOf[ConfigModule]).to(classOf[AddressIndexConfigModule]).asEagerSingleton()
    bind(classOf[ElasticsearchRepository]).to(classOf[AddressIndexRepository]).asEagerSingleton()
    bind(classOf[ParserModule]).to(classOf[AddressParserModule]).asEagerSingleton()
    bind(classOf[VersionModule]).to(classOf[AddressIndexVersionModule]).asEagerSingleton()
  }
}
