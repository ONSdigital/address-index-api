package uk.gov.ons.addressIndex.server.modules

import javax.inject.{Inject, Singleton}

import uk.gov.ons.addressIndex.server.model.dao.ElasticClientProvider
import com.google.inject.ImplementedBy
import com.sksamuel.elastic4s.ElasticDsl._
import com.sksamuel.elastic4s._
import play.api.Logger
import play.api.libs.json.Json
import uk.gov.ons.addressIndex.crfscala.CrfScala.{CrfTokenResult, Input}
import uk.gov.ons.addressIndex.model.db.index._
import uk.gov.ons.addressIndex.parsers.Tokens
import uk.gov.ons.addressIndex.parsers.Tokens.Token
import uk.gov.ons.addressIndex.server.modules.Model._

import scala.concurrent.{ExecutionContext, Future}

object Model {
  case class Pagination(offset: Int, limit: Int)
  implicit class AutoPaginate(searchDefinition: SearchDefinition) {
    def paginate(implicit p: Pagination): SearchDefinition = searchDefinition start p.offset limit p.limit
  }
}

@ImplementedBy(classOf[AddressIndexRepository])
trait ElasticsearchRepository {
  /**
    * An ElasticClient.
    */
  def client(): ElasticClient

  /**
    * Query the PAF addres index by UPRN.
    *
    * @param uprn the identificator of the address
    * @return Fucture containing a PAF address or `None` if not in the index
    */
  def queryPafUprn(uprn: String): Future[Option[PostcodeAddressFileAddress]]

  /**
    * Query the NAG addres index by UPRN.
    *
    * @param uprn the identificator of the address
    * @return Fucture containing a NAG address or `None` if not in the index
    */
  def queryNagUprn(uprn: String): Future[Option[NationalAddressGazetteerAddress]]

  /**
    * Query the address index for PAF addresses.
    * Currently the query must be for building number and postcode.
    *
    * @param tokens address tokens
    * @return Future with found PAF addresses and the maximum score
    */
  def queryPafAddresses(tokens: Seq[CrfTokenResult])(implicit p: Pagination): Future[PostcodeAddressFileAddresses]

  /**
    * Query the address index for NAG addresses.
    * Currently the query must be for building number and postcode.
    *
    * @param tokens address tokens
    * @return Future with found PAF addresses and the maximum score
    */
  def queryNagAddresses(tokens: Seq[CrfTokenResult])(implicit p: Pagination): Future[NationalAddressGazetteerAddresses]

  def queryHybrid(tokens: Seq[CrfTokenResult])(implicit p: Pagination): Future[RichSearchResponse]
}

@Singleton
class AddressIndexRepository @Inject()(
  conf: AddressIndexConfigModule,
  elasticClientProvider: ElasticClientProvider
)(implicit ec: ExecutionContext) extends ElasticsearchRepository {

  private val esConf = conf.config.elasticSearch
  private val pafIndex = esConf.indexes.pafIndex
  private val nagIndex = esConf.indexes.nagIndex
  private val hybridIndex = esConf.indexes.hybridIndex
  private val logger = Logger("AddressIndexRepository")
  val client: ElasticClient = elasticClientProvider.client

  def queryPafUprn(uprn: String): Future[Option[PostcodeAddressFileAddress]] = {
    logExecute("Query Paf UPRN") {
      search in pafIndex query { termQuery("uprn", uprn) }
    } map(_.as[PostcodeAddressFileAddress].headOption)
  }

  def queryNagUprn(uprn: String): Future[Option[NationalAddressGazetteerAddress]] = {
    logExecute("Query Nag UPRN") {
      search in nagIndex query { termQuery("uprn", uprn)}
    } map(_.as[NationalAddressGazetteerAddress].headOption)
  }

  private def getTokenValue(token: Token, tokens: Seq[CrfTokenResult]): String = {
    tokens.filter(_.label == token).map(_.value).mkString(" ")
  }

  private def collapseTokens(tokens: Seq[CrfTokenResult]): Map[Token, Input] = {
    tokens.groupBy(_.label).map(e => e._1 -> e._2.map(_.value).mkString(" "))
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

  def queryPafAddresses(tokens: Seq[CrfTokenResult])(implicit p: Pagination): Future[PostcodeAddressFileAddresses] = {
    logExecute("Query Paf Addresses") {
      search.in(pafIndex).paginate query {
        bool(
          must(
            tokensToMatchQueries(
              tokens = tokens,
              tokenFieldMap = Map(
                Tokens.buildingNumber -> PostcodeAddressFileAddress.Fields.buildingNumber,
                //Tokens.Locality -> "",//PostcodeAddressFileAddress.Fields.Dependentlocality OR q welsh == //DoubleDependentlocality,
                Tokens.organisationName -> PostcodeAddressFileAddress.Fields.organizationName,
                Tokens.departmentName -> PostcodeAddressFileAddress.Fields.departmentName,
                Tokens.subBuildingName -> PostcodeAddressFileAddress.Fields.subBuildingName,
                Tokens.buildingName -> PostcodeAddressFileAddress.Fields.buildingName,
                Tokens.streetName -> PostcodeAddressFileAddress.Fields.thoroughfare,
                Tokens.townName -> PostcodeAddressFileAddress.Fields.postTown,
                Tokens.postcode -> PostcodeAddressFileAddress.Fields.postcode
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


  def queryNagAddresses(tokens: Seq[CrfTokenResult])(implicit p: Pagination): Future[NationalAddressGazetteerAddresses] = {
    logExecute("Query Nag Addresses") {
      search.in(nagIndex).paginate query {
        bool(
          must(
            tokensToMatchQueries(
              tokens = tokens,
              tokenFieldMap = Map(
                Tokens.buildingNumber -> NationalAddressGazetteerAddress.Fields.paoStartNumber,
                Tokens.postcode -> NationalAddressGazetteerAddress.Fields.postcodeLocator,
                Tokens.locality -> NationalAddressGazetteerAddress.Fields.locality,
                Tokens.organisationName -> NationalAddressGazetteerAddress.Fields.organisation,
                Tokens.departmentName -> NationalAddressGazetteerAddress.Fields.legalName,
                Tokens.subBuildingName -> NationalAddressGazetteerAddress.Fields.saoText,
                Tokens.buildingName -> NationalAddressGazetteerAddress.Fields.paoText,
                //Tokens.BuildingNumber -> NationalAddressGazetteerAddress.Fields., StartPrefix EndPrefix,
                Tokens.streetName -> NationalAddressGazetteerAddress.Fields.streetDescriptor,
                Tokens.townName -> NationalAddressGazetteerAddress.Fields.townName,
                Tokens.postcode -> NationalAddressGazetteerAddress.Fields.postcodeLocator
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

  override def queryHybrid(tokens: Seq[CrfTokenResult])(implicit p: Pagination): Future[RichSearchResponse] = {
    logExecute("Query Hybrid Addresses") {
      search.in(hybridIndex).paginate query {
        query(tokens.map(_.value).mkString(" "))
      }
    }
  }

  private def logExecute[T, R, Q](info: String)(t: T)(implicit executable: Executable[T, R, Q]): Future[Q] = {
    logger info s"$info: ${t.toString}"
    client execute t
  }
}