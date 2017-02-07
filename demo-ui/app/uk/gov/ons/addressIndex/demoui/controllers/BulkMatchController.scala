package uk.gov.ons.addressIndex.demoui.controllers

import java.util.UUID
import javax.inject.{Inject, Singleton}

import play.api.Logger
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.Files.TemporaryFile
import play.api.mvc._
import uk.gov.ons.addressIndex.demoui.client.AddressIndexClientInstance
import uk.gov.ons.addressIndex.demoui.model.ui.Navigation
import uk.gov.ons.addressIndex.demoui.modules.DemouiConfigModule
import uk.gov.ons.addressIndex.demoui.utils.ClassHierarchy
import uk.gov.ons.addressIndex.model.AddressIndexSearchRequest
import uk.gov.ons.addressIndex.model.server.response.AddressBySearchResponseContainer

import scala.io.Source
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.language.postfixOps

/**
  * Controller class for a multiple addresses to be matched
  *
  * @param messagesApi
  * @param conf
  * @param apiClient
  * @param ec
  */
@Singleton
class BulkMatchController @Inject()(
  val messagesApi: MessagesApi,
  conf: DemouiConfigModule,
  apiClient: AddressIndexClientInstance,
  classHierarchy: ClassHierarchy
 )(
  implicit
  ec: ExecutionContext,
  mat: akka.stream.Materializer
) extends Controller with I18nSupport {

  private val multiMatchFormName = "file"

  def bulkMatchPage(): Action[AnyContent] = Action.async { implicit request =>
    Future successful Ok(
      uk.gov.ons.addressIndex.demoui.views.html.multiMatch(
        nav = Navigation.default,
        fileFormName = multiMatchFormName
      )
    )
  }

  def q(source: Source): Seq[Future[AddressBySearchResponseContainer]] = {
    source.getLines.map(line =>
      apiClient.addressQuery(
        request = AddressIndexSearchRequest(
          input = line,
          limit = "10",
          offset = "0",
          id = UUID.randomUUID
        )
      )
    ).toSeq
  }

  import scala.concurrent.duration._

  def uploadFile(): Action[Either[MaxSizeExceeded, MultipartFormData[TemporaryFile]]] = Action.async(
    parse.maxLength(
      10 * 1024 * 1024, //10MB
      parse.multipartFormData
    )
  ) { implicit request =>

    Logger("uploadFile").info("invoked")

    val optRes = request.body match {
      case Right(file) => {
        file.file(multiMatchFormName) map { file =>

          val source = Source.fromFile(file.ref.file)
          val x = source.iter
//          val reqs = q(source)

          val test = source.getLines.map(line =>
            apiClient.addressQuery(
              request = AddressIndexSearchRequest(
                input = line,
                limit = "10",
                offset = "0",
                id = UUID.randomUUID
              )
            )
          )
          println("lines txfd into reqs")

          val s  = test.grouped(30).map(x => Future.sequence(x))
          println("about to do blocking")

          val y = s.map { f =>

            val z = Await.result(f, 100 seconds)
            z
          }



          pprint.pprintln(y.toSeq)

          Future.successful(Ok)
//          val seqAddress = source.mkString.split("\n").toSeq

//          Logger("TEST").info("SIZE OF ADDRESSES:: " + seqAddress.size.toString)
          //
          //         Future.sequence(reqs) map { resp =>
//            Logger("szie resp").info(resp.size.toString)
//            Ok(
//              uk.gov.ons.addressIndex.demoui.views.html.multiMatch(
//                nav = Navigation.default,
//                fileFormName = multiMatchFormName,
//                results = None
////                  Some(
////                  seqAddress.zip(resp)
////                )
//              )
//            )
//          }
        }
      }
      case Left(maxSizeExceeded) => {
        Some(Future.successful(EntityTooLarge))
      }
    }
    optRes.getOrElse(Future.successful(InternalServerError))
  }
}