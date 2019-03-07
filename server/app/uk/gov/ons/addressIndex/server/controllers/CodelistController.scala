package uk.gov.ons.addressIndex.server.controllers

import javax.inject.{Inject, Singleton}
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import uk.gov.ons.addressIndex.model.server.response.address._
import uk.gov.ons.addressIndex.model.server.response.codelists.{AddressResponseCodelist, AddressResponseCodelistListContainer}
import uk.gov.ons.addressIndex.parsers.Tokens
import uk.gov.ons.addressIndex.server.modules.VersionModule
import uk.gov.ons.addressIndex.server.modules.response.Response

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CodelistController @Inject()(val controllerComponents: ControllerComponents,
                                   versionProvider: VersionModule,
                                  )(implicit ec: ExecutionContext)
  extends PlayHelperController(versionProvider) with Response {

  /**
    * Codelist List API
    *
    * @return Json response with codelist
    */
  def codeList(): Action[AnyContent] = Action async { implicit req =>
    val codList = Tokens.codeList.map { clval =>

      new AddressResponseCodelist(
        name = clval.split("=").headOption.getOrElse(""),
        description = clval.split("=").lastOption.getOrElse("")
      )
    }

    val codeListContainer = new AddressResponseCodelistListContainer(codList)

    Future(Ok(Json.toJson(codeListContainer)))
  }

  /**
    * Classification List API
    *
    * @return Json response with codelist
    */
  def codeListClassification(): Action[AnyContent] = Action async { implicit req =>
    val classList = Tokens.classList.map { classval =>

      new AddressResponseClassification(
        code = classval.split("=").headOption.getOrElse(""),
        label = classval.split("=").lastOption.getOrElse("")
      )
    }

    val codListContainer = new AddressResponseClassificationListContainer(classList)
    Future(Ok(Json.toJson(codListContainer)))
  }

  /**
    * Custodian List API
    *
    * @return Json response with codelist
    */
  def codeListCustodian(): Action[AnyContent] = Action async { implicit req =>
    val custList = Tokens.custodianList.map { custval =>
      val fields = custval.split(",")

      new AddressResponseCustodian(
        fields.lift(0).getOrElse(""),
        fields.lift(1).getOrElse(""),
        fields.lift(2).getOrElse(""),
        fields.lift(3).getOrElse(""),
        fields.lift(4).getOrElse(""),
        fields.lift(5).getOrElse("")
      )
    }

    val custListContainer = new AddressResponseCustodianListContainer(custList)
    Future(Ok(Json.toJson(custListContainer)))
  }

  /**
    * Source List API
    *
    * @return Json response with codelist
    */
  def codeListSource(): Action[AnyContent] = Action async { implicit req =>

    val sourceList = Tokens.sourceList.map { sourceval =>
      new AddressResponseSource(
        sourceval.split("=").headOption.getOrElse(""),
        sourceval.split("=").lastOption.getOrElse("")
      )
    }

    val sourceListContainer = new AddressResponseSourceListContainer(sourceList)
    Future(Ok(Json.toJson(sourceListContainer)))
  }

  /**
    * Logical Status List API
    *
    * @return Json response with codelist
    */
  def codeListLogicalStatus(): Action[AnyContent] = Action async { implicit req =>

    val logicalList = Tokens.logicalStatusList.map { logstatval =>
      new AddressResponseLogicalStatus(
        logstatval.split("=").headOption.getOrElse(""),
        logstatval.split("=").lastOption.getOrElse("")
      )
    }

    val logicalListContainer = new AddressResponseLogicalStatusListContainer(logicalList)
    Future(Ok(Json.toJson(logicalListContainer)))
  }

}
