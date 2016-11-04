package uk.gov.ons.address.controllers

import javax.inject.{Inject, Singleton}

import play.api.Logger
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Controller, _}
import uk.gov.ons.address.client.AddressApiClient
import uk.gov.ons.address.conf.OnsFrontendConfiguration
import uk.gov.ons.address.views

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class MultipleMatch @Inject()(
    addressApiClient: AddressApiClient,
    val messagesApi: MessagesApi,
    configuration: OnsFrontendConfiguration)(implicit exec: ExecutionContext)
    extends Controller
    with I18nSupport {
  val mimeTypes: List[String] = List("application/excel", "application/vnd.ms-excel", "application/vnd.msexcel", "text/anytext", "text/comma-separated-values", "text/csv")
  val logger = Logger("app-log")
  def showMultipleMatchPage = Action {
    logger.debug("Rendering bulk search page")
    Ok(views.html.multipleMatch(None, None, None, configuration))
  }

  def doBulkMatch = Action.async(parse.multipartFormData) { implicit request =>
    logger.info("Entering doBulkMatch Action")

    request.body
      .file("file")
      .map { file =>
        import java.io.File
        val filename = randomFileName
        val contentType: Option[String] = file.contentType
        if(mimeTypes.contains(contentType.get)){
          file.ref.moveTo(
            new File(s"${configuration.onsUploadFileLocation}/$filename").getCanonicalFile,
            true)
          val result = addressApiClient.multipleMatch(filename)
          result.map(a =>
            Ok(views.html
              .multipleMatch(Some(a), None, Some(filename), configuration)))
        } else {
          Future.successful(
            Ok(
              views.html.multipleMatch(
                None,
                Some("Currently the API is only supporting .csv format"),
                None,
                configuration)))
        }
      }
      .getOrElse {
        Future.successful(
          Ok(
            views.html.multipleMatch(
              None,
              Some("Something went wrong while uploading the file. Please contact System Administrator"),
              None,
              configuration)))
      }
  }
  def randomFileName = java.util.UUID.randomUUID.toString + ".csv"

  def downloadfile(fileName: String) = Action {
    Ok.sendFile(
        new java.io.File(s"${configuration.onsUploadFileLocation}/$fileName"),
        inline = true)
      .withHeaders(
        CACHE_CONTROL -> "max-age=3600",
        CONTENT_DISPOSITION -> "attachment; filename=BulkMatchOutput.csv",
        CONTENT_TYPE -> "application/x-download")
  }
}
