package uk.gov.ons.addressIndex.server.controllers.general

import javax.inject.{Inject, Singleton}
import play.api.mvc.{Action, AnyContent, BaseController, ControllerComponents}
import uk.gov.ons.addressIndex.server.controllers.{PostcodeController, UPRNController}

import scala.concurrent.{ExecutionContext, Future}

/**
  * The main controller of the application.
  */
@Singleton
class ApplicationController @Inject()(val controllerComponents: ControllerComponents,
                                      postcodeController: PostcodeController,
                                      uprnController: UPRNController) (implicit ec: ExecutionContext) extends BaseController {
  def index(): Action[AnyContent] = Action {
    Ok("hello world")
  }

  /**
    * Health check endpoint used in Cloud deployments to keep alive Elasticsearch index channels.
    *
    * @return result to view
    */
  def healthz(): Action[AnyContent] = Action async { implicit req =>

    Future.sequence(List(postcodeController.postcodeQuery("PO155RR") (req), uprnController.uprnQuery("1") (req)))
      .map {
        results => {
          if (results.dropWhile(response => response.header.status == OK).isEmpty) {
            Ok
          } else {
            ImATeapot //Anything above 400 will fail the readiness probe
          }
        }
      }
  }
}