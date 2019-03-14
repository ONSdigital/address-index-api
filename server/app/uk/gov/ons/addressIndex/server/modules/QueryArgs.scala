package uk.gov.ons.addressIndex.server.modules

import uk.gov.ons.addressIndex.model.config.QueryParamsConfig
import uk.gov.ons.addressIndex.model.db.BulkAddressRequestData

case class DateRange(start: String = "", end: String = "")

case class Region(range: Int, lat: Double, lon: Double)

// the query can be limited to only return a certain number of inputs
trait Limitable {
  def limit: Int
}

// the query can be given a starting offset from which to return results
trait StartAtOffset {
  def start: Int
}

// the query can be filtered
trait Filterable {
  val filters: String

  def getFiltersType: String = filters match {
    case "residential" | "commercial" => "prefix"
    case f if f.endsWith("*") => "prefix"
    case _ => "term"
  }

  def getFiltersValuePrefix: String = filters match {
    case "residential" => "R"
    case "commercial" => "C"
    case f if f.endsWith("*") => filters.substring(0, filters.length - 1).toUpperCase
    case f => f.toUpperCase()
  }

  def getFiltersValueTerm: Seq[String] = filters.toUpperCase.split(",")
}

// the query can be filtered by date
trait DateFilterable {
  def filterDateRange: DateRange
}

// the query can be told to report back in verbose mode
trait Verboseable {
  def verbose: Boolean
}

// the query can be run over skinny indexes
trait Skinnyable {
  def skinny: Boolean
}

// the query takes additional configuration parameters
trait Configurable {
  def queryParamsConfig: Option[QueryParamsConfig]
}

sealed abstract class QueryArgs {
  def epoch: String

  def getEpochParam(epoch: String): String = if (epoch.isEmpty) "_current" else "_" + epoch

  def historical: Boolean
}

final case class UPRNArgs(uprn: String,
                          historical: Boolean = true,
                          epoch: String = "",
                          skinny: Boolean,
                         ) extends QueryArgs with Skinnyable

// TODO find a better name for this
sealed abstract class NonUPRNArgs extends QueryArgs with Limitable with Filterable with Verboseable with Skinnyable {

}

final case class PartialArgs(input: String,
                             epoch: String = "",
                             historical: Boolean = true,
                             limit: Int,
                             start: Int,
                             filters: String,
                             filterDateRange: DateRange = DateRange(),
                             verbose: Boolean = false,
                             skinny: Boolean = false,
                            ) extends NonUPRNArgs with DateFilterable with StartAtOffset

final case class PostcodeArgs(postcode: String,
                              epoch: String = "",
                              historical: Boolean = true,
                              limit: Int,
                              start: Int,
                              filters: String,
                              filterDateRange: DateRange = DateRange(),
                              verbose: Boolean = false,
                              skinny: Boolean = false,
                             ) extends NonUPRNArgs with DateFilterable with StartAtOffset

final case class RandomArgs(epoch: String = "",
                            historical: Boolean = true,
                            filters: String,
                            limit: Int,
                            verbose: Boolean = false,
                            skinny: Boolean = false,
                           ) extends NonUPRNArgs

sealed abstract class StandardArgs extends QueryArgs with Limitable with DateFilterable with Configurable {

}

final case class AddressArgs(tokens: Map[String, String],
                             region: Region,
                             isBulk: Boolean = true,
                             epoch: String = "",
                             historical: Boolean = true,
                             filters: String,
                             filterDateRange: DateRange = DateRange(),
                             start: Int,
                             limit: Int,
                             queryParamsConfig: Option[QueryParamsConfig] = None,
                            ) extends StandardArgs with Filterable with StartAtOffset

final case class BulkArgs(requestsData: Stream[BulkAddressRequestData],
                          matchThreshold: Float,
                          includeFullAddress: Boolean = false,
                          epoch: String = "",
                          historical: Boolean = true,
                          limit: Int,
                          filterDateRange: DateRange = DateRange(),
                          queryParamsConfig: Option[QueryParamsConfig] = None,
                         ) extends StandardArgs
