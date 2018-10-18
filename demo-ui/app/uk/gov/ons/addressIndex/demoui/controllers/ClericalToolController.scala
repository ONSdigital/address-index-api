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
import uk.gov.ons.addressIndex.demoui.modules.DemouiConfigModule
import uk.gov.ons.addressIndex.demoui.utils.{ClassHierarchy, RelativesExpander}
import uk.gov.ons.addressIndex.model.{AddressIndexSearchRequest, AddressIndexUPRNRequest}
import uk.gov.ons.addressIndex.demoui.modules.DemoUIAddressIndexVersionModule
import uk.gov.ons.addressIndex.model.server.response.address.AddressBySearchResponseContainer
import uk.gov.ons.addressIndex.model.server.response.uprn.AddressByUprnResponseContainer

import scala.concurrent.{ExecutionContext, Future}
import scala.language.implicitConversions
import scala.util.Try

/**
  * Controller class for a single address to be matched
  * @param conf
  * @param messagesApi
  * @param apiClient
  * @param ec
  */
@Singleton
class ClericalToolController @Inject()(
  val controllerComponents: ControllerComponents,
  conf : DemouiConfigModule,
  override val messagesApi: MessagesApi,
  langs: Langs,
  apiClient: AddressIndexClientInstance,
  classHierarchy: ClassHierarchy,
  relativesExpander: RelativesExpander,
  version: DemoUIAddressIndexVersionModule
  )(implicit ec: ExecutionContext) extends BaseController with I18nSupport {

  implicit val lang: Lang = langs.availables.head

  val logger = Logger("ClericalToolController")
  val pageSize = conf.config.limit
  val maxOff = conf.config.maxOffset
  val maxPages = (maxOff + pageSize - 1) / pageSize
  // val apiUrl = conf.config.apiURL.host + ":" + conf.config.apiURL.port + conf.config.apiURL.gatewayPath
  val apiUrl = conf.config.apiURL.ajaxHost + ":" + conf.config.apiURL.ajaxPort + conf.config.apiURL.gatewayPath

  /**
    * Present empty form for user to input address
    *
    * @return result to view
    */
  def showSingleMatchPage(): Action[AnyContent] = Action.async { implicit request =>
    val refererUrl = request.uri
    request.session.get("api-key").map { apiKey =>
  //  logger info ("Clerial Tool: Rendering Single Match Page")
    val viewToRender = uk.gov.ons.addressIndex.demoui.views.html.clericalTool(
      title = messagesApi("clerical.sfatext"),
      action = uk.gov.ons.addressIndex.demoui.controllers.routes.ClericalToolController.doMatch,
      singleSearchForm = SingleMatchController.form,
      warningMessage = None,
      query = "",
      filter = "",
      historical = false,
      dates = Map.empty,
      pageNum = 1,
      pageSize = pageSize,
      pageMax = maxPages,
      expandRow = -1,
      pagerAction = "clerical",
      addressBySearchResponse = None,
      classification = None,
      apiUrl = apiUrl,
      version = version,
      placeholder = messagesApi("clericalsearchform.placeholder"),
      labelFilter = messagesApi("clericalsearchform.labelfilter"),
      placeholderFilter = messagesApi("clericalsearchform.placeholderfilter"),
      apiKey = apiKey
    )
      Future.successful(Ok(viewToRender))
    }.getOrElse {
      Future.successful(Redirect(uk.gov.ons.addressIndex.demoui.controllers.routes.ApplicationHomeController.login()).withSession("referer" -> refererUrl))
    }
  }

  /**
    * Accept posted form, deal with empty address or pass on to MatchWithInput
    *
    * @return result to view or redirect
    */
  def doMatch(): Action[AnyContent] = Action { implicit request =>
    val addressText = Try(request.body.asFormUrlEncoded.get("address").mkString).getOrElse("")
    val filterText = Try(request.body.asFormUrlEncoded.get("filter").mkString).getOrElse("")
    val historical  : Boolean = Try(request.body.asFormUrlEncoded.get("historical").mkString.toBoolean).getOrElse(true)
    val startDateVal = Try(request.body.asFormUrlEncoded.get("startdate").mkString).getOrElse("")
    val endDateVal = Try(request.body.asFormUrlEncoded.get("enddate").mkString).getOrElse("")
    val optmatchthreshold: Option[Int] = Try(request.body.asFormUrlEncoded.get("matchthreshold").mkString.toInt).toOption
    val matchthresholdValue = optmatchthreshold.getOrElse(5)
    if (addressText.trim.isEmpty) {
      logger.info("Clerical Tool with Empty input address")
      val viewToRender = uk.gov.ons.addressIndex.demoui.views.html.clericalTool(
        title = messagesApi("clerical.sfatext"),
        action = uk.gov.ons.addressIndex.demoui.controllers.routes.ClericalToolController.doMatch,
        singleSearchForm = SingleMatchController.form,
        warningMessage = Some(messagesApi("single.pleasesupply")),
        query = "",
        filter = "",
        historical = historical,
        dates = Map.empty,
        pageNum = 1,
        pageSize = pageSize,
        pageMax = maxPages,
        expandRow = -1,
        pagerAction = "clerical",
        addressBySearchResponse = None,
        classification = None,
        apiUrl = apiUrl,
        version = version,
        placeholder = messagesApi("clericalsearchform.placeholder"),
        labelFilter = messagesApi("clericalsearchform.labelfilter"),
        placeholderFilter = messagesApi("clericalsearchform.placeholderfilter"),
        apiKey = ""
      )
        Ok(viewToRender)
    } else if (Try(addressText.toLong).isSuccess) {
        Redirect(uk.gov.ons.addressIndex.demoui.controllers.routes.ClericalToolController.doUprnWithInput(addressText.toLong, Some(filterText), Some(historical), Some(matchthresholdValue), Some(startDateVal), Some(endDateVal)))
    } else {
        Redirect(uk.gov.ons.addressIndex.demoui.controllers.routes.ClericalToolController.doMatchWithInput(addressText, Some(filterText), Some(1), Some(-1), Some(historical), Some(matchthresholdValue), Some(startDateVal), Some(endDateVal)))
    }
  }

  /**
    * Perform match by calling API with address string. Can be called directly via get or redirect from form
    *
    * @param input
    * @return result to view
    */
  def doMatchWithInput(input: String, filter: Option[String], page: Option[Int], expand: Option[Int], historical: Option[Boolean], matchthreshold: Option[Int], startdate: Option[String], enddate: Option[String]): Action[AnyContent] = Action.async { implicit request =>
    val refererUrl = request.uri
    request.session.get("api-key").map { apiKey =>
 //     generateClericalView(input, page, expand, messagesApi("clerical.sfatext"), uk.gov.ons.addressIndex.demoui.controllers.routes.ClericalToolController.doMatch, "clerical", messagesApi("clericalsearchform.placeholder"), apiKey)
      val addressText = StringUtils.stripAccents(input)
      val filterText = StringUtils.stripAccents(filter.getOrElse(""))
      val startDateVal =  StringUtils.stripAccents(startdate.getOrElse(""))
      val endDateVal =  StringUtils.stripAccents(enddate.getOrElse(""))
      val historicalValue = historical.getOrElse(true)
      val matchthresholdValue = matchthreshold.getOrElse(5)
      val expandr = expand.getOrElse(-1)
      val limit = pageSize.toString()
      val pageNum = page.getOrElse(1)
      val offNum = (pageNum - 1) * pageSize
      val offset = offNum.toString
      if (addressText.trim.isEmpty) {
        logger info ("Clerical Tool with expected input address missing")
        val viewToRender = uk.gov.ons.addressIndex.demoui.views.html.clericalTool(
          title = messagesApi("clerical.sfatext"),
          action = uk.gov.ons.addressIndex.demoui.controllers.routes.ClericalToolController.doMatch,
          singleSearchForm = SingleMatchController.form,
          warningMessage = Some(messagesApi("single.pleasesupply")),
          query = "",
          filter = "",
          historical = historicalValue,
          dates = Map.empty,
          pageNum = 1,
          pageSize = pageSize,
          pageMax = maxPages,
          expandRow = -1,
          pagerAction = "clerical",
          addressBySearchResponse = None,
          classification = None,
          apiUrl = apiUrl,
          version = version,
          placeholder = messagesApi("clericalsearchform.placeholder"),
          labelFilter = messagesApi("clericalsearchform.labelfilter"),
          placeholderFilter = messagesApi("clericalsearchform.placeholderfilter"),
          apiKey = apiKey
        )

        Future.successful(
          Ok(viewToRender)
        )
      } else {
   //     logger info ("Clerical Tool with supplied input address " + addressText)
        apiClient.addressQuery(
          AddressIndexSearchRequest(
            input = addressText,
            filter = filterText,
            historical = historicalValue,
            matchthreshold = matchthresholdValue,
            startdate = startDateVal,
            enddate = endDateVal,
            limit = limit,
            rangekm = "",
            lat = "50.705948",
            lon = "-3.5091076",
            offset = offset,
            id = UUID.randomUUID,
            apiKey = apiKey,
            verbose = true
          )
        ) map { resp: AddressBySearchResponseContainer =>
          val filledForm = SingleMatchController.form.fill(SingleSearchForm(addressText, filterText, historicalValue, matchthresholdValue, startDateVal, endDateVal))

          val nags = resp.response.addresses.flatMap(_.nag)
          val classCodes: Map[String, String] = nags.map(nag =>
            (nag(0).uprn, classHierarchy.analyseClassCode(nag(0).classificationCode))).toMap

          val warningMessage =
            if (resp.status.code == 200) None
            else Some(s"${resp.status.code} ${resp.status.message} : ${resp.errors.headOption.map(_.message).getOrElse("")}")


          Ok(uk.gov.ons.addressIndex.demoui.views.html.clericalTool(
            title = messagesApi("clerical.sfatext"),
            action = uk.gov.ons.addressIndex.demoui.controllers.routes.ClericalToolController.doMatch,
            singleSearchForm = filledForm,
            warningMessage = warningMessage,
            query = "",
            filter = "",
            historical = historicalValue,
            dates = Map.empty,
            pageNum = pageNum,
            pageSize = pageSize,
            pageMax = maxPages,
            expandRow = expandr,
            pagerAction = "clerical",
            addressBySearchResponse = Some(resp.response),
            classification = Some(classCodes),
            apiUrl = apiUrl,
            version = version,
            placeholder = messagesApi("debugsearchform.placeholder"),
            labelFilter = messagesApi("clericalsearchform.labelfilter"),
            placeholderFilter = messagesApi("clericalsearchform.placeholderfilter"),
            apiKey = apiKey
          ))
        }
      }
    }.getOrElse {
      Future.successful(Redirect(uk.gov.ons.addressIndex.demoui.controllers.routes.ApplicationHomeController.login()).withSession("referer" -> refererUrl))
    }
  }

  /** This shared method can't be used for now as it breaks Welsh langugage - might be able to reinstate later
  private def generateClericalView(input: String, page: Option[Int], expand: Option[Int], title: String, action: Call,
    pagerAction: String, placeholder: String, apiKey: String, query: String = ""): Future[Result] = {
    val addressText = input
    val expandr = expand.getOrElse(-1)
    val limit = pageSize.toString()
    val pageNum = page.getOrElse(1)
    val offNum = (pageNum - 1) * pageSize
    val offset = offNum.toString
    if (addressText.trim.isEmpty) {
      logger info ("Clerical Tool with expected input address missing")
      val viewToRender = uk.gov.ons.addressIndex.demoui.views.html.clericalTool(
        title = title,
        action = action,
        singleSearchForm = SingleMatchController.form,
        warningMessage = Some(messagesApi("single.pleasesupply")),
        query = "",
        pageNum = 1,
        pageSize = pageSize,
        pageMax = maxPages,
        expandRow = -1,
        pagerAction = pagerAction,
        addressBySearchResponse = None,
        classification = None,
        apiUrl = apiUrl,
        version = version,
        placeholder = placeholder
      )

      Future.successful(
        Ok(viewToRender)
      )
    } else {
      logger info ("Clerical Tool with supplied input address " + addressText)
      apiClient.addressQuery(
        AddressIndexSearchRequest(
          input = addressText,
          limit = limit,
          offset = offset,
          id = UUID.randomUUID,
          apiKey = apiKey
        )
      ) map { resp: AddressBySearchResponseContainer =>
        val filledForm = SingleMatchController.form.fill(SingleSearchForm(addressText))

        val nags = resp.response.addresses.flatMap(_.nag)
        val classCodes: Map[String, String] = nags.map(nag =>
          (nag.uprn, classHierarchy.analyseClassCode(nag.classificationCode))).toMap

        val warningMessage =
          if (resp.status.code == 200) None
          else Some(s"${resp.status.code} ${resp.status.message} : ${resp.errors.headOption.map(_.message).getOrElse("")}")


        Ok(uk.gov.ons.addressIndex.demoui.views.html.clericalTool(
          title = title,
          action = action,
          singleSearchForm = filledForm,
          warningMessage = warningMessage,
          query = query,
          pageNum = pageNum,
          pageSize = pageSize,
          pageMax = maxPages,
          expandRow = expandr,
          pagerAction = pagerAction,
          addressBySearchResponse = Some(resp.response),
          classification = Some(classCodes),
          apiUrl = apiUrl,
          version = version,
          placeholder = messagesApi("debugsearchform.placeholder")
        ))
      }
    }
  }
*/
  /**
    * Perform match by calling API with address string. Can be called directly via get or redirect from form
    * @param input
    * @return result to view
    */
  def doUprnWithInput(input : Long, filter: Option[String], historical: Option[Boolean], matchthreshold: Option[Int], startdate: Option[String], enddate: Option[String]) : Action[AnyContent] = Action.async { implicit request =>
    val refererUrl = request.uri
    val historicalValue = historical.getOrElse(true)
    val matchthresholdValue = matchthreshold.getOrElse(5)
    val startDateVal =  StringUtils.stripAccents(startdate.getOrElse(""))
    val endDateVal =  StringUtils.stripAccents(enddate.getOrElse(""))
    request.session.get("api-key").map { apiKey =>
   //   logger info("UPRN with supplied input address " + input)
      apiClient.uprnQuery(
        AddressIndexUPRNRequest(
          uprn = input,
          id = UUID.randomUUID,
          apiKey = apiKey,
          historical = historicalValue,
          startdate = startDateVal,
          enddate = endDateVal,
          verbose = true
        )
      ) map { resp: AddressByUprnResponseContainer =>
        val filledForm = SingleMatchController.form.fill(SingleSearchForm(input.toString, filter.getOrElse(""), historicalValue, matchthresholdValue, startDateVal, endDateVal))

        val nags = resp.response.address.flatMap(_.nag)
        val classCodes: Map[String, String] = nags.map(nag =>
          (nag(0).uprn , classHierarchy.analyseClassCode(nag(0).classificationCode))).toMap

        val warningMessage =
          if (resp.status.code == 200) None
          else Some(s"${resp.status.code} ${resp.status.message} : ${resp.errors.headOption.map(_.message).getOrElse("")}")


        val viewToRender = uk.gov.ons.addressIndex.demoui.views.html.uprnResult(
          singleSearchForm = filledForm,
          filter = None,
          historical = historicalValue,
          startdate = Some(startDateVal),
          enddate = Some(endDateVal),
          warningMessage = warningMessage,
          addressByUprnResponse = Some(resp.response),
          classification = Some(classCodes),
          version = version,
          isClerical = true,
          apiUrl = apiUrl,
          apiKey = apiKey
        )
        Ok(viewToRender)
      }
    }.getOrElse {
      Future.successful(Redirect(uk.gov.ons.addressIndex.demoui.controllers.routes.ApplicationHomeController.login()).withSession("referer" -> refererUrl))
    }
  }


  /**
    * Perform match by calling API with address string. Can be called directly via get or redirect from form
    * @param input
    * @return result to view
    */
  def doGetResultClerical(input : String, historical: Option[Boolean], matchthreshold: Option[Int], startdate: Option[String], enddate: Option[String]) : Action[AnyContent] = Action.async { implicit request =>
    val refererUrl = request.uri
    request.session.get("api-key").map { apiKey =>
      //   logger info("UPRN with supplied input address " + input)
      val addressText = StringUtils.stripAccents(input)
      val numericUPRN = BigInt(addressText)
      val historicalValue = historical.getOrElse(true)
      val matchthresholdValue = matchthreshold.getOrElse(5)
      val startDateVal =  StringUtils.stripAccents(startdate.getOrElse(""))
      val endDateVal =  StringUtils.stripAccents(enddate.getOrElse(""))

      apiClient.uprnQuery(
        AddressIndexUPRNRequest(
          uprn = numericUPRN,
          id = UUID.randomUUID,
          apiKey = apiKey,
          historical = historicalValue,
          startdate = startDateVal,
          enddate = endDateVal,
          verbose = true
        )
      ) flatMap { resp: AddressByUprnResponseContainer =>
        val filledForm = SingleMatchController.form.fill(SingleSearchForm(input.toString,"", historicalValue, matchthresholdValue, startDateVal, endDateVal))

        val nags = resp.response.address.flatMap(_.nag)
        val classCodes: Map[String, String] = nags.map(nag =>
          (nag(0).uprn , classHierarchy.analyseClassCode(nag(0).classificationCode))).toMap

        val warningMessage =
          if (resp.status.code == 200) None
          else Some(s"${resp.status.code} ${resp.status.message} : ${resp.errors.headOption.map(_.message).getOrElse("")}")

        val rels = resp.response.address.map(_.relatives)
        val futExpandedRels = relativesExpander.futExpandRelatives(apiKey, rels.get.getOrElse(Seq())).recover {
          case _: Throwable => Seq()
        }

        futExpandedRels.map { expandedRels =>
          // logger info("expanded rels = " + expandedRels.toString())
          val viewToRender = uk.gov.ons.addressIndex.demoui.views.html.result(
            singleSearchForm = filledForm,
            filter = None,
            historical = false,
            warningMessage = warningMessage,
            startdate = Some(startDateVal),
            enddate = Some(endDateVal),
            addressByUprnResponse = Some(resp.response),
            classification = Some(classCodes),
            expandedRels = Some(expandedRels),
            version = version,
            isClerical = true,
            apiUrl = apiUrl,
            apiKey = apiKey
          )
          Ok(viewToRender)
        }
      }
    }.getOrElse {
      Future.successful(Redirect(uk.gov.ons.addressIndex.demoui.controllers.routes.ApplicationHomeController.login()).withSession("referer" -> refererUrl))
    }
  }



  def showQuery(): Action[AnyContent] = Action.async {implicit request =>
    val refererUrl = request.uri
    request.session.get("api-key").map { apiKey =>
    val viewToRender = uk.gov.ons.addressIndex.demoui.views.html.clericalTool(
      title = messagesApi("debug.sfatext"),
      action = uk.gov.ons.addressIndex.demoui.controllers.routes.ClericalToolController.doShowQuery,
      singleSearchForm = SingleMatchController.form,
      warningMessage = None,
      query = "",
      filter = "",
      historical = false,
      dates = Map.empty,
      pageNum = 1,
      pageSize = pageSize,
      pageMax = maxPages,
      expandRow = -1,
      pagerAction = "debug",
      addressBySearchResponse = None,
      classification = None,
      apiUrl = apiUrl,
      version = version,
      placeholder = messagesApi("debugsearchform.placeholder"),
      labelFilter = messagesApi("clericalsearchform.labelfilter"),
      placeholderFilter = messagesApi("clericalsearchform.placeholderfilter"),
      apiKey = apiKey
    )
      Future.successful(
        Ok(viewToRender)
      )
    }.getOrElse {
      Future.successful(Redirect(uk.gov.ons.addressIndex.demoui.controllers.routes.ApplicationHomeController.login()).withSession("referer" -> refererUrl))
    }
  }

  def doShowQuery(): Action[AnyContent] = Action { implicit request =>

    val input: String = Try(request.body.asFormUrlEncoded.get("address").mkString).getOrElse("")
    val filter: String = Try(request.body.asFormUrlEncoded.get("filter").mkString).getOrElse("")
    val historical  : Boolean = Try(request.body.asFormUrlEncoded.get("historical").mkString.toBoolean).getOrElse(false)
    val optmatchthreshold: Option[Int] = Try(request.body.asFormUrlEncoded.get("matchthreshold").mkString.toInt).toOption
    val matchthreshold = optmatchthreshold.getOrElse(5)
    val startDateVal: String = Try(request.body.asFormUrlEncoded.get("startdate").mkString).getOrElse("")
    val endDateVal: String = Try(request.body.asFormUrlEncoded.get("enddate").mkString).getOrElse("")
    Redirect(uk.gov.ons.addressIndex.demoui.controllers.routes.ClericalToolController.showQueryWithInput(input, Some(filter), Some(1), Some(-1), Some(historical), Some(matchthreshold), Some(startDateVal), Some(endDateVal)))

  }

  def showQueryWithInput(input: String, filter: Option[String], page: Option[Int], expand: Option[Int], historical: Option[Boolean], matchthreshold: Option[Int], startdate: Option[String], enddate: Option[String]): Action[AnyContent] = Action.async { implicit request =>
    val refererUrl = request.uri
    request.session.get("api-key").map { apiKey =>
      apiClient.showQuery(input, filter.getOrElse(""), startdate.getOrElse(""), enddate.getOrElse(""), apiKey).flatMap{ query =>
   //     generateClericalView(input, page, expand, messagesApi("debug.sfatext"),  uk.gov.ons.addressIndex.demoui.controllers.routes.ClericalToolController.doShowQuery, "debug", messagesApi("debugsearchform.placeholder"), apiKey, query)
        val addressText = StringUtils.stripAccents(input)
        val filterText = StringUtils.stripAccents(filter.getOrElse(""))
        val startDateVal = StringUtils.stripAccents(startdate.getOrElse(""))
        val endDateVal = StringUtils.stripAccents(enddate.getOrElse(""))
        val expandr = expand.getOrElse(-1)
        val limit = pageSize.toString()
        val pageNum = page.getOrElse(1)
        val offNum = (pageNum - 1) * pageSize
        val offset = offNum.toString
        val historicalValue = historical.getOrElse(true)
        val matchthresholdValue = matchthreshold.getOrElse(5)
        if (addressText.trim.isEmpty) {
          logger info ("Clerical Tool with expected input address missing")
          val viewToRender = uk.gov.ons.addressIndex.demoui.views.html.clericalTool(
            title = messagesApi("debug.sfatext"),
            action = uk.gov.ons.addressIndex.demoui.controllers.routes.ClericalToolController.doShowQuery,
            singleSearchForm = SingleMatchController.form,
            warningMessage = Some(messagesApi("single.pleasesupply")),
            query = "",
            filter = filterText,
            historical = historicalValue,
            dates = Map("startdate" -> startDateVal, "enddate" -> endDateVal), // Avoids the 22 arg limit
            pageNum = 1,
            pageSize = pageSize,
            pageMax = maxPages,
            expandRow = -1,
            pagerAction = "debug",
            addressBySearchResponse = None,
            classification = None,
            apiUrl = apiUrl,
            version = version,
            placeholder = messagesApi("debugsearchform.placeholder"),
            labelFilter = messagesApi("clericalsearchform.labelfilter"),
            placeholderFilter = messagesApi("clericalsearchform.placeholderfilter"),
            apiKey = apiKey
          )

          Future.successful(
            Ok(viewToRender)
          )
        } else {
          logger info ("Clerical Tool with supplied input address " + addressText)
          apiClient.addressQuery(
            AddressIndexSearchRequest(
              input = addressText,
              filter = filterText,
              historical = historicalValue,
              matchthreshold = matchthresholdValue,
              startdate = startDateVal,
              enddate = endDateVal,
              limit = limit,
              rangekm = "",
              lat = "50.705948",
              lon = "-3.5091076",
              offset = offset,
              id = UUID.randomUUID,
              apiKey = apiKey,
              verbose = true
            )
          ) map { resp: AddressBySearchResponseContainer =>
            val filledForm = SingleMatchController.form.fill(SingleSearchForm(addressText, filterText, historicalValue, matchthresholdValue, startDateVal, endDateVal))

            val nags = resp.response.addresses.flatMap(_.nag)
            val classCodes: Map[String, String] = nags.map(nag =>
              (nag(0).uprn, classHierarchy.analyseClassCode(nag(0).classificationCode))).toMap

            val warningMessage =
              if (resp.status.code == 200) None
              else Some(s"${resp.status.code} ${resp.status.message} : ${resp.errors.headOption.map(_.message).getOrElse("")}")


            Ok(uk.gov.ons.addressIndex.demoui.views.html.clericalTool(
              title = messagesApi("debug.sfatext"),
              action = uk.gov.ons.addressIndex.demoui.controllers.routes.ClericalToolController.doShowQuery,
              singleSearchForm = filledForm,
              warningMessage = warningMessage,
              query = query,
              filter = filterText,
              historical = historicalValue,
              dates = Map("startdate" -> startDateVal, "enddate" -> endDateVal),
              pageNum = pageNum,
              pageSize = pageSize,
              pageMax = maxPages,
              expandRow = expandr,
              pagerAction = "debug",
              addressBySearchResponse = Some(resp.response),
              classification = Some(classCodes),
              apiUrl = apiUrl,
              version = version,
              placeholder = messagesApi("debugsearchform.placeholder"),
              labelFilter = messagesApi("clericalsearchform.labelfilter"),
              placeholderFilter = messagesApi("clericalsearchform.placeholderfilter"),
              apiKey = apiKey
            ))
          }
        }
      }
    }.getOrElse {
      Future.successful(Redirect(uk.gov.ons.addressIndex.demoui.controllers.routes.ApplicationHomeController.login()).withSession("referer" -> refererUrl))
    }
  }
}

object ClericalToolController {
  val form = Form(
    mapping(
      "address" -> text,
      "filter" -> text,
      "historical" -> boolean,
      "matchthreshold" -> number,
      "startdate" -> text,
      "enddate" -> text
    )(SingleSearchForm.apply)(SingleSearchForm.unapply)
  )
}
