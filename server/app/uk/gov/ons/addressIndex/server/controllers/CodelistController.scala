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
  def codeList(): Action[AnyContent] = Action async {
    val codList = Tokens.codeList.map { clVal =>
      val fields = clVal.split("=")

      new AddressResponseCodelist(
        name = fields.headOption.getOrElse(""),
        description = fields.lastOption.getOrElse("")
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
  def codeListClassification(): Action[AnyContent] = Action async {
    val classList = Tokens.classList.map { classVal =>
      val fields = classVal.split("=")

      new AddressResponseClassification(
        code = fields.headOption.getOrElse(""),
        label = fields.lastOption.getOrElse("")
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
  def codeListCustodian(): Action[AnyContent] = Action async {
    val custList = Tokens.custodianList.map { custVal =>
      val fields = custVal.split(",")

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
  def codeListSource(): Action[AnyContent] = Action async {

    val sourceList = Tokens.sourceList.map { sourceVal =>
      val fields = sourceVal.split("=")

      new AddressResponseSource(
        fields.headOption.getOrElse(""),
        fields.lastOption.getOrElse("")
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
  def codeListLogicalStatus(): Action[AnyContent] = Action async {

    val logicalList = Tokens.logicalStatusList.map { logStatVal =>
      val fields = logStatVal.split("=")

      new AddressResponseLogicalStatus(
        fields.headOption.getOrElse(""),
        fields.lastOption.getOrElse("")
      )
    }

    val logicalListContainer = new AddressResponseLogicalStatusListContainer(logicalList)
    Future(Ok(Json.toJson(logicalListContainer)))
  }

}
