package uk.gov.ons.addressIndex.demoui.controllers

import java.util.UUID
import javax.inject.{Inject, Singleton}

import play.api.Logger
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.Files.TemporaryFile
import play.api.mvc._
import uk.gov.ons.addressIndex.demoui.client.AddressIndexClientInstance
import uk.gov.ons.addressIndex.demoui.model.ui.Navigation
import uk.gov.ons.addressIndex.demoui.modules.DemouiConfigModule
import uk.gov.ons.addressIndex.demoui.utils.ClassHierarchy
import uk.gov.ons.addressIndex.model.{AddressIndexSearchRequest, BulkBody, BulkQuery}
import uk.gov.ons.addressIndex.model.server.response.AddressBySearchResponseContainer

import scala.io.Source
import scala.concurrent.{Await, ExecutionContext, Future}
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

    Logger("uploadFile").info("invoked")

    val optRes = request.body match {
      case Right(file) => {
        file.file(multiMatchFormName) map { file =>
          val lines = Source.fromFile(file.ref.file).getLines()
          //format
          //id | address
          apiClient bulk BulkBody(
            addresses = lines.map { line =>
              println(line)
              val arr = line.split("\\|")

              BulkQuery(
                id = arr(0),
                address = arr(1)
              )
            }.toSeq
          ) map { resp =>
            Logger("uploadFile").info(s"${resp.resp.size}")

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
        Some(Future.successful(EntityTooLarge))
      }
    }
    optRes.getOrElse(Future.successful(InternalServerError))
  }
}