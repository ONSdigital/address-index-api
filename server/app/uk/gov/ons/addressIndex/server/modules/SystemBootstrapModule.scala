package uk.gov.ons.addressIndex.server.modules

import com.google.inject.AbstractModule

/**
  * Application Play Module as EagerSingleton.
  */
class SystemBootstrapModule extends AbstractModule {
  def configure() = {
//    bind(classOf[Bootstrap])
//      .annotatedWith(Names named "uk.gov.ons.addressIndex.server.SystemBootstrap")
//      .to(classOf[SystemBootstrap])
//      .asEagerSingleton
  }
}