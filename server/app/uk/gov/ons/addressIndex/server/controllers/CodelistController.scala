package uk.gov.ons.addressIndex.server.controllers

import javax.inject.{Inject, Singleton}
import play.api.Logger
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, BaseController, ControllerComponents}
import uk.gov.ons.addressIndex.model.server.response._
import uk.gov.ons.addressIndex.parsers.Tokens
import uk.gov.ons.addressIndex.server.modules.{ConfigModule, ElasticsearchRepository, ParserModule, VersionModule}
import uk.gov.ons.addressIndex.server.modules.response.{Response, PostcodeResponse}
import uk.gov.ons.addressIndex.server.modules.validation.{CodelistValidation, PostcodeValidation}
import uk.gov.ons.addressIndex.server.utils.{APILogging, Overload}
import uk.gov.ons.addressIndex.server.utils.impl.{AddressLogMessage, AddressLogging}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CodelistController @Inject()(
  val controllerComponents: ControllerComponents,
  esRepo: ElasticsearchRepository,
  parser: ParserModule,
  conf: ConfigModule,
  versionProvider: VersionModule,
  overloadProtection: Overload,
  codelistValidation: CodelistValidation
)(implicit ec: ExecutionContext)
  extends PlayHelperController(versionProvider) with Response with APILogging[AddressLogMessage] {

  override def trace(message: AddressLogMessage): Unit = AddressLogging trace message
  override def log(message: AddressLogMessage): Unit = AddressLogging trace message
  override def debug(message: AddressLogMessage): Unit = AddressLogging debug message

  lazy val logger = Logger("address-index-server:CodelistController")

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

      new AddressResponseCustodian(
        custval.split(",").lift(0).getOrElse(""),
        custval.split(",").lift(1).getOrElse(""),
        custval.split(",").lift(2).getOrElse(""),
        custval.split(",").lift(3).getOrElse(""),
        custval.split(",").lift(4).getOrElse(""),
        custval.split(",").lift(5).getOrElse("")
      )
    }

    val custListContainer = new AddressResponseCustodianListContainer(custList)
    Future(Ok(Json.toJson(custListContainer)))
  }

  /**
    * Classification List API
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
    * Classification List API
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
