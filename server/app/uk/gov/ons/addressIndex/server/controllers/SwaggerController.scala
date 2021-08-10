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

  // Get's host configuration.
  val host = config.get[String]("swagger.host")

  lazy val swagger = Action { request =>
    generator.generate().map(_ + ("host" -> JsString(host))).fold(
      e => InternalServerError("Couldn't generate swagger."),
      s => Ok(s))
  }

  def specs = swagger
}
