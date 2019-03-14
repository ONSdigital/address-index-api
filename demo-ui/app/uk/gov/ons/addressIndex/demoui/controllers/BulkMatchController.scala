package uk.gov.ons.addressIndex.demoui.controllers

import javax.inject.{Inject, Singleton}
import akka.stream.scaladsl.Source
import com.github.tototoshi.csv._
import play.api.Logger
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.Files.TemporaryFile
import play.api.mvc._
import uk.gov.ons.addressIndex.demoui.client.AddressIndexClientInstance
import uk.gov.ons.addressIndex.demoui.model.ui.Navigation
import uk.gov.ons.addressIndex.demoui.modules.DemoUIAddressIndexVersionModule
import uk.gov.ons.addressIndex.demoui.views.{MatchTypeHelper, ScoreHelper}
import uk.gov.ons.addressIndex.model.server.response.bulk.AddressBulkResponseContainer
import uk.gov.ons.addressIndex.model.{BulkBody, BulkQuery}

import scala.concurrent.{ExecutionContext, Future}

/**
  * Controller class for a multiple addresses to be matched
  *
  * @param messagesApi
  * @param apiClient
  * @param ec
  */
@Singleton
class BulkMatchController @Inject()(
  val controllerComponents: ControllerComponents,
  override val messagesApi: MessagesApi,
  apiClient: AddressIndexClientInstance,
  version: DemoUIAddressIndexVersionModule
 )(
  implicit
  ec: ExecutionContext,
  mat: akka.stream.Materializer
) extends BaseController with I18nSupport {

  private val multiMatchFormName = "file"
  private val logger = Logger("BulkMatchController")

  def bulkMatchPage(): Action[AnyContent] = Action.async { implicit request =>
    val refererUrl = request.uri
    request.session.get("api-key").map { apiKey =>
    Future successful Ok(
      uk.gov.ons.addressIndex.demoui.views.html.multiMatch(
        nav = Navigation.default,
        fileFormName = multiMatchFormName,
        version = version
      )
    )
    }.getOrElse {
      Future.successful(Redirect(uk.gov.ons.addressIndex.demoui.controllers.routes.ApplicationHomeController.login()).withSession("referer" -> refererUrl))
    }
  }

  /**
    * Visualises the result of the uploaded bulk request in a form of a table
    */
  def uploadFileVisualise(): Action[Either[MaxSizeExceeded, MultipartFormData[TemporaryFile]]] = upload {
    (apiResponse: AddressBulkResponseContainer, _, requestParam) =>

      // This is required by the `Ok()` class to render the actual html page
      implicit val request = requestParam

      logger info s"Response size: ${apiResponse.bulkAddresses.size}"

      Ok(
        uk.gov.ons.addressIndex.demoui.views.html.multiMatch(
          nav = Navigation.default,
          fileFormName = multiMatchFormName,
          results = Some(apiResponse),
          version = version)
      )
  }

  /**
    * Generates csv file with a result from analysing addresses in the upload.
    * As this request does not refresh the page, we need to do something to
    * tell the browser that the generation of the csv file is over and it
    * can stop showing ajax-load.gif .
    * To do this, we set a cookie to a special token that was generated on the
    * client side. When the generation is over and the reply is sent, the cookie
    * will be set and the browser will know it.
    */
  def uploadFileDownloadCsv(): Action[Either[MaxSizeExceeded, MultipartFormData[TemporaryFile]]] = upload {
    (apiResponse: AddressBulkResponseContainer, token: String, requestParam) =>

      // This is required by the `Ok()` class to render the actual html page
      implicit val request = requestParam

      logger info s"Response size: ${apiResponse.bulkAddresses.size}"

      val header = "id,inputAddress,matchedAddress,uprn,matchType,confidenceScore,documentScore,rank\n"

      val ids = apiResponse.bulkAddresses.map(_.id)
      val data = apiResponse.bulkAddresses.zipWithIndex.map { case (bulkAddress, index) =>
        Seq(
          bulkAddress.id.toString,
          "\"" + bulkAddress.inputAddress + "\"",
          "\"" + bulkAddress.matchedFormattedAddress + "\"",
          bulkAddress.uprn,
          MatchTypeHelper.matchType(bulkAddress.id, ids, bulkAddress.matchedFormattedAddress),
          bulkAddress.confidenceScore,
          bulkAddress.underlyingScore,
          ScoreHelper.getRank(index, apiResponse)
        ).mkString(",") + "\n"
      }.toList

      val csv = (header :: data).grouped(500).map(_.mkString("")).toList
      val filename = s"result_size_${apiResponse.bulkAddresses.size}.csv"

      // The important part is that the response is chunked, otherwise it may not work
      // (or at least, will be much slower)
      Ok.chunked(Source[String](csv))
        .withHeaders(("Content-Disposition", s"""attachment;filename="$filename""""))
        // without httpOnly we won't be able to read the token in js
        .withCookies(Cookie("token", token, Some(86400), httpOnly = false))
  }


  /**
    * Helper function that handles file upload (as we have 2 endpoints with this functionality)
    * @param apiResponseAction lambda function that actually decides how to transform the response
    *                          from the API into something useful, like html page with a table
    *                          or a csv file
    */
  private def upload(apiResponseAction: (AddressBulkResponseContainer, String, Request[Either[MaxSizeExceeded, MultipartFormData[TemporaryFile]]]) => Result): Action[Either[MaxSizeExceeded, MultipartFormData[TemporaryFile]]] =
    Action.async(
      parse.maxLength(10 * 1024 * 1024, parse.multipartFormData)
    ) { request =>
      request.session.get("api-key").map { apiKey =>

        val result: Option[Future[Result]] = request.body match {
          case Right(file) =>
            val token: String = file.asFormUrlEncoded.get("token").flatMap(_.headOption).getOrElse("")

            file.file(multiMatchFormName).map { file =>

              val addresses = CSVReader.open(file.ref.path.toFile).all().tail.collect { case List(id, address) if address.nonEmpty =>
                BulkQuery(id, address)
              }

              apiClient.bulk(BulkBody(addresses), apiKey).map {
                apiResponse => apiResponseAction(apiResponse, token, request)
              }

            }

          case Left(maxSizeExceeded) =>
            logger.info(s"Max size exceeded: $maxSizeExceeded")
            Some(Future.successful(EntityTooLarge))
        }

        result.getOrElse(Future.successful(InternalServerError))

      }.getOrElse {
        Future.successful(Redirect(uk.gov.ons.addressIndex.demoui.controllers.routes.ApplicationHomeController.login()))
      }
    }

}
