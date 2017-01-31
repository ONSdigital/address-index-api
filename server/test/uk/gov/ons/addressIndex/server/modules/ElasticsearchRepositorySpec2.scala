package uk.gov.ons.addressIndex.server.modules

import com.sksamuel.elastic4s.ElasticClient
import com.sksamuel.elastic4s.testkit.ElasticSugar
import org.scalatest.{FlatSpec, Matchers}
import uk.gov.ons.addressIndex.crfscala.CrfScala.CrfTokenResult
import uk.gov.ons.addressIndex.server.model.dao.ElasticClientProvider
import scala.concurrent.ExecutionContext.Implicits.global

class ElasticSearchRepositorySpec2 extends FlatSpec with ElasticSugar with Matchers {

  val testClient = client

  val repo = new AddressIndexRepository(
    conf = new AddressIndexConfigModule,
    elasticClientProvider = new ElasticClientProvider {
      override def client: ElasticClient = testClient
    }
  )

  implicit class StringStripper(str: String) {
    def normalise(): String = {
      str.replace(" ", "").replace("\n", "")
    }
  }


  it should "produce the correct search definition for a uprn" in {
    val input = "109102"
    val actual = (repo generateQueryUprnRequest input).toString.normalise
    println(actual)
    val expected = """
                     |{"query":{"term":{"uprn":"109102"}}}
                   """.stripMargin.normalise

    actual shouldBe expected
  }


  it should "produce the correct search definition for an address" in {
    val tokens: Seq[CrfTokenResult] = Seq(
      CrfTokenResult(
        value = "wd244re",
        label = "Postcode"
      )
    )
    val actual = (repo generateQueryAddressRequest tokens).toString.normalise
    val expected = """
                     |{"query":{"bool":{"should":[{"match":{"lpi.postcodeLocator":{"query":"wd244re","type":"boolean"}}},{"match":{"paf.postcode":{"query":"wd244re","type":"boolean"}}},{"match":{"_all":{"query":"wd244re","type":"boolean","boost":0.5}}}],"minimum_should_match":"55%"}}}
                   """.stripMargin.normalise

    actual shouldBe expected
  }
}
