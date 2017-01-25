package uk.gov.ons.addressIndex.server.controllers

import uk.gov.ons.addressIndex.server.modules.{AddressIndexCannedResponse, AddressParserModule, ElasticSearchRepository}
import com.sksamuel.elastic4s.{ElasticClient, RichSearchResponse, SearchDefinition}
import org.elasticsearch.common.settings.Settings
import play.api.mvc.Result
import org.scalatestplus.play._
import play.api.Logger
import play.api.test.FakeRequest
import play.test.WithApplication
import uk.gov.ons.addressIndex.crfscala.CrfScala.CrfTokenResult
import uk.gov.ons.addressIndex.model.AddressScheme
import uk.gov.ons.addressIndex.model.config.{AddressIndexConfig, ElasticSearchConfig, IndexesConfig, ShieldConfig}
import uk.gov.ons.addressIndex.server.modules.AddressIndexConfigModule
import uk.gov.ons.addressIndex.server.modules.Model.Pagination
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class AddressControllerSpec extends PlaySpec with AddressIndexCannedResponse  {

  "Address controller" should {
    new WithApplication {
      val maxLimit = 11
      val maxOffset = 2
      val controller = new AddressController(
        parser = new AddressParserModule,
        conf = new AddressIndexConfigModule(
          optOverride = Some(
            AddressIndexConfig(
              elasticSearch = ElasticSearchConfig(
                local = false,
                cluster = "",
                uri = "",
                indexes = IndexesConfig(
                  hybridIndex = ""
                ),
                shield = ShieldConfig(
                  ssl = true,
                  user = "",
                  password = ""
                ),
                defaultLimit = 10,
                defaultOffset = 1,
                maximumLimit = maxLimit,
                maximumOffset = maxOffset
              )
            )
          )
        ),
        esRepo = new ElasticSearchRepository {
          override def logger: Logger = ???
          override def queryUprnSearchDefinition(uprn: String)
            (implicit fmt: Option[AddressScheme]): SearchDefinition = ???
          override def queryAddressSearchDefinition(tokens: Seq[CrfTokenResult])
            (implicit p: Pagination, fmt: Option[AddressScheme]): SearchDefinition = ???
          override def client(): ElasticClient = ElasticClient.local(Settings.builder().build())
          override def queryAddress(tokens: Seq[CrfTokenResult])
           (implicit p: Pagination, fmt: Option[AddressScheme]): Future[RichSearchResponse] = {
            Future.successful(null)
          }
          override def queryUprn(uprn: String)
            (implicit fmt: Option[AddressScheme]): Future[RichSearchResponse] = {
            Future.successful(null)
          }
        }
      )

      def fakeRequest(
        input: String,
        format: Option[String] = None,
        limit: Option[String] = None,
        offset: Option[String] = None
       ): Future[Result] = {
        controller.addressQuery(
          input = input,
          format = format,
          limit = limit,
          offset = offset
        )(FakeRequest())
      }

      def fakeRequestDummyInput(
        format: Option[String] = None,
        limit: Option[String] = None,
        offset: Option[String] = None
      ): Future[Result] = {
        fakeRequest(
          input = "input",
          format = format,
          limit = limit,
          offset = offset
        )
      }

      "400 error on non-numeric offset" in {
        fakeRequestDummyInput(
          offset = Some("non-numeric")
        ) map(_.header.status mustBe 400)
      }

      "400 error on non-numeric limit" in {
        fakeRequestDummyInput(
          limit = Some("non-numeric")
        ) map(_.header.status mustBe 400)
      }

      "400 error on negative offset" in {
        fakeRequestDummyInput(
          limit = Some("non-numeric")
        ) map(_.header.status mustBe 400)
      }

      "400 error on negative limit" in {
        fakeRequestDummyInput(
          limit = Some("-1")
        ) map(_.header.status mustBe 400)
      }

      "400 error on zero limit" in {
        fakeRequestDummyInput(
          limit = Some("0")
        ) map(_.header.status mustBe 400)
      }

      "400 error on offset greater than the maximum limit configured" in {
        fakeRequestDummyInput(
          limit = Some((maxLimit + 1).toString)
        ) map(_.header.status mustBe 400)
      }

      "400 error on offset greater than the maximum offset configured" in {
        fakeRequestDummyInput(
          offset = Some((maxOffset + 1).toString)
        ) map(_.header.status mustBe 400)
      }

      "400 error if query is empty" in {
        fakeRequest(
          input = ""
        ) map(_.header.status mustBe 400)
      }

      "404 error if address was not found (by uprn)" in {
        controller.uprnQuery(
          uprn = "2330"
        )(FakeRequest()) map(_.header.status mustBe 404)
      }

      "400 error if address format is not supported (by uprn)" in {
        controller.uprnQuery(
          uprn = "arbitrary",
          format = Some("not supported")
        )(FakeRequest()) map(_.header.status mustBe 400)
      }
    }
    ()
  }
}
