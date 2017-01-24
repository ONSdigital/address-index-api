package uk.gov.ons.addressIndex.server.modules

import com.sksamuel.elastic4s.ElasticClient
import com.sksamuel.elastic4s.testkit.ElasticSugar
import org.scalatest.{FlatSpec, Matchers}
import uk.gov.ons.addressIndex.crfscala.CrfScala.CrfTokenResult
import uk.gov.ons.addressIndex.model.{AddressScheme, BritishStandard7666, PostcodeAddressFile}
import uk.gov.ons.addressIndex.server.model.dao.ElasticClientProvider
import uk.gov.ons.addressIndex.server.modules.Model.Pagination
import scala.concurrent.ExecutionContext.Implicits.global

class ElasticSearchRepositorySpec extends FlatSpec with ElasticSugar with Matchers {

  object Resources {

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

    object Formats {
      val none: Option[AddressScheme] = None
      val paf: Option[AddressScheme] = Some(PostcodeAddressFile("paf"))
      val nag: Option[AddressScheme] = Some(BritishStandard7666("bs"))
    }

  }

  import Resources._


  it should "produce the correct search definition for a uprn, no format" in {
    val input = "109102"
    implicit val format: Option[AddressScheme] = Formats.none
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

  it should "produce the correct search definition for a uprn, `PostcodeAddressFile` format" in {
    val input = "109102"
    implicit val format: Option[AddressScheme] = Formats.paf
    val actual = (repo queryUprnSearchDefinition input).toString.normalise
    val expected = """
                     |{
                     |    "query" : {
                     |        "bool" : {
                     |            "must" : {
                     |                "match" : {
                     |                    "uprn" : {
                     |                        "query" : "109102",
                     |                        "type" : "boolean"
                     |                     }
                     |                 }
                     |            }
                     |        }
                     |    },
                     |    "_source" : {
                     |        "includes" : [],
                     |        "excludes" : ["lpi"]
                     |    }
                     | }
                     |""".stripMargin.normalise

    actual shouldBe expected
  }
  it should "produce the correct search definition for a uprn, `BritishStandard7666` format" in {
    val input = "109102"
    implicit val format: Option[AddressScheme] = Formats.nag
    val actual = (repo queryUprnSearchDefinition input).toString.normalise
    val expected = """
                     |{
                     |    "query" : {
                     |        "bool":{
                     |            "must":{
                     |                "match":{
                     |                    "uprn":{
                     |                        "query":"109102",
                     |                        "type":"boolean"
                     |                     }
                     |                 }
                     |            }
                     |        }
                     |    },
                     |    "_source" : {
                     |        "includes":[],
                     |        "excludes":["paf"]
                     |    }
                     | }
                     |""".stripMargin.normalise

    actual shouldBe expected
  }

  it should "produce the correct search definition for an address, no format" in {
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
    implicit val format: Option[AddressScheme] = Formats.none
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

  it should "produce the correct search definition for an address, `PostcodeAddressFile` format" in {
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
    implicit val format: Option[AddressScheme] = Formats.paf
    val actual = (repo queryAddressSearchDefinition tokens).toString.normalise
    val expected = """
                     |{
                     |   "from" : 0,
                     |   "size" : 10,
                     |   "query" : {
                     |       "query_string" : {
                     |           "query" : "wd244re"
                     |       }
                     |   },
                     |   "_source" : {
                     |       "includes":[],
                     |       "excludes":["lpi"]
                     |   }
                     |}
                   """.stripMargin.normalise

    actual shouldBe expected
  }

  it should "produce the correct search definition for an address, `BritishStandard7666` format" in {
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
    implicit val format: Option[AddressScheme] = Formats.nag
    val actual = (repo queryAddressSearchDefinition tokens).toString.normalise
    val expected = """
                     |{
                     |   "from" : 0,
                     |   "size" : 10,
                     |   "query" : {
                     |       "query_string" : {
                     |           "query" : "wd244re"
                     |       }
                     |   },
                     |   "_source" : {
                     |       "includes":[],
                     |       "excludes":["paf"]
                     |   }
                     |}
                   """.stripMargin.normalise

    actual shouldBe expected
  }
}
