package uk.gov.ons.addressIndex.server.controllers

import play.api.mvc.ControllerComponents
import play.api.Configuration
import com.iheart.playSwagger.SwaggerSpecGenerator
import play.api.libs.json.JsString
import play.api.mvc._

import javax.inject.{Inject, Singleton}

@Singleton
class ApiSpecs @Inject()(cc: ControllerComponents, config: Configuration) extends AbstractController(cc) {
  implicit val cl = getClass.getClassLoader

  val domainPackage = "uk.gov.ons.addressIndex.model.server.response"
  lazy val generator = SwaggerSpecGenerator(true,domainNameSpaces = domainPackage)

// it is possible to modify the swagger output and items can be drawn from config
// the code below appends a space but could do more

  lazy val swagger = Action {
      generator.generate().map(_ + (" ")).fold(
      e => InternalServerError("Couldn't generate swagger."),
      s => Ok(s))
  }

  def specs = swagger
}
