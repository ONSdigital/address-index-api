package uk.gov.ons.addressIndex.server.modules

import javax.inject.{Inject, Singleton}

import uk.gov.ons.addressIndex.server.model.dao.ElasticClientProvider
import com.google.inject.ImplementedBy
import com.sksamuel.elastic4s.ElasticDsl._
import com.sksamuel.elastic4s._
import play.api.Logger
import uk.gov.ons.addressIndex.model.db.index.{PostcodeAddressFileAddress, PostcodeAddressFileAddresses}
import uk.gov.ons.addressIndex.server.model.response.AddressTokens

import scala.concurrent.{ExecutionContext, Future}

@ImplementedBy(classOf[AddressIndexRepository])
trait ElasticsearchRepository {
  /**
    * An ElasticClient.
    */
  def client() : ElasticClient

  /**
    * Query the addres index by UPRN.
    *
    * @param uprn
    * @return
    */
  def queryUprn(uprn : String) : Future[Option[PostcodeAddressFileAddress]]

  /**
    * Query the address index for addresses.
    * Currently the query must be for building number and postcode.
    *
    * @param tokens address tokens
    * @return Future with found addresses and the maximum score
    */
  def queryAddress(tokens: AddressTokens) : Future[PostcodeAddressFileAddresses]
}

@Singleton
class AddressIndexRepository @Inject()(conf : AddressIndexConfigModule, elasticClientProvider: ElasticClientProvider)(implicit ec: ExecutionContext) extends ElasticsearchRepository {

  private val logger = Logger("address-index:ElasticsearchRepositoryModule")
  private val esConf = conf.config.elasticSearch
  private val pafIndex = esConf.indexes.pafIndex
  val client: ElasticClient = elasticClientProvider.client

  def queryUprn(uprn: String): Future[Option[PostcodeAddressFileAddress]] = client.execute{
    search in pafIndex query { termQuery("uprn", uprn) }
  }.map(_.as[PostcodeAddressFileAddress].headOption)

  def queryAddress(tokens: AddressTokens) : Future[PostcodeAddressFileAddresses] = client.execute {
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
}