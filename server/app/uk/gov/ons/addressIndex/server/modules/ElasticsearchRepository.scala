package uk.gov.ons.addressIndex.server.modules

import com.google.inject.ImplementedBy
import com.sksamuel.elastic4s.searches.SearchDefinition
import uk.gov.ons.addressIndex.model.config.QueryParamsConfig
import uk.gov.ons.addressIndex.model.db.BulkAddressRequestData
import uk.gov.ons.addressIndex.model.db.index._
import uk.gov.ons.addressIndex.model.server.response.bulk.AddressBulkResponseAddress

import scala.concurrent.Future

@ImplementedBy(classOf[AddressIndexRepository])
trait ElasticsearchRepository {

  /**
    * Queries if ES is up
    */
  def queryHealth(): Future[String]

  def queryUprn(uprn: String, startDate: String, endDate: String, historical: Boolean = true, epoch: String = ""): Future[Option[HybridAddressOpt]]

  def queryUprnSkinny(uprn: String, startDate: String, endDate: String, historical: Boolean = true, epoch: String = ""): Future[Option[HybridAddressSkinny]]

  def queryPartialAddress(input: String, start: Int, limit: Int, filters: String, startDate: String = "", endDate: String = "", historical: Boolean = true, verbose: Boolean = true, epoch: String = ""): Future[HybridAddressCollection]

  def queryPartialAddressSkinny(input: String, start: Int, limit: Int, filters: String, startDate: String = "", endDate: String = "", historical: Boolean = true, verbose: Boolean = false, epoch: String = ""): Future[HybridAddressesSkinny]

  def queryPostcode(postcode: String, start: Int, limit: Int, filters: String, startDate: String = "", endDate: String = "", historical: Boolean = true, verbose: Boolean = true, epoch: String = ""): Future[HybridAddressCollection]

  def queryPostcodeSkinny(postcode: String, start: Int, limit: Int, filters: String, startDate: String = "", endDate: String = "", historical: Boolean = true, verbose: Boolean = false, epoch: String = ""): Future[HybridAddressesSkinny]

  def queryRandom(filters: String, limit: Int, historical: Boolean = true, verbose: Boolean = true, epoch: String = ""): Future[HybridAddressCollection]

  def queryRandomSkinny(filters: String, limit: Int, historical: Boolean = true, verbose: Boolean = false, epoch: String = ""): Future[HybridAddressesSkinny]

  def queryAddresses(tokens: Map[String, String], start: Int, limit: Int, filters: String, range: String, lat: String, lon: String, startDate: String = "", endDate: String = "", queryParamsConfig: Option[QueryParamsConfig] = None, historical: Boolean = true, isBulk: Boolean = true, epoch: String): Future[HybridAddressCollection]

  def generateQueryAddressRequest(tokens: Map[String, String], filters: String, range: String, lat: String, lon: String, startDate: String, endDate: String, queryParamsConfig: Option[QueryParamsConfig] = None, historical: Boolean = true, isBulk: Boolean = true, epoch: String): SearchDefinition

  def queryBulk(requestsData: Stream[BulkAddressRequestData], limit: Int, startDate: String = "", endDate: String = "", queryParamsConfig: Option[QueryParamsConfig] = None, historical: Boolean = true, matchThreshold: Float, includeFullAddress: Boolean = false, epoch: String = ""): Future[Stream[Either[BulkAddressRequestData, Seq[AddressBulkResponseAddress]]]]

  /**
    * Generates the ES request to get addresses from ES
    * Supports all of the different query types
    * Public so that it can be accessed by the debug controller, and tests
    *
    * @param args arguments for the ES query
    * @return Search definition describing an ES query
    */
  def makeQuery(args: QueryArgs): SearchDefinition

  /**
    * Query the address index by UPRN.
    *
    * @param args the query arguments, including the identificator of the address
    * @return Future containing a address or `None` if not in the index
    */
  def runUPRNQuery(args: UPRNArgs): Future[Option[HybridAddressOpt]]

  /**
    * Query the address index by partial address, randomness, postcode, or full address.
    *
    * @param args the query arguments
    * @return Future containing a collection of addresses
    */
  def runMultiResultQuery(args: MultiResultArgs): Future[HybridAddressCollection]

  /**
    * Query ES using MultiSearch endpoint
    *
    * @param args bulk query arguments
    * @return a stream of `Either`, `Right` will contain resulting bulk address,
    *         `Left` will contain request data that is to be re-send
    */
  def runBulkQuery(args: BulkArgs): Future[Stream[Either[BulkAddressRequestData, Seq[AddressBulkResponseAddress]]]]
}
