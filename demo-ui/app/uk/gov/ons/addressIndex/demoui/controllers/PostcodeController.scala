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
import uk.gov.ons.addressIndex.model.AddressIndexPostcodeRequest
import uk.gov.ons.addressIndex.model.server.response.postcode.AddressByPostcodeResponseContainer

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

/**
  * Controller class for a postcode to be matched
  * @param conf           conf
  * @param messagesApi    messageApi
  * @param apiClient      apiClient
  * @param ec             ec
  */
@Singleton
class PostcodeController @Inject()(
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
  def showPostcodeMatchPage(): Action[AnyContent] = Action.async { implicit request =>
    //  logger info ("SingleMatch: Rendering Single Match Page")
    val refererUrl = request.uri
    request.session.get("api-key").map { apiKey =>
      val viewToRender = uk.gov.ons.addressIndex.demoui.views.html.postcodeSearch(
        postcodeSearchForm = PostcodeController.form,
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
    val historical  : Boolean = Try(request.body.asFormUrlEncoded.get("historical").mkString.toBoolean).getOrElse(true)
    val startDateVal: Option[String] = Try(request.body.asFormUrlEncoded.get("startdate").mkString).toOption
    val endDateVal: Option[String] = Try(request.body.asFormUrlEncoded.get("enddate").mkString).toOption
    if (addressText.trim.isEmpty) {
      logger info "Postcode Match with Empty input address"
      val viewToRender = uk.gov.ons.addressIndex.demoui.views.html.postcodeMatch(
        postcodeSearchForm = PostcodeController.form,
        warningMessage = Some(messagesApi("postcode.pleasesupply")),
        pageNum = 1,
        pageSize = pageSize,
        addressByPostcodeResponse = None,
        classification = None,
        version = version)
      Future.successful(
        Ok(viewToRender)
      )
    } else {
      Future.successful(
        Redirect(uk.gov.ons.addressIndex.demoui.controllers.routes.PostcodeController.doMatchWithInput(addressText, Some(filterText), Some(1), Some(historical), Some(startDateVal.getOrElse("")), Some(endDateVal.getOrElse(""))))
      )
    }
  }

  /**
    * Perform match by calling API with address string. Can be called directly via get or redirect from form
    *
    * @param postcode the postcode
    * @return result to view
    */
  def doMatchWithInput(postcode: String, filter: Option[String], page: Option[Int], historical: Option[Boolean], startdate: Option[String], enddate: Option[String]): Action[AnyContent] = Action.async { implicit request =>

    val refererUrl = request.uri
    request.session.get("api-key").map { apiKey =>
      val addressText = StringUtils.stripAccents(postcode)
      val filterText = StringUtils.stripAccents(filter.getOrElse(""))
      val startDateVal =  StringUtils.stripAccents(startdate.getOrElse(""))
      val endDateVal =  StringUtils.stripAccents(enddate.getOrElse(""))
      val limit = pageSize.toString()
      val pageNum = page.getOrElse(1)
      val offNum = (pageNum - 1) * pageSize
      val offset = offNum.toString
      val historicalValue = historical.getOrElse(true)
      if (addressText.trim.isEmpty) {
        logger info "Postcode Match with expected input address missing"
        val viewToRender = uk.gov.ons.addressIndex.demoui.views.html.postcodeMatch(
          postcodeSearchForm = PostcodeController.form,
          warningMessage = Some(messagesApi("postcode.pleasesupply")),
          pageNum = 1,
          pageSize = pageSize,
          addressByPostcodeResponse = None,
          classification = None,
          version = version)
        Future.successful(
          Ok(viewToRender)
        )
      } else {
        //   logger info ("Postcode Match with supplied input address " + addressText)

        apiClient.postcodeQuery {
          AddressIndexPostcodeRequest(
            postcode = addressText,
            filter = filterText,
            historical = historicalValue,
            startdate = startDateVal,
            enddate = endDateVal,
            limit = limit,
            offset = offset,
            id = UUID.randomUUID,
            apiKey = apiKey,
            verbose = true
          )
        } map { resp: AddressByPostcodeResponseContainer =>
          val filledForm = PostcodeController.form.fill(PostcodeSearchForm(addressText,filterText, historicalValue, startDateVal, endDateVal))

          val classCodes: Map[String, String] = resp.response.addresses.map(address =>
            (address.uprn, classHierarchy.analyseClassCode(address.classificationCode))).toMap

          val warningMessage =
            if (resp.status.code == 200) None
            else Some(s"${resp.status.code} ${resp.status.message} : ${resp.errors.headOption.map(_.message).getOrElse("")}")


          val viewToRender = uk.gov.ons.addressIndex.demoui.views.html.postcodeMatch(
            postcodeSearchForm = filledForm,
            warningMessage = warningMessage,
            pageNum = pageNum,
            pageSize = pageSize,
            addressByPostcodeResponse = Some(resp.response),
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


object PostcodeController {
  val form = Form(
    mapping(
      "address" -> text,
      "filter" -> text,
      "historical" -> boolean,
      "startdate" -> text,
      "enddate" -> text
    )(PostcodeSearchForm.apply)(PostcodeSearchForm.unapply)
  )
}
