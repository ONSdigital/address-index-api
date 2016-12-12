package uk.gov.ons.addressIndex.server.modules

import com.google.inject.{AbstractModule, Singleton}

/**
  * Application Play Module as EagerSingleton.
  */
@Singleton
class SystemBootstrapModule extends AbstractModule {
  def configure() = {
    bind(classOf[SystemBootstrap]).asEagerSingleton
  }
}