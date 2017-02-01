package uk.gov.ons.addressIndex.demoui.controllers

import javax.inject.{Inject, Singleton}

import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, Controller}
import uk.gov.ons.addressIndex.demoui.client.AddressIndexClientInstance
import uk.gov.ons.addressIndex.demoui.modules.DemouiConfigModule
import uk.gov.ons.addressIndex.demoui.utils.ClassHierarchy

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

  def bulkMatchPage(): Action[AnyContent] = Action.async { implicit request =>
    Future.successful(Ok)
  }
}