package uk.gov.ons.addressIndex.server.modules

import com.sksamuel.elastic4s.ElasticClient
import com.sksamuel.elastic4s.testkit.ElasticSugar
import org.scalatest.{FlatSpec, Matchers}
import uk.gov.ons.addressIndex.crfscala.CrfScala.CrfTokenResult
import uk.gov.ons.addressIndex.model.AddressScheme
import uk.gov.ons.addressIndex.server.model.dao.ElasticClientProvider
import uk.gov.ons.addressIndex.server.modules.Model.Pagination
import scala.concurrent.ExecutionContext.Implicits.global

class ElasticSearchRepositorySpec extends FlatSpec with ElasticSugar with Matchers {

  val testClient = client

  private val repo = new AddressIndexRepository(
    conf = new AddressIndexConfigModule,
    elasticClientProvider = new ElasticClientProvider {
      override def client: ElasticClient = testClient
    }
  )

  implicit val format: Option[AddressScheme] = None

  implicit class StringStripper(str: String) {
    def normalise(): String = {
      str.replace(" ", "").replace("\n", "")
    }
  }

  it should "produce the correct search definition for a uprn" in {
    val input = "109102"
    val actual = (repo queryUprnSearchDefinition input).toString.normalise
    val expected = """
                     |{
                     |    "query" : {
                     |        "bool" : {
                     |             "must" : {
                     |                 "match" : {
                     |                     "uprn" : {
                     |                         "query" : "109102",
                     |                         "type" : "boolean"
                     |                     }
                     |                 }
                     |             }
                     |         }
                     |    }
                     |}
                     |""".stripMargin.normalise

    actual shouldBe expected
  }

  it should "produce the correct search definition for an address" in {
    val tokens: Seq[CrfTokenResult] = Seq(
      CrfTokenResult(
        value = "wd244re",
        label = "Postcode"
      )
    )
    implicit val pagination = Pagination(
      offset = 0,
      limit = 10
    )
    val actual = (repo queryAddressSearchDefinition tokens).toString.normalise
    val expected = """
                     |{
                     |   "from" : 0,
                     |   "size" : 10,
                     |   "query" : {
                     |       "query_string" : {
                     |           "query" : "wd244re"
                     |       }
                     |   }
                     |}
                   """.stripMargin.normalise

    actual shouldBe expected
  }
}
