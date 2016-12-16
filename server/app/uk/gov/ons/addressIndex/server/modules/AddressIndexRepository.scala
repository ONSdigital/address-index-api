package uk.gov.ons.addressIndex.server.modules

import javax.inject.{Inject, Singleton}

import uk.gov.ons.addressIndex.server.model.dao.ElasticClientProvider
import com.google.inject.ImplementedBy
import com.sksamuel.elastic4s.ElasticDsl._
import com.sksamuel.elastic4s._
import uk.gov.ons.addressIndex.crfscala.CrfScala.{CrfTokenResult, Input}
import uk.gov.ons.addressIndex.model.db.index.{NationalAddressGazetteerAddress, NationalAddressGazetteerAddresses, PostcodeAddressFileAddress, PostcodeAddressFileAddresses}
import uk.gov.ons.addressIndex.parsers.Tokens
import uk.gov.ons.addressIndex.parsers.Tokens.Token

import scala.concurrent.{ExecutionContext, Future}

@ImplementedBy(classOf[AddressIndexRepository])
trait ElasticsearchRepository {
  /**
    * An ElasticClient.
    */
  def client() : ElasticClient

  /**
    * Query the PAF addres index by UPRN.
    *
    * @param uprn the identificator of the address
    * @return Fucture containing a PAF address or `None` if not in the index
    */
  def queryPafUprn(uprn : String) : Future[Option[PostcodeAddressFileAddress]]

  /**
    * Query the NAG addres index by UPRN.
    *
    * @param uprn the identificator of the address
    * @return Fucture containing a NAG address or `None` if not in the index
    */
  def queryNagUprn(uprn : String) : Future[Option[NationalAddressGazetteerAddress]]

  /**
    * Query the address index for PAF addresses.
    * Currently the query must be for building number and postcode.
    *
    * @param tokens address tokens
    * @return Future with found PAF addresses and the maximum score
    */
  def queryPafAddresses(tokens: Seq[CrfTokenResult]) : Future[PostcodeAddressFileAddresses]

  /**
    * Query the address index for NAG addresses.
    * Currently the query must be for building number and postcode.
    *
    * @param tokens address tokens
    * @return Future with found PAF addresses and the maximum score
    */
  def queryNagAddresses(tokens: Seq[CrfTokenResult]) : Future[NationalAddressGazetteerAddresses]
}

@Singleton
class AddressIndexRepository @Inject()(
  conf: AddressIndexConfigModule,
  elasticClientProvider: ElasticClientProvider
)(implicit ec: ExecutionContext) extends ElasticsearchRepository {

  private val esConf = conf.config.elasticSearch
  private val pafIndex = esConf.indexes.pafIndex
  private val nagIndex = esConf.indexes.nagIndex
  val client: ElasticClient = elasticClientProvider.client

  def queryPafUprn(uprn: String): Future[Option[PostcodeAddressFileAddress]] = {
    client execute {
      search in pafIndex query { termQuery("uprn", uprn) }
    } map(_.as[PostcodeAddressFileAddress].headOption)
  }

  def queryNagUprn(uprn: String): Future[Option[NationalAddressGazetteerAddress]] = {
    client execute {
      search in nagIndex query { termQuery("uprn", uprn) }
    } map(_.as[NationalAddressGazetteerAddress].headOption)
  }

  private def getTokenValue(token: Token, tokens: Seq[CrfTokenResult]): String = {
    tokens.filter(_.label == token).map(_.value).mkString(" ")
  }

  private def collapseTokens(tokens: Seq[CrfTokenResult]): Map[Token, Input] = {
    tokens.groupBy(_.label).map(e => e._1 -> e._2.mkString(" "))
  }

  def tokensToMatchQueries(tokens: Seq[CrfTokenResult], tokenFieldMap: Map[Token, String]): Iterable[QueryDefinition] = {
    collapseTokens(tokens) flatMap { case (tkn, input) =>
      tokenFieldMap get tkn map { pafField =>
        matchQuery(
          field = pafField,
          value = input
        )
      }
    }
  }

  def queryPafAddresses(tokens: Seq[CrfTokenResult]) : Future[PostcodeAddressFileAddresses] = {
    client execute {
      search in pafIndex query {
        bool(
          must(
            tokensToMatchQueries(
              tokens = tokens,
              tokenFieldMap = Map(
                Tokens.BuildingNumber -> "buildingNumber",
                Tokens.Postcode -> "postCode"
              )
            )
          )
        )
      }
    } map { response =>
      PostcodeAddressFileAddresses(
        addresses = response.as[PostcodeAddressFileAddress],
        maxScore = response.maxScore
      )
    }
  }

  def queryNagAddresses(tokens: Seq[CrfTokenResult]) : Future[NationalAddressGazetteerAddresses] = {
    client execute {
      search in nagIndex query {
        bool(
          must(
            tokensToMatchQueries(
              tokens = tokens,
              tokenFieldMap = Map(
                Tokens.BuildingNumber -> "paoStartNumber",
                Tokens.Postcode -> "postcodeLocator"
              )
            )
          )
        )
      }
    } map { response =>
      NationalAddressGazetteerAddresses(
        addresses = response.as[NationalAddressGazetteerAddress],
        maxScore = response.maxScore
      )
    }
  }
}