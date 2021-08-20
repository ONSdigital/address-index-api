package uk.gov.ons.addressIndex.demoui.controllers

import javax.inject.{Inject, Singleton}
import org.apache.commons.lang3.StringUtils
import play.api.Logger
import play.api.i18n.{I18nSupport, Lang, Langs, MessagesApi}
import play.api.mvc._
import uk.gov.ons.addressIndex.demoui.client.AddressIndexClientInstance
import uk.gov.ons.addressIndex.demoui.modules.{DemoUIAddressIndexVersionModule, DemouiConfigModule}
import uk.gov.ons.addressIndex.demoui.utils.{ClassHierarchy, RelativesExpander}
import uk.gov.ons.addressIndex.demoui.{controllers, views}
import uk.gov.ons.addressIndex.model.AddressIndexPartialRequestGcp

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

/**
  * Controller class for a partial address to be matched
  *
  * @param conf        conf reference
  * @param messagesApi messagesApi ref
  * @param apiClient   apiClient ref
  * @param ec          ec ref
  */
@Singleton
class TypeaheadController @Inject()(val controllerComponents: ControllerComponents,
                                      conf: DemouiConfigModule,
                                      override val messagesApi: MessagesApi,
                                      langs: Langs,
                                      apiClient: AddressIndexClientInstance,
                                      classHierarchy: ClassHierarchy,
                                      relativesExpander: RelativesExpander,
                                      version: DemoUIAddressIndexVersionModule
                                     )(implicit ec: ExecutionContext) extends BaseController with I18nSupport {

  implicit val lang: Lang = langs.availables.head

  val logger = Logger("TypeaheadController")
  val pageSize: Int = conf.config.limit
  val maxOff: Int = conf.config.maxOffset
  val maxPages: Int = (maxOff + pageSize - 1) / pageSize
  val apiUrl: String = conf.config.apiURL.ajaxHost + ":" + conf.config.apiURL.ajaxPort + conf.config.apiURL.gatewayPath
  val showNisra: Boolean = Try(conf.config.nisra.toBoolean).getOrElse(true)

  /**
    * Present empty form for user to input address
    *
    * @return result to view
    */
  def showMatchPartialPage(): Action[AnyContent] = Action.async { implicit request =>
    request.session.get("api-key").map { _ =>
      val viewToRender = views.html.typeaheadSearch(
        conf = conf,
        version = version
      )
      Future.successful(Ok(viewToRender))
    }.getOrElse {
      Future.successful(Redirect(controllers.routes.ApplicationHomeController.login())
        .withSession("referer" -> request.uri))
    }
  }

  def doMatchPartial(input: String, filter: Option[String] = None, fallback: Option[String] = None ): Action[AnyContent] = Action.async {

    val addressText = StringUtils.stripAccents(input)
    val filterText = StringUtils.stripAccents(filter.getOrElse(""))
    val limit = pageSize.toString
    val fallbackOrDefault = fallback.flatMap(x => Try(x.toBoolean).toOption).getOrElse(true)

    apiClient.gcpPartialQueryWSRequest(
      AddressIndexPartialRequestGcp (
        partial = addressText,
        filter = filterText,
        limit = limit,
        fallback = fallbackOrDefault
      )
    ).get().map(_.json) map { resp =>
      Ok(resp).as("application/json")
    }
  }
}