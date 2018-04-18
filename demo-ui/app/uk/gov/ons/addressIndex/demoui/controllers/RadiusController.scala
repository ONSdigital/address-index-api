package uk.gov.ons.addressIndex.demoui.controllers

import java.util.UUID
import javax.inject.{Inject, Singleton}

import org.apache.commons.lang3.StringUtils
import play.api.Logger
import play.api.data.Forms._
import play.api.data._
import play.api.i18n.{I18nSupport, Lang, Langs, MessagesApi}
import play.api.mvc._
import uk.gov.ons.addressIndex.demoui.client.AddressIndexClientInstance
import uk.gov.ons.addressIndex.demoui.model._
import uk.gov.ons.addressIndex.demoui.modules.{DemoUIAddressIndexVersionModule, DemouiConfigModule}
import uk.gov.ons.addressIndex.demoui.utils.ClassHierarchy
import uk.gov.ons.addressIndex.model.{AddressIndexSearchRequest}
import uk.gov.ons.addressIndex.model.server.response.{AddressBySearchResponseContainer}

import scala.concurrent.{ExecutionContext, Future}
import scala.language.implicitConversions
import scala.util.Try

/**
  * Controller class for a postcode to be matched
  * @param conf           conf
  * @param messagesApi    messageApi
  * @param apiClient      apiClient
  * @param ec             ec
  */
