package uk.gov.ons.addressIndex.server.modules

import javax.inject.{Inject, Singleton}

import addressIndex.model.dao.ElasticClientProvider
import com.google.inject.ImplementedBy
import com.sksamuel.elastic4s.ElasticDsl._
import com.sksamuel.elastic4s._
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse
import org.elasticsearch.action.admin.indices.delete.DeleteIndexResponse
import play.api.Logger
import uk.gov.ons.addressIndex.model.db.ElasticIndexSugar
import uk.gov.ons.addressIndex.model.db.index.{PostcodeAddressFileAddress, PostcodeIndex}

import scala.concurrent.{ExecutionContext, Future}

@ImplementedBy(classOf[AddressIndexRepository])
trait ElasticsearchRepository extends ElasticIndexSugar {
  /**
    * An ElasticClient.
    */
  def client() : ElasticClient

  /**
    * Create the repository.
    *
    * @return
    */
  def createAll() : Future[Seq[_]]

  /**
    * Delete the repository.
    *
    * @return
    */
  def deleteAll() : Future[Seq[_]]

  /**
    * Query the addres index by UPRN.
    *
    * @param uprn
    * @return
    */
  def queryUprn(uprn : String) : Future[Seq[PostcodeAddressFileAddress]]

  /**
    * Query the address index for addresses.
    * Currently the query must be for building number and postcode.
    *
    * @param buildingNumber
    * @param postcode
    * @return
    */
  def queryAddress(buildingNumber : Int, postcode : String) : Future[Seq[PostcodeAddressFileAddress]]
}

@Singleton
class AddressIndexRepository @Inject()(conf : AddressIndexConfigModule, elasticClientProvider: ElasticClientProvider)(implicit ec: ExecutionContext) extends ElasticsearchRepository {

  val logger = Logger("address-index:ElasticsearchRepositoryModule")
  val esConf = conf.config.elasticSearch
  val pafIndex = esConf.indexes.pafIndex
  val client = elasticClientProvider.client

  def createAll() : Future[Seq[CreateIndexResponse]] = {
    createIndex(
      PostcodeAddressFileAddress,
      PostcodeIndex
    )(client)
  }

  def deleteAll() : Future[Seq[DeleteIndexResponse]] = {
    deleteIndex(
      PostcodeAddressFileAddress,
      PostcodeIndex
    )(client)
  }

  def queryUprn(uprn: String): Future[Seq[PostcodeAddressFileAddress]] = client.execute{
    search in pafIndex query { termQuery("uprn", uprn) }
  }.map(_.as[PostcodeAddressFileAddress])

  def queryAddress(buildingNumber : Int, postcode : String) : Future[Seq[PostcodeAddressFileAddress]] = client.execute {
    search in pafIndex query {
      bool(
        must(
          matchQuery(
            field = "buildingNumber",
            value = buildingNumber
          ),
          matchQuery(
            field = "postcode",
            value = postcode
          )
        )
      )
    }
  }.map(_.as[PostcodeAddressFileAddress])
}