package uk.gov.ons.addressIndex.server.modules.validation

import play.api.mvc.{RequestHeader, Result}
import uk.gov.ons.addressIndex.model.server.response._
import uk.gov.ons.addressIndex.server.modules.response.Response
import uk.gov.ons.addressIndex.server.modules.{ConfigModule, VersionModule}
import uk.gov.ons.addressIndex.server.utils.impl.AddressLogMessage

import scala.concurrent.Future

abstract class Validation()(implicit conf: ConfigModule, versionProvider: VersionModule)
  extends Object with Response {

  // lazy to avoid application crash at startup if ES is down
  override lazy val dataVersion: String = versionProvider.dataVersion
  override lazy val apiVersion: String = versionProvider.apiVersion

  val missing: String = "missing"
  val invalid: String = "invalid"
  val valid: String = "valid"
  val notRequired: String = "not required"

  def trace(message: AddressLogMessage): Unit
  def log(message: AddressLogMessage): Unit
  def debug(message: AddressLogMessage): Unit

  def validateKeyStatus(implicit request: RequestHeader): Option[Future[Result]] = {
    val apiKey = request.headers.get("authorization").getOrElse(missing)

    checkAPIkey(apiKey) match {
      case `missing` =>
        log(AddressLogMessage(badRequestMessage = ApiKeyMissingError.message))
        Some(futureJsonUnauthorized(KeyMissing))
      case `invalid` =>
        log(AddressLogMessage(badRequestMessage = ApiKeyInvalidError.message))
        Some(futureJsonUnauthorized(KeyInvalid))
      case _ =>
        None
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

  def validateSource(implicit request: RequestHeader): Option[Future[Result]] = {

    val source = request.headers.get("Source").getOrElse(missing)

    checkSource(source) match {
      case `missing` =>
        log(AddressLogMessage(badRequestMessage = SourceMissingError.message))
        Some(futureJsonUnauthorized(SourceMissing))
      case `invalid` =>
        log(AddressLogMessage(badRequestMessage = SourceInvalidError.message))
        Some(futureJsonUnauthorized(SourceInvalid))
      case _ =>
        None
    }
  }

  /**
    * Method to check source of query
    *
    * @param source the source to check
    * @return not required, valid, invalid or missing
    */
  protected def checkSource(source: String): String = {
    val sourceRequired = conf.config.sourceRequired
    if (sourceRequired) {
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
