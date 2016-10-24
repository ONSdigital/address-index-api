package addressIndex.modules

import com.google.inject.AbstractModule

/**
  * Application Play Module as EagerSingleton.
  */
class SystemBootstrapModule extends AbstractModule {
  def configure() = {
//    bind(classOf[Bootstrap])
//      .annotatedWith(Names named "addressIndex.SystemBootstrap")
//      .to(classOf[SystemBootstrap])
//      .asEagerSingleton
  }
}