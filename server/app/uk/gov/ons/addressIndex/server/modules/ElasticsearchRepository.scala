package uk.gov.ons.addressIndex.server.modules

import com.google.inject.ImplementedBy
import com.sksamuel.elastic4s.searches.SearchDefinition
import uk.gov.ons.addressIndex.model.config.QueryParamsConfig
import uk.gov.ons.addressIndex.model.db.BulkAddressRequestData
import uk.gov.ons.addressIndex.model.db.index.{HybridAddress, HybridAddresses, HybridAddressesPartial}
import uk.gov.ons.addressIndex.model.server.response.bulk.AddressBulkResponseAddress

import scala.concurrent.Future

@ImplementedBy(classOf[AddressIndexRepository])
trait ElasticsearchRepository {

  /**
    * Queries if ES is up
    */
  def queryHealth(): Future[String]

  /**
    * Query the address index by UPRN.
    *
    * @param uprn the identificator of the address
    * @return Future containing a address or `None` if not in the index
    */
  def queryUprn(uprn: String, startDate: String, endDate:String, historical: Boolean = true): Future[Option[HybridAddress]]

  /**
    * Query the address index by partial address.
    *
    * @param input the identificator of the address
    * @return Future containing a address or `None` if not in the index
    */
  def queryPartialAddress(input: String, start: Int, limit: Int, filters: String, startDate: String = "", endDate: String = "", queryParamsConfig: Option[QueryParamsConfig] = None, historical: Boolean = true, verbose: Boolean = true): Future[HybridAddressesPartial]

  /**
    * Query the address index by postcode.
    *
    * @param postcode the identificator of the address
    * @return Future containing a address or `None` if not in the index
    */
  def queryPostcode(postcode: String, start: Int, limit: Int, filters: String, startDate:String = "", endDate:String = "", queryParamsConfig: Option[QueryParamsConfig] = None, historical: Boolean = true): Future[HybridAddresses]

  /**
    * Query the address index for a random address.
    *
    * @return Future containing an address or `None` if not in the index
    */
  def queryRandom(filters: String, limit: Int, queryParamsConfig: Option[QueryParamsConfig] = None, historical: Boolean = true, verbose: Boolean = false): Future[HybridAddressesPartial]

  /**
    * Query the address index for addresses.
    *
    * @param start  the offset for the query
    * @param limit  maximum number of returned results
    * @param tokens address tokens
    * @return Future with found addresses and the maximum score
    */
  def queryAddresses(tokens: Map[String, String], start: Int, limit: Int, filters: String, range: String, lat: String, lon: String, startDate:String = "", endDate:String = "", queryParamsConfig: Option[QueryParamsConfig] = None, historical: Boolean = true, isBulk: Boolean = true): Future[HybridAddresses]

  /**
    * Generates request to get address from ES by UPRN
    * Public so that it could be accessible to controllers for User's debug purposes
    *
    * @param tokens tokens for the ES query
    * @return Search definition containing query to the ES
    */
  def generateQueryAddressRequest(tokens: Map[String, String], filters: String, range: String, lat: String, lon: String, startDate: String, endDate: String, queryParamsConfig: Option[QueryParamsConfig] = None, historical: Boolean = true, isBulk: Boolean = true): SearchDefinition

  /**
    * Query ES using MultiSearch endpoint
    *
    * @param requestsData data that will be used in the multi search request
    * @param limit        how many addresses to take per a request
    * @return a stream of `Either`, `Right` will contain resulting bulk address,
    *         `Left` will contain request data that is to be re-send
    */
  def queryBulk(requestsData: Stream[BulkAddressRequestData], limit: Int, startDate:String = "", endDate:String = "", queryParamsConfig: Option[QueryParamsConfig] = None, historical: Boolean = true, matchThreshold: Float, includeFullAddress: Boolean = false): Future[Stream[Either[BulkAddressRequestData, Seq[AddressBulkResponseAddress]]]]
}
