package uk.gov.ons.addressIndex.server.modules

import javax.inject.{Inject, Singleton}
import uk.gov.ons.addressIndex.server.model.dao.ElasticClientProvider
import com.google.inject.ImplementedBy
import com.sksamuel.elastic4s.ElasticDsl._
import com.sksamuel.elastic4s._
import uk.gov.ons.addressIndex.model.db.index.{NationalAddressGazetteerAddress, NationalAddressGazetteerAddresses, PostcodeAddressFileAddress, PostcodeAddressFileAddresses}
import uk.gov.ons.addressIndex.model.server.response.AddressTokens
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
  def queryPafAddresses(tokens: AddressTokens) : Future[PostcodeAddressFileAddresses]

  /**
    * Query the address index for NAG addresses.
    * Currently the query must be for building number and postcode.
    *
    * @param tokens address tokens
    * @return Future with found PAF addresses and the maximum score
    */
  def queryNagAddresses(tokens: AddressTokens) : Future[NationalAddressGazetteerAddresses]
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

  def queryPafUprn(uprn: String): Future[Option[PostcodeAddressFileAddress]] = client.execute {
    search in pafIndex query { termQuery("uprn", uprn) }
  }.map(_.as[PostcodeAddressFileAddress].headOption)

  def queryNagUprn(uprn: String): Future[Option[NationalAddressGazetteerAddress]] = client.execute {
    search in nagIndex query { termQuery("uprn", uprn) }
  }.map(_.as[NationalAddressGazetteerAddress].headOption)

  def queryPafAddresses(tokens: AddressTokens) : Future[PostcodeAddressFileAddresses] = client.execute {
    search in pafIndex query {
      bool(
        must(
          matchQuery(
            field = "buildingNumber",
            value = tokens.buildingNumber
          ),
          matchQuery(
            field = "postcode",
            value = tokens.postcode
          )
        )
      )
    }
  }.map(response => PostcodeAddressFileAddresses(response.as[PostcodeAddressFileAddress], response.maxScore))

  def queryNagAddresses(tokens: AddressTokens) : Future[NationalAddressGazetteerAddresses] = client.execute {
    search in nagIndex query {
      bool(
        must(
          matchQuery(
            field = "paoStartNumber",
            value = tokens.buildingNumber
          ),
          matchQuery(
            field = "postcodeLocator",
            value = tokens.postcode
          )
        )
      )
    }
  }.map(response => NationalAddressGazetteerAddresses(response.as[NationalAddressGazetteerAddress], response.maxScore))
}