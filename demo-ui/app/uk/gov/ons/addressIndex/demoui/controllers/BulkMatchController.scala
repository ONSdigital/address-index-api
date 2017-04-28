package uk.gov.ons.addressIndex.demoui.controllers

import javax.inject.{Inject, Singleton}

import com.github.tototoshi.csv._
import play.api.Logger
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.Files.TemporaryFile
import play.api.mvc._
import uk.gov.ons.addressIndex.demoui.client.AddressIndexClientInstance
import uk.gov.ons.addressIndex.demoui.model.ui.Navigation
import uk.gov.ons.addressIndex.demoui.modules.DemoUIAddressIndexVersionModule
import uk.gov.ons.addressIndex.model.{BulkBody, BulkQuery}

import scala.concurrent.{ExecutionContext, Future}
import scala.language.postfixOps

/**
  * Controller class for a multiple addresses to be matched
  *
  * @param messagesApi
  * @param apiClient
  * @param ec
  */
@Singleton
class BulkMatchController @Inject()(
  val messagesApi: MessagesApi,
  apiClient: AddressIndexClientInstance,
  version: DemoUIAddressIndexVersionModule
 )(
  implicit
  ec: ExecutionContext,
  mat: akka.stream.Materializer
) extends Controller with I18nSupport {

  private val multiMatchFormName = "file"
  private val logger = Logger("BulkMatchController")

  def bulkMatchPage(): Action[AnyContent] = Action.async { implicit request =>
    val refererUrl = request.uri
    logger.info("referer = " + refererUrl)
    request.session.get("api-key").map { apiKey =>
    Future successful Ok(
      uk.gov.ons.addressIndex.demoui.views.html.multiMatch(
        nav = Navigation.default,
        fileFormName = multiMatchFormName,
        version = version
      )
    )
    }.getOrElse {
      Future.successful(Redirect(uk.gov.ons.addressIndex.demoui.controllers.routes.ApplicationHomeController.login()).withSession("referer" -> refererUrl))
    }
  }

  def uploadFile(): Action[Either[MaxSizeExceeded, MultipartFormData[TemporaryFile]]] = Action.async(
    parse.maxLength(
      10 * 1024 * 1024, //10MB
      parse.multipartFormData
    )
  )  { implicit request =>
    request.session.get("api-key").map { apiKey =>
      logger info "invoked"

    val optRes = request.body match {
      case Right(file) => {
        file.file(multiMatchFormName) map { file =>
          apiClient bulk BulkBody(
            addresses = CSVReader.open(file.ref.file).all().zipWithIndex.flatMap { case (lines, index) =>
              if(index == 0) {
                None
              } else {
                Some(
                  BulkQuery(
                    id = lines.head,
                    address = lines(1)
                  )
                )
              }
            },
          apiKey = apiKey) map { resp =>
            logger info s"Response size: ${resp.bulkAddresses.size}"
            Ok(
              uk.gov.ons.addressIndex.demoui.views.html.multiMatch(
                nav = Navigation.default,
                fileFormName = multiMatchFormName,
                results = Some(resp),
              version = version)
            )
          }
        }
      }
      case Left(maxSizeExceeded) => {
        logger info "Max size exceeded"
        Some(Future.successful(EntityTooLarge))
      }
    }
    optRes.getOrElse(Future.successful(InternalServerError))}.getOrElse {
      Future.successful(Redirect(uk.gov.ons.addressIndex.demoui.controllers.routes.ApplicationHomeController.login()))
    }
  }
}