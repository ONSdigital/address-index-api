package uk.gov.ons.address.controllers

import javax.inject.{Inject, Singleton}

import org.slf4j.LoggerFactory
import play.Logger
import play.api.mvc.{Controller, _}
import uk.gov.ons.address.conf.OnsFrontendConfiguration
import uk.gov.ons.address.views

@Singleton
class ApplicationHome @Inject()(configuation: OnsFrontendConfiguration)
    extends Controller {
  def indexPage = Action {
    Logger.info("Rendering Index page")
    Ok(views.html.index(configuation))
  }
}
