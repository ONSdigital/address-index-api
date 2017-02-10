package uk.gov.ons.addressIndex.demoui.controllers

import javax.inject.{Inject, Singleton}

import play.api.Logger
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.Files.TemporaryFile
import play.api.mvc._
import uk.gov.ons.addressIndex.demoui.client.AddressIndexClientInstance
import uk.gov.ons.addressIndex.demoui.model.ui.Navigation
import uk.gov.ons.addressIndex.demoui.modules.DemouiConfigModule
import uk.gov.ons.addressIndex.demoui.utils.ClassHierarchy
import uk.gov.ons.addressIndex.model.{BulkBody, BulkQuery}

import scala.io.Source
import scala.concurrent.{ExecutionContext, Future}
import scala.language.postfixOps

/**
  * Controller class for a multiple addresses to be matched
  *
  * @param messagesApi
  * @param conf
  * @param apiClient
  * @param ec
  */
@Singleton
class BulkMatchController @Inject()(
  val messagesApi: MessagesApi,
  conf: DemouiConfigModule,
  apiClient: AddressIndexClientInstance,
  classHierarchy: ClassHierarchy
 )(
  implicit
  ec: ExecutionContext,
  mat: akka.stream.Materializer
) extends Controller with I18nSupport {

  private val multiMatchFormName = "file"
  private val logger =  Logger("BulkMatchController")

  def bulkMatchPage(): Action[AnyContent] = Action.async { implicit request =>
    Future successful Ok(
      uk.gov.ons.addressIndex.demoui.views.html.multiMatch(
        nav = Navigation.default,
        fileFormName = multiMatchFormName
      )
    )
  }


  def uploadFile(): Action[Either[MaxSizeExceeded, MultipartFormData[TemporaryFile]]] = Action.async(
    parse.maxLength(
      10 * 1024 * 1024, //10MB
      parse.multipartFormData
    )
  ) { implicit request =>
    logger info "invoked"

    val optRes = request.body match {
      case Right(file) => {
        file.file(multiMatchFormName) map { file =>
          val lines = Source.fromFile(file.ref.file).getLines
          apiClient bulk BulkBody(
            addresses = lines.map { line =>
              //format
              //id | address
              val arr = line.split("\\|")
              BulkQuery(
                id = arr(0),
                address = arr(1)
              )
            }.toSeq
          ) map { resp =>
            logger info s"Response size: ${resp.resp.size}"
            Ok(
              uk.gov.ons.addressIndex.demoui.views.html.multiMatch(
                nav = Navigation.default,
                fileFormName = multiMatchFormName,
                results = Some(resp)
              )
            )
          }

        }
      }
      case Left(maxSizeExceeded) => {
        logger info "Max size exceeded"
        Some(Future.successful(EntityTooLarge))
      }
    }
    optRes.getOrElse(Future.successful(InternalServerError))
  }
}