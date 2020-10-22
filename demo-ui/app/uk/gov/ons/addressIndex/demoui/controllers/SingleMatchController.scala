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
import uk.gov.ons.addressIndex.demoui.controllers
import uk.gov.ons.addressIndex.demoui.model._
import uk.gov.ons.addressIndex.demoui.modules.{DemoUIAddressIndexVersionModule, DemouiConfigModule}
import uk.gov.ons.addressIndex.demoui.utils.{ClassHierarchy, RelativesExpander}
import uk.gov.ons.addressIndex.demoui.views
import uk.gov.ons.addressIndex.model.server.response.address.AddressBySearchResponseContainer
import uk.gov.ons.addressIndex.model.server.response.partialaddress.{AddressByPartialAddressResponse, AddressByPartialAddressResponseContainer}
import uk.gov.ons.addressIndex.model.server.response.uprn.AddressByUprnResponseContainer
import uk.gov.ons.addressIndex.model.{AddressIndexPartialRequest, AddressIndexPartialRequestGcp, AddressIndexSearchRequest, AddressIndexUPRNRequest}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

/**
  * Controller class for a single address to be matched
  *
  * @param conf        conf reference
  * @param messagesApi messagesApi ref
  * @param apiClient   apiClient ref
  * @param ec          ec ref
  */
@Singleton
class SingleMatchController @Inject()(val controllerComponents: ControllerComponents,
                                      conf: DemouiConfigModule,
                                      override val messagesApi: MessagesApi,
                                      langs: Langs,
                                      apiClient: AddressIndexClientInstance,
                                      classHierarchy: ClassHierarchy,
                                      relativesExpander: RelativesExpander,
                                      version: DemoUIAddressIndexVersionModule
                                     )(implicit ec: ExecutionContext) extends BaseController with I18nSupport {

  implicit val lang: Lang = langs.availables.head

  val logger = Logger("SingleMatchController")
  val pageSize: Int = conf.config.limit
  val maxOff: Int = conf.config.maxOffset
  val maxPages: Int = (maxOff + pageSize - 1) / pageSize
  // val apiUrl = conf.config.apiURL.host + ":" + conf.config.apiURL.port + conf.config.apiURL.gatewayPath
  val apiUrl: String = conf.config.apiURL.ajaxHost + ":" + conf.config.apiURL.ajaxPort + conf.config.apiURL.gatewayPath
  val showNisra: Boolean = Try(conf.config.nisra.toBoolean).getOrElse(true)



  /**
    * Present empty form for user to input address
    *
    * @return result to view
    */
  def showSingleMatchPage(): Action[AnyContent] = Action.async { implicit request =>
    // logger info ("SingleMatch: Rendering Single Match Page")
    request.session.get("api-key").map { _ =>
      val viewToRender = views.html.singleSearch(
        singleSearchForm = SingleMatchController.form,
        version = version
      )
      Future.successful(Ok(viewToRender))
    }.getOrElse {
      Future.successful(Redirect(controllers.routes.ApplicationHomeController.login())
        .withSession("referer" -> request.uri))
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
    val rangeOpt = request.getQueryString("rangekm")
    val latOpt = request.getQueryString("lat")
    val lonOpt = request.getQueryString("lon")
    val historical: Boolean = Try(request.body.asFormUrlEncoded.get("historical").mkString.toBoolean).getOrElse(true)
    val optMatchThreshold: Option[Int] = Try(request.body.asFormUrlEncoded.get("matchthreshold").mkString.toInt).toOption
    val matchThresholdValue = optMatchThreshold.getOrElse(5)
    val startDateVal: Option[String] = Try(request.body.asFormUrlEncoded.get("startdate").mkString).toOption
    val endDateVal: Option[String] = Try(request.body.asFormUrlEncoded.get("enddate").mkString).toOption
    val epochVal: Option[String] = Try(request.body.asFormUrlEncoded.get("epoch").mkString).toOption
    val optFromSource: Option[String] = Try(request.body.asFormUrlEncoded.get("fromsource").mkString).toOption
    val fromSourceValue = optFromSource.getOrElse("all")
    val optPartial: Option[Boolean] = Try(request.body.asFormUrlEncoded.get("partial").mkString.toBoolean).toOption
    val partialValue = optPartial.getOrElse(false)

    if (addressText.trim.isEmpty) {
      logger info "Single Match with Empty input address"
      val viewToRender = views.html.singleMatch(
        singleSearchForm = SingleMatchController.form,
        rangekm = None,
        lat = None,
        lon = None,
        warningMessage = Some(messagesApi("single.pleasesupply")),
        pageNum = 1,
        pageSize = pageSize,
        addressBySearchResponse = None,
        classification = None,
        version = version,
        showNisra = showNisra)
      Future.successful(Ok(viewToRender))
    } else if (Try(addressText.toLong).isSuccess) {
      Future.successful(
        Redirect(controllers.routes.SingleMatchController
          .doGetUprn(addressText, Some(filterText), Some(historical), Some(matchThresholdValue), Some(startDateVal.getOrElse("")), Some(endDateVal.getOrElse("")), Some(epochVal.getOrElse(""))))
      )
    } else {
      Future.successful(
        Redirect(controllers.routes.SingleMatchController
          .doMatchWithInput(addressText, Some(filterText), Some(1), rangeOpt, latOpt, lonOpt, Some(historical), Some(matchThresholdValue), Some(fromSourceValue), Some(partialValue), Some(startDateVal.getOrElse("")), Some(endDateVal.getOrElse("")),Some(epochVal.getOrElse(""))))
      )
    }
  }

  /**
    * Perform match by calling API with address string. Can be called directly via get or redirect from form
    *
    * @param input Input value
    * @return result to view
    */
  def doMatchWithInput(input: String, filter: Option[String] = None, page: Option[Int], rangekm: Option[String] = None, lat: Option[String] = None, lon: Option[String] = None, historical: Option[Boolean] = None, matchthreshold: Option[Int] = None, fromsource: Option[String] = None, partial: Option[Boolean] = None, startdate: Option[String] = None, enddate: Option[String] = None, epoch: Option[String] = None): Action[AnyContent] = Action.async { implicit request =>
    request.session.get("api-key").map { apiKey =>
      val addressText = StringUtils.stripAccents(input)
      val filterText = StringUtils.stripAccents(filter.getOrElse(""))
      val historicalValue = historical.getOrElse(true)
      val epochVal = epoch.getOrElse("")
      val matchThresholdValue = matchthreshold.getOrElse(5)
      val startDateVal = StringUtils.stripAccents(startdate.getOrElse(""))
      val endDateVal = StringUtils.stripAccents(enddate.getOrElse(""))
      val limit = pageSize.toString
      val pageNum = page.getOrElse(1)
      val offNum = (pageNum - 1) * pageSize
      val offset = offNum.toString
      val rangeString = rangekm.getOrElse("")
      val latString = lat.getOrElse("50.705948")
      val lonString = lon.getOrElse("-3.5091076")
      val fromSourceValue = fromsource.getOrElse("all")
      val partialValue = partial.getOrElse(false)
      if (addressText.trim.isEmpty) {
        logger info "Single Match with expected input address missing"
        val viewToRender = views.html.singleMatch(
          singleSearchForm = SingleMatchController.form,
          rangekm = None,
          lat = None,
          lon = None,
          warningMessage = Some(messagesApi("single.pleasesupply")),
          pageNum = 1,
          pageSize = pageSize,
          addressBySearchResponse = None,
          classification = None,
          version = version,
          showNisra = showNisra)
        Future.successful(Ok(viewToRender))
      } else {
        // logger info ("Single Match with supplied input address " + addressText)
        if (partialValue) {
          apiClient.partialQuery(
            AddressIndexPartialRequest(
              partial = addressText,
              limit = limit,
              offset = offset,
              filter = filterText,
              historical = historicalValue,
              startdate = startDateVal,
              enddate = endDateVal,
              verbose = true,
              epoch = epochVal,
              fromsource = fromSourceValue,
              fallback = true,
              id = UUID.randomUUID,
              apiKey = apiKey
            )
          ) map { resp: AddressByPartialAddressResponseContainer =>
            val filledForm = SingleMatchController.form.fill(SingleSearchForm(addressText, filterText, historicalValue, matchThresholdValue, fromSourceValue, partialValue, epochVal, startDateVal, endDateVal))

            val classCodes: Map[String, String] = resp.response.addresses.map(address =>
              (address.uprn, classHierarchy.analyseClassCode(address.classificationCode))).toMap

            val warningMessage =
              if (resp.status.code == 200) None
              else Some(s"${resp.status.code} ${resp.status.message} : ${resp.errors.headOption.map(_.message).getOrElse("")}")

            val viewToRender = views.html.singleMatch(
              singleSearchForm = filledForm,
              rangekm = Some(""),
              lat = Some(""),
              lon = Some(""),
              warningMessage = warningMessage,
              pageNum = pageNum,
              pageSize = pageSize,
             addressBySearchResponse = Some(AddressByPartialAddressResponse.toAddressBySearchResponse(resp.response)),
          //   addressBySearchResponse = Some(resp.response),
              classification = Some(classCodes),
              version = version,
              showNisra = showNisra)
            Ok(viewToRender)
          }
        } else {
        apiClient.addressQuery(
          AddressIndexSearchRequest(
            input = addressText,
            limit = limit,
            offset = offset,
            filter = filterText,
            historical = historicalValue,
            matchthreshold = matchThresholdValue,
            startdate = startDateVal,
            enddate = endDateVal,
            rangekm = rangeString,
            lat = latString,
            lon = lonString,
            verbose = true,
            epoch = epochVal,
            fromsource = fromSourceValue,
            id = UUID.randomUUID,
            apiKey = apiKey
          )
        ) map { resp: AddressBySearchResponseContainer =>
          val filledForm = SingleMatchController.form.fill(SingleSearchForm(addressText, filterText, historicalValue, matchThresholdValue, fromSourceValue, partialValue, epochVal, startDateVal, endDateVal))

          val classCodes: Map[String, String] = resp.response.addresses.map(address =>
            (address.uprn, classHierarchy.analyseClassCode(address.classificationCode))).toMap

          val warningMessage =
            if (resp.status.code == 200) None
            else Some(s"${resp.status.code} ${resp.status.message} : ${resp.errors.headOption.map(_.message).getOrElse("")}")

          val viewToRender = views.html.singleMatch(
            singleSearchForm = filledForm,
            rangekm = rangekm,
            lat = lat,
            lon = lon,
            warningMessage = warningMessage,
            pageNum = pageNum,
            pageSize = pageSize,
            addressBySearchResponse = Some(resp.response),
            classification = Some(classCodes),
            version = version,
            showNisra = showNisra)
          Ok(viewToRender)
        }
        }
      }
    }.getOrElse {
      Future.successful(Redirect(controllers.routes.ApplicationHomeController.login())
        .withSession("referer" -> request.uri))
    }
  }

  /**
    * Perform match by calling API with address string. Can be called directly via get or redirect from form
    *
    * @param input Input value
    * @return result to view
    */
  def doGetUprn(input: String, filter: Option[String], historical: Option[Boolean], matchthreshold: Option[Int], startdate: Option[String], enddate: Option[String], epoch: Option[String]): Action[AnyContent] = Action.async { implicit request =>
    request.session.get("api-key").map { apiKey =>
      val addressText = StringUtils.stripAccents(input)
      val filterText = StringUtils.stripAccents(filter.getOrElse(""))
      val historicalValue = historical.getOrElse(true)
      val epochVal = epoch.getOrElse("")
      val matchThresholdValue = matchthreshold.getOrElse(5)
      val startDateVal = StringUtils.stripAccents(startdate.getOrElse(""))
      val endDateVal = StringUtils.stripAccents(enddate.getOrElse(""))
      if (addressText.trim.isEmpty) {
        logger info "UPRN with expected input address missing"
        val viewToRender = views.html.singleMatch(
          singleSearchForm = SingleMatchController.form,
          rangekm = None,
          lat = None,
          lon = None,
          warningMessage = Some(messagesApi("single.pleasesupply")),
          pageNum = 1,
          pageSize = pageSize,
          addressBySearchResponse = None,
          classification = None,
          version = version,
          showNisra = showNisra)
        Future.successful(Ok(viewToRender))
      } else {
        // logger info("UPRN with supplied input address " + addressText)
        val numericUPRN = BigInt(addressText)
        apiClient.uprnQuery(
          AddressIndexUPRNRequest(
            uprn = numericUPRN,
            id = UUID.randomUUID,
            historical = historicalValue,
            apiKey = apiKey,
            startdate = startDateVal,
            enddate = endDateVal,
            verbose = true,
            epoch = epochVal
          )
        ) map { resp: AddressByUprnResponseContainer =>
          val filledForm = SingleMatchController.form.fill(SingleSearchForm(addressText, filterText, historicalValue, matchThresholdValue, "all", partial = false, epochVal, startDateVal, endDateVal))

          val classCodes: Map[String, String] = resp.response.address.map(address =>
            (address.uprn, classHierarchy.analyseClassCode(address.classificationCode))).toMap

          val warningMessage =
            if (resp.status.code == 200) None
            else Some(s"${resp.status.code} ${resp.status.message} : ${resp.errors.headOption.map(_.message).getOrElse("")}")

          val viewToRender = views.html.uprnResult(
            singleSearchForm = filledForm,
            warningMessage = warningMessage,
            addressByUprnResponse = Some(resp.response),
            classification = Some(classCodes),
            version = version,
            isClerical = false
          )
          Ok(viewToRender)
        }
      }
    }.getOrElse {
      Future.successful(Redirect(controllers.routes.ApplicationHomeController.login())
        .withSession("referer" -> request.uri))
    }
  }

  def doMatchPartial(input: String, filter: Option[String] = None, fallback: Option[String] = None): Action[AnyContent] = Action.async {

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
    ).get.map(_.json) map { resp =>
      Ok(resp).as("application/json")
    }
  }

  /**
    * Perform match by calling API with address string. Can be called directly via get or redirect from form
    *
    * @param input Input value
    * @return result to view
    */
  def doGetResult(input: String, historical: Option[Boolean], startdate: Option[String], enddate: Option[String], epoch: Option[String]): Action[AnyContent] = Action.async { implicit request =>
    request.session.get("api-key").map { apiKey =>
      val addressText = StringUtils.stripAccents(input)
      val filterText = ""
      val historicalValue = historical.getOrElse(true)
      val epochVal = epoch.getOrElse("")
      val matchthresholdValue = 5
      val startDateVal = StringUtils.stripAccents(startdate.getOrElse(""))
      val endDateVal = StringUtils.stripAccents(enddate.getOrElse(""))
      if (addressText.trim.isEmpty) {
        logger info "Result with expected input address missing"
        val viewToRender = views.html.singleMatch(
          singleSearchForm = SingleMatchController.form,
          warningMessage = Some(messagesApi("single.pleasesupply")),
          pageNum = 1,
          rangekm = None,
          lat = None,
          lon = None,
          pageSize = pageSize,
          addressBySearchResponse = None,
          classification = None,
          version = version,
          showNisra = showNisra)
        Future.successful(Ok(viewToRender))
      } else {
        //   logger info("UPRN with supplied input address " + addressText)
        val numericUPRN = BigInt(addressText)
        apiClient.uprnQuery(
          AddressIndexUPRNRequest(
            uprn = numericUPRN,
            id = UUID.randomUUID,
            historical = historicalValue,
            apiKey = apiKey,
            startdate = startDateVal,
            enddate = endDateVal,
            verbose = true,
            epoch = epochVal
          )
        ) flatMap { resp: AddressByUprnResponseContainer =>
          val filledForm = SingleMatchController.form.fill(SingleSearchForm(addressText, filterText, historicalValue, matchthresholdValue, "all", partial = false, epochVal, startDateVal, endDateVal))

          val classCodes: Map[String, String] = resp.response.address.map(address =>
            (address.uprn, classHierarchy.analyseClassCode(address.classificationCode))).toMap

          val warningMessage =
            if (resp.status.code == 200) None
            else Some(s"${resp.status.code} ${resp.status.message} : ${resp.errors.headOption.map(_.message).getOrElse("")}")

          val rels = resp.response.address.map(_.relatives)
          val futExpandedRels = relativesExpander.futExpandRelatives(apiKey, rels.get.getOrElse(Seq())).recover {
            case exception =>
              logger.warn("relatives failed" + exception)
              Seq()
          }

          futExpandedRels.map { expandedRels =>
            //     logger info("expanded rels = " + expandedRels.toString())
            val viewToRender = views.html.result(
              singleSearchForm = filledForm,
              warningMessage = warningMessage,
              addressByUprnResponse = Some(resp.response),
              classification = Some(classCodes),
              expandedRels = Some(expandedRels),
              version = version,
              isClerical = false
            )
            Ok(viewToRender)
          }
        }
      }
    }.getOrElse {
      Future.successful(Redirect(controllers.routes.ApplicationHomeController.login())
        .withSession("referer" -> request.uri))
    }
  }

  /**
    * Perform match by calling API with address string. Can be called directly via get or redirect from form
    *
    * @param input Input value
    * @return result to view
    */
  def doGetResultClerical(input: String, historical: Boolean, startdate: Option[String], enddate: Option[String], epoch: Option[String]): Action[AnyContent] = Action.async { implicit request =>
    request.session.get("api-key").map { apiKey =>
      val addressText = StringUtils.stripAccents(input)
      val filterText = ""
      val matchthresholdValue = 5
      val epochVal = epoch.getOrElse("")
      val startDateVal = StringUtils.stripAccents(startdate.getOrElse(""))
      val endDateVal = StringUtils.stripAccents(enddate.getOrElse(""))
      if (addressText.trim.isEmpty) {
        logger info "Result with expected input address missing"
        val viewToRender = views.html.singleMatch(
          singleSearchForm = SingleMatchController.form,
          warningMessage = Some(messagesApi("single.pleasesupply")),
          pageNum = 1,
          rangekm = None,
          lat = None,
          lon = None,
          pageSize = pageSize,
          addressBySearchResponse = None,
          classification = None,
          version = version,
          showNisra = showNisra)
        Future.successful(Ok(viewToRender))
      } else {
        //   logger info("UPRN with supplied input address " + addressText)
        val numericUPRN = BigInt(addressText)
        apiClient.uprnQuery(
          AddressIndexUPRNRequest(
            uprn = numericUPRN,
            id = UUID.randomUUID,
            historical = historical,
            apiKey = apiKey,
            startdate = startDateVal,
            enddate = endDateVal,
            verbose = true,
            epoch = epochVal
          )
        ) flatMap { resp: AddressByUprnResponseContainer =>
          val filledForm = SingleMatchController.form.fill(SingleSearchForm(addressText, filterText, historical, matchthresholdValue, "all", partial = false, epochVal, startDateVal, endDateVal))

          val classCodes: Map[String, String] = resp.response.address.map(address =>
            (address.uprn, classHierarchy.analyseClassCode(address.classificationCode))).toMap

          val warningMessage =
            if (resp.status.code == 200) None
            else Some(s"${resp.status.code} ${resp.status.message} : ${resp.errors.headOption.map(_.message).getOrElse("")}")

          val rels = resp.response.address.map(_.relatives)
          val futExpandedRels = relativesExpander.futExpandRelatives(apiKey, rels.get.getOrElse(Seq())).recover {
            case _: Throwable => Seq()
          }

          futExpandedRels.map { expandedRels =>
            //logger info("expanded rels = " + expandedRels.toString())
            val viewToRender = views.html.result(
              singleSearchForm = filledForm,
              warningMessage = warningMessage,
              addressByUprnResponse = Some(resp.response),
              classification = Some(classCodes),
              expandedRels = Some(expandedRels),
              version = version,
              isClerical = true
            )
            Ok(viewToRender)
          }
        }
      }
    }.getOrElse {
      Future.successful(Redirect(controllers.routes.ApplicationHomeController.login())
        .withSession("referer" -> request.uri))
    }
  }
}


object SingleMatchController {
  val form = Form(
    mapping(
      "address" -> text,
      "filter" -> text,
      "historical" -> boolean,
      "matchthreshold" -> number,
      "fromsource" -> text,
      "partial" -> boolean,
      "epoch" -> text,
      "startdate" -> text,
      "enddate" -> text
    )(SingleSearchForm.apply)(SingleSearchForm.unapply)
  )
}
