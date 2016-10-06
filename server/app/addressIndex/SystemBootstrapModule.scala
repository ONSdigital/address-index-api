package addressIndex

import com.google.inject.AbstractModule
import com.google.inject.name.Names

/**
  * Application Play Module as EagerSingleton. Injected using Guice and Configuration
  */
class SystemBootstrapModule
  extends AbstractModule {
  def configure() = {
    bind(classOf[Bootstrap])
      .annotatedWith(Names.named("addressIndex.SystemBootstrap"))
      .to(classOf[SystemBootstrap]).asEagerSingleton()
  }
}