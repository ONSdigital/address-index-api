package uk.gov.ons.addressIndex.server.controllers

import javax.inject.{Inject, Singleton}
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import uk.gov.ons.addressIndex.model.server.response.address._
import uk.gov.ons.addressIndex.model.server.response.codelists.{AddressResponseCodelist, AddressResponseCodelistListContainer}
import uk.gov.ons.addressIndex.parsers.Tokens
import uk.gov.ons.addressIndex.server.modules.response.Response
import uk.gov.ons.addressIndex.server.modules.validation.CodelistControllerValidation
import uk.gov.ons.addressIndex.server.modules.{ConfigModule, ElasticsearchRepository, ParserModule, VersionModule}
import uk.gov.ons.addressIndex.server.utils.APIThrottler

import scala.concurrent.{ExecutionContext, Future}
import java.nio.charset.Charset

@Singleton
class CodelistController @Inject()(val controllerComponents: ControllerComponents,
  esRepo: ElasticsearchRepository,
  parser: ParserModule,
  conf: ConfigModule,
  versionProvider: VersionModule,
  overloadProtection: APIThrottler,
  codelistValidation: CodelistControllerValidation
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
    println("REG-1985 : Inside codeListClassification")
    println("REG-1985 : Tokens.classList " + Tokens.classList)
    println("REG-1985 : ********* defaultCharSet " + Charset.defaultCharset())
    println("REG-1985 : ********* System.prop " + System.getProperty("file.encoding"))

    val classList = Tokens.classList.map { classval => {

      println("REG-1985 : classval " + classval)

      new AddressResponseClassification(
        code = classval.split("=").headOption.getOrElse(""),
        label = classval.split("=").lastOption.getOrElse("")
      )
    }
    }
    println("REG-1985 : After creation of codeList")

    val codListContainer = new AddressResponseClassificationListContainer(classList)
    println("REG-1985 : Before Future")

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
