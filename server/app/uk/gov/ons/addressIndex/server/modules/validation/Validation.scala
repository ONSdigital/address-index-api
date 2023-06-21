package uk.gov.ons.addressIndex.server.modules.validation

import java.text.SimpleDateFormat

import play.api.mvc.{RequestHeader, Result}
import uk.gov.ons.addressIndex.model.server.response.address._
import uk.gov.ons.addressIndex.server.model.dao.QueryValues
import uk.gov.ons.addressIndex.server.modules.response.Response
import uk.gov.ons.addressIndex.server.modules.{ConfigModule, VersionModule}
import uk.gov.ons.addressIndex.server.utils.AddressAPILogger

import scala.concurrent.Future
import scala.util.Try

abstract class Validation(implicit conf: ConfigModule, versionProvider: VersionModule)
  extends Object with Response {

  // lazy to avoid application crash at startup if ES is down
  override lazy val dataVersion: String = versionProvider.dataVersion
  override lazy val apiVersion: String = versionProvider.apiVersion
  override lazy val termsAndConditions: String = versionProvider.termsAndConditions
  override lazy val epochList: List[String] = versionProvider.epochList
  override lazy val epochDates: Map[String,String] = versionProvider.epochDates
  lazy val logger = AddressAPILogger("address-index-server:Validation")

  val missing: String = "missing"
  val invalid: String = "invalid"
  val valid: String = "valid"
  val notRequired: String = "not required"

  protected def invalidDate(date: String): Boolean = !date.isEmpty && Try(new SimpleDateFormat("yyyy-MM-dd").parse(date)).isFailure

  def validateKeyStatus(queryValues: QueryValues)(implicit request: RequestHeader): Option[Future[Result]] = {
    val apiKey = request.headers.get("authorization").getOrElse(missing)

    checkAPIkey(apiKey) match {
      case `missing` =>
        logger.systemLog(responsecode = "400",badRequestMessage = ApiKeyMissingError.message)
        Some(futureJsonUnauthorized(KeyMissing(queryValues)))
      case `invalid` =>
        logger.systemLog(responsecode = "400",badRequestMessage = ApiKeyInvalidError.message)
        Some(futureJsonUnauthorized(KeyInvalid(queryValues)))
      case _ => None
    }
  }

  /**
    * Method to validate api key
    *
    * @param apiKey the key to check
    * @return not required, valid, invalid or missing
    */
  protected def checkAPIkey(apiKey: String): String = {
    val keyRequired = conf.config.apiKeyRequired

    if (keyRequired) {
      val masterKey = conf.config.masterKey
      val apiKeyTest = apiKey.drop(apiKey.indexOf("_") + 1)

      apiKeyTest match {
        case key if key == missing => missing
        case key if key == masterKey => valid
        case _ => invalid
      }
    } else {
      notRequired
    }
  }

  def validateSource(queryValues: QueryValues)(implicit request: RequestHeader): Option[Future[Result]] = {
    val source = request.headers.get("Source").getOrElse(missing)

    checkSource(source) match {
      case `missing` =>
        logger.systemLog(responsecode = "400",badRequestMessage = SourceMissingError.message)
        Some(futureJsonUnauthorized(SourceMissing(queryValues)))
      case `invalid` =>
        logger.systemLog(responsecode = "400",badRequestMessage = SourceInvalidError.message)
        Some(futureJsonUnauthorized(SourceInvalid(queryValues)))
      case _ => None
    }
  }

  /**
    * Method to check source of query
    *
    * @param source the source to check
    * @return not required, valid, invalid or missing
    */
  protected def checkSource(source: String): String = {
    if (conf.config.sourceRequired) {
      val sourceName = conf.config.sourceKey
      source match {
        case key if key == missing => missing
        case key if key == sourceName => valid
        case _ => invalid
      }
    } else {
      notRequired
    }
  }
}
