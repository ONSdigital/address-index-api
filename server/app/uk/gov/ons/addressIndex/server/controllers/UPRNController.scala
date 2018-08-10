package uk.gov.ons.addressIndex.server.controllers

import javax.inject.{Inject, Singleton}
import play.api.Logger
import play.api.libs.json.Json
import play.api.mvc._
import uk.gov.ons.addressIndex.model.db.index.HybridAddress
import uk.gov.ons.addressIndex.model.server.response._
import uk.gov.ons.addressIndex.server.modules.response.UPRNResponse
import uk.gov.ons.addressIndex.server.modules.validation.UPRNValidation
import uk.gov.ons.addressIndex.server.modules.{ConfigModule, ElasticsearchRepository, ParserModule, VersionModule}
import uk.gov.ons.addressIndex.server.utils.impl.{AddressLogMessage, AddressLogging}
import uk.gov.ons.addressIndex.server.utils.{APILogging, Overload, ProtectorStatus}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try
import scala.util.control.NonFatal

@Singleton
class UPRNController @Inject()(
  val controllerComponents: ControllerComponents,
  esRepo: ElasticsearchRepository,
  parser: ParserModule,
  conf: ConfigModule,
  versionProvider: VersionModule,
  overloadProtection: Overload,
  uprnValidation: UPRNValidation
)(implicit ec: ExecutionContext)
  extends PlayHelperController(versionProvider) with UPRNResponse with APILogging[AddressLogMessage] {

  lazy val logger = Logger("address-index-server:PostcodeController")

  override def trace(message: AddressLogMessage): Unit = AddressLogging trace message
  override def log(message: AddressLogMessage): Unit = AddressLogging trace message
  override def debug(message: AddressLogMessage): Unit = AddressLogging debug message

  /**
    * UPRN query API
    *
    * @param uprn uprn of the address to be fetched
    * @return
    */
  def uprnQuery(uprn: String, historical: Option[String] = None): Action[AnyContent] = Action async { implicit req =>

    val hist = historical match {
      case Some(x) => Try(x.toBoolean).getOrElse(true)
      case None => true
    }

    val startingTime = System.currentTimeMillis()

    def writeLog(badRequestErrorMessage: String = "", notFound: Boolean = false, formattedOutput: String = "", numOfResults: String = "", score: String = ""): Unit = {
      val responseTime = System.currentTimeMillis() - startingTime
      val networkid = req.headers.get("authorization").getOrElse("Anon").split("_")(0)

      log(AddressLogMessage(ip = req.remoteAddress, url = req.uri, responseTimeMillis = responseTime.toString,
        uprn = uprn, isNotFound = notFound, formattedOutput = formattedOutput,
        numOfResults = numOfResults, score = score, networkid = networkid, historical = hist
      ))
    }

    val result: Option[Future[Result]] =
      uprnValidation.validateUprn(uprn) orElse uprnValidation.validateSource orElse uprnValidation.validateKeyStatus orElse None

    result match {

      case Some(res) =>
        res // a validation error

      case _ =>

        val request: Future[Option[HybridAddress]] = overloadProtection.breaker.withCircuitBreaker(
          esRepo.queryUprn(uprn, hist)
        )

        request.map {
          case Some(hybridAddress) =>

            val address = AddressResponseAddress.fromHybridAddress(hybridAddress)

            writeLog(
              formattedOutput = address.formattedAddressNag, numOfResults = "1",
              score = hybridAddress.score.toString
            )

            jsonOk(
              AddressByUprnResponseContainer(
                apiVersion = apiVersion,
                dataVersion = dataVersion,
                response = AddressByUprnResponse(
                  address = Some(address)
                ),
                status = OkAddressResponseStatus
              )
            )

          case None =>
            writeLog(notFound = true)
            jsonNotFound(NoAddressFoundUprn)

        }.recover {
          case NonFatal(exception) =>

            overloadProtection.currentStatus match {
              case ProtectorStatus.HalfOpen =>
                logger.warn(
                  s"Elasticsearch is overloaded or down (address input). Circuit breaker is Half Open: ${exception.getMessage}"
                )
                TooManyRequests(Json.toJson(FailedRequestToEsTooBusy))
              case ProtectorStatus.Open =>
                logger.warn(
                  s"Elasticsearch is overloaded or down (address input). Circuit breaker is open: ${exception.getMessage}"
                )
                TooManyRequests(Json.toJson(FailedRequestToEsTooBusy))
              case _ =>
                // Circuit Breaker is closed. Some other problem
                writeLog(badRequestErrorMessage = FailedRequestToEsError.message)
                logger.warn(
                  s"Could not handle individual request (uprn), problem with ES ${exception.getMessage}"
                )
                InternalServerError(Json.toJson(FailedRequestToEs))
            }

        }
    }
  }

}