@Singleton
class RadiusController @Inject()(
                                    val controllerComponents: ControllerComponents,
                                    conf : DemouiConfigModule,
                                    override val messagesApi: MessagesApi,
                                    langs: Langs,
                                    apiClient: AddressIndexClientInstance,
                                    classHierarchy: ClassHierarchy,
                                    version: DemoUIAddressIndexVersionModule
                                  )(implicit ec: ExecutionContext) extends BaseController with I18nSupport {

  implicit val lang: Lang = langs.availables.head

  val logger = Logger("PostcodeController")
  val pageSize = conf.config.limit
  val maxOff = conf.config.maxOffset
  val maxPages = (maxOff + pageSize - 1) / pageSize

  /**
    * Present empty form for user to input address
    *
    * @return result to view
    */
  def showRadiusMatchPage(): Action[AnyContent] = Action.async { implicit request =>
    //  logger info ("SingleMatch: Rendering Single Match Page")
    val refererUrl = request.uri
    request.session.get("api-key").map { apiKey =>
      val viewToRender = uk.gov.ons.addressIndex.demoui.views.html.radiusSearch(
        radiusSearchForm = RadiusController.form,
        filter = None,
        historical = false,
        rangekm = None,
        lat = None,
        lon = None,
        warningMessage = None,
        pageNum = 1,
        pageSize = pageSize,
        pageMax = maxPages,
        addressBySearchResponse = None,
        classification = None,
        version = version)
      Future.successful(
        Ok(viewToRender)
      )
    }.getOrElse {
      Future.successful(Redirect(uk.gov.ons.addressIndex.demoui.controllers.routes.ApplicationHomeController.login()).withSession("referer" -> refererUrl))
    }
  }

  /**
    * Accept posted form, deal with empty address or pass on to MatchWithInput
    *
    * @return result to view or redirect
    */
  def doMatch(): Action[AnyContent] = Action.async { implicit request =>
    val optAddress: Option[String] = Try(request.body.asFormUrlEncoded.get("address").mkString).toOption
    val addressText = optAddress.getOrElse("")
    val optFilter: Option[String] = Try(request.body.asFormUrlEncoded.get("filter").mkString).toOption
    val filterText = optFilter.getOrElse("")
    val optRange: Option[String] = Try(request.body.asFormUrlEncoded.get("rangekm").mkString).toOption
    val rangeText = optRange.getOrElse("")
    val optLat: Option[String] = Try(request.body.asFormUrlEncoded.get("lat").mkString).toOption
    val latText = optLat.getOrElse("")
    val optLon: Option[String] = Try(request.body.asFormUrlEncoded.get("lon").mkString).toOption
    val lonText = optLon.getOrElse("")
    val historical  : Boolean = Try(request.body.asFormUrlEncoded.get("historical").mkString.toBoolean).getOrElse(true)
    if (addressText.trim.isEmpty) {
      logger info "Radius Match with Empty search term"
      val viewToRender = uk.gov.ons.addressIndex.demoui.views.html.radiusMatch(
        radiusSearchForm = RadiusController.form,
        filter = None,
        rangekm = None,
        lat = None,
        lon = None,
        historical = historical,
        warningMessage = Some(messagesApi("radius.pleasesupply")),
        pageNum = 1,
        pageSize = pageSize,
        pageMax = maxPages,
        addressBySearchResponse = None,
        classification = None,
        version = version)
      Future.successful(
        Ok(viewToRender)
      )
    } else {
      Future.successful(
        Redirect(uk.gov.ons.addressIndex.demoui.controllers.routes.RadiusController.doMatchWithInput(addressText, Some(filterText), Some(rangeText), Some(latText), Some(lonText), Some(1), Some(historical)))
      )
    }
  }

  /**
    * Perform match by calling API with address string. Can be called directly via get or redirect from form
    *
    * @param input the term
    * @return result to view
    */
  def doMatchWithInput(input: String, filter: Option[String] = None, rangekm: Option[String] = None, lat: Option[String] = None, lon: Option[String] = None, page: Option[Int], historical: Option[Boolean]): Action[AnyContent] = Action.async { implicit request =>

  val refererUrl = request.uri
    request.session.get("api-key").map { apiKey =>
      val addressText = StringUtils.stripAccents(input)
      val filterText = StringUtils.stripAccents(filter.getOrElse(""))
      val limit = pageSize.toString()
      val pageNum = page.getOrElse(1)
      val offNum = (pageNum - 1) * pageSize
      val offset = offNum.toString
      val rangeString = rangekm.getOrElse("")
      val latString = lat.getOrElse("")
      val lonString = lon.getOrElse("")
      val historicalValue = historical.getOrElse(true)
      if (addressText.trim.isEmpty) {
        logger info ("Radius Match with expected search term missing")
        val viewToRender = uk.gov.ons.addressIndex.demoui.views.html.radiusMatch(
          radiusSearchForm = RadiusController.form,
          filter = None,
          historical = historicalValue,
          rangekm = None,
          lat = None,
          lon = None,
          warningMessage = Some(messagesApi("single.pleasesupply")),
          pageNum = 1,
          pageSize = pageSize,
          pageMax = maxPages,
          addressBySearchResponse = None,
          classification = None,
          version = version)
        Future.successful(
          Ok(viewToRender)
        )
      } else {
        //   logger info ("Single Match with supplied input address " + addressText)

        apiClient.addressQuery(
          AddressIndexSearchRequest(
            input = addressText,
            limit = limit,
            offset = offset,
            filter = filterText,
            historical = historicalValue,
            rangekm = rangeString,
            lat = latString,
            lon = lonString,
            id = UUID.randomUUID,
            apiKey = apiKey
          )
        ) map { resp: AddressBySearchResponseContainer =>
          val filledForm = RadiusController.form.fill(RadiusSearchForm(addressText,filterText,rangeString,latString,lonString, historicalValue))

          val nags = resp.response.addresses.flatMap(_.nag)
          val classCodes: Map[String, String] = nags.map(nag =>
            (nag.uprn, classHierarchy.analyseClassCode(nag.classificationCode))).toMap

          val warningMessage =
            if (resp.status.code == 200) None
            else Some(s"${resp.status.code} ${resp.status.message} : ${resp.errors.headOption.map(_.message).getOrElse("")}")


          val viewToRender = uk.gov.ons.addressIndex.demoui.views.html.radiusMatch(
            radiusSearchForm = filledForm,
            filter = None,
            rangekm = rangekm,
            lat = lat,
            lon = lon,
            historical = historicalValue,
            warningMessage = warningMessage,
            pageNum = pageNum,
            pageSize = pageSize,
            pageMax = maxPages,
            addressBySearchResponse = Some(resp.response),
            classification = Some(classCodes),
            version = version)
          Ok(viewToRender)
        }
      }
    }.getOrElse {
      Future.successful(Redirect(uk.gov.ons.addressIndex.demoui.controllers.routes.ApplicationHomeController.login()).withSession("referer" -> refererUrl))
    }
  }

}


object RadiusController {
  val form = Form(
    mapping(
      "address" -> text,
      "filter" -> text,
      "rangekm" -> text,
      "lat" -> text,
      "lon" -> text,
      "historical" -> boolean
    )(RadiusSearchForm.apply)(RadiusSearchForm.unapply)
  )
}