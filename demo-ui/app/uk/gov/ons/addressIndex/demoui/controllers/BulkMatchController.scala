package uk.gov.ons.addressIndex.demoui.controllers

import java.util.UUID
import javax.inject.{Inject, Singleton}

import play.api.Logger
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.Files.TemporaryFile
import play.api.mvc.{Action, AnyContent, Controller, MultipartFormData}
import uk.gov.ons.addressIndex.demoui.client.AddressIndexClientInstance
import uk.gov.ons.addressIndex.demoui.model.ui.Navigation
import uk.gov.ons.addressIndex.demoui.modules.DemouiConfigModule
import uk.gov.ons.addressIndex.demoui.utils.ClassHierarchy
import uk.gov.ons.addressIndex.model.AddressIndexSearchRequest
import scala.io.Source
import scala.concurrent.{ExecutionContext, Future}

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
 )(implicit ec: ExecutionContext) extends Controller with I18nSupport {

  private val multiMatchFormName = "file"

  def bulkMatchPage(): Action[AnyContent] = Action.async { implicit request =>
    Future successful Ok(
      uk.gov.ons.addressIndex.demoui.views.html.multiMatch(
        nav = Navigation.default,
        fileFormName = multiMatchFormName
      )
    )
  }

  def uploadFile(): Action[MultipartFormData[TemporaryFile]] = Action.async(parse.multipartFormData) { implicit request =>
    request.body.file(multiMatchFormName) map { file =>
      val seqAddress = Source.fromFile(file.ref.file).mkString.split("\n").toSeq
      Future.sequence(
        seqAddress map { address =>
          apiClient.addressQuery(
            request = AddressIndexSearchRequest(
              input = address,
              limit = "10",
              offset = "0",
              id = UUID.randomUUID
            )
          )
        }
      ) map { resp =>
        Ok(
          uk.gov.ons.addressIndex.demoui.views.html.multiMatch(
            nav = Navigation.default,
            fileFormName = multiMatchFormName,
            results = Some(
              resp
            )
          )
        )
      }

    } getOrElse Future.successful(InternalServerError)
  }
}