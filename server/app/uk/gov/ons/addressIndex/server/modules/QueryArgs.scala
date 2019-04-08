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

  def filtersType: String = filters match {
    case "residential" | "commercial" => "prefix"
    case f if f.endsWith("*") => "prefix"
    case _ => "term"
  }

  def filtersValuePrefix: String = filters match {
    case "residential" => "R"
    case "commercial" => "C"
    case f if f.endsWith("*") => filters.substring(0, filters.length - 1).toUpperCase
    case f => f.toUpperCase()
  }

  def filtersValueTerm: Seq[String] = filters.toUpperCase.split(",")
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

  def epochParam: String = if (epoch.isEmpty) "_current" else "_" + epoch

  def historical: Boolean
}

final case class UPRNArgs(uprn: String,
                          historical: Boolean = true,
                          epoch: String = "",
                          filterDateRange: DateRange = DateRange(),
                          skinny: Boolean = false,
                         ) extends QueryArgs with DateFilterable with Skinnyable

sealed abstract class MultiResultArgs extends QueryArgs with Limitable with Filterable {}

final case class PartialArgs(input: String,
                             fallback: Boolean = false,
                             epoch: String = "",
                             historical: Boolean = true,
                             limit: Int,
                             start: Int,
                             filters: String,
                             filterDateRange: DateRange = DateRange(),
                             verbose: Boolean = true,
                             skinny: Boolean = false,
                            ) extends MultiResultArgs with DateFilterable with StartAtOffset with Verboseable with Skinnyable {
  def inputNumbers: List[String] = input.split("\\D+").filter(_.nonEmpty).toList
}

final case class PostcodeArgs(postcode: String,
                              epoch: String = "",
                              historical: Boolean = true,
                              limit: Int,
                              start: Int,
                              filters: String,
                              filterDateRange: DateRange = DateRange(),
                              verbose: Boolean = true,
                              skinny: Boolean = false,
                             ) extends MultiResultArgs with DateFilterable with StartAtOffset with Verboseable with Skinnyable

final case class RandomArgs(epoch: String = "",
                            historical: Boolean = true,
                            filters: String,
                            limit: Int,
                            verbose: Boolean = true,
                            skinny: Boolean = false,
                           ) extends MultiResultArgs with Verboseable with Skinnyable

final case class AddressArgs(tokens: Map[String, String],
                             region: Option[Region],
                             isBulk: Boolean = false,
                             epoch: String = "",
                             historical: Boolean = true,
                             filters: String,
                             filterDateRange: DateRange = DateRange(),
                             start: Int,
                             limit: Int,
                             queryParamsConfig: Option[QueryParamsConfig] = None,
                            ) extends MultiResultArgs with StartAtOffset with DateFilterable with Configurable

final case class BulkArgs(requestsData: Stream[BulkAddressRequestData],
                          matchThreshold: Float,
                          includeFullAddress: Boolean = false,
                          epoch: String = "",
                          historical: Boolean = true,
                          limit: Int,
                          filterDateRange: DateRange = DateRange(),
                          queryParamsConfig: Option[QueryParamsConfig] = None,
                         ) extends QueryArgs with Limitable with DateFilterable with Configurable
