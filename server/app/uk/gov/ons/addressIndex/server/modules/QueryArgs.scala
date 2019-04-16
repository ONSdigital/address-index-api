package uk.gov.ons.addressIndex.server.modules

import uk.gov.ons.addressIndex.model.config.QueryParamsConfig
import uk.gov.ons.addressIndex.model.db.BulkAddressRequestData

import scala.util.Try

case class DateRange(start: String = "", end: String = "")

case class Region(range: Int, lat: Double, lon: Double)

object Region {
  def fromStrings(range: String, lat: String, lon: String): Option[Region] = {
    Try(Region(range.toInt, lat.toDouble, lat.toDouble)).toOption
  }
}

/** the query can be limited to only return a certain number of inputs */
trait Limitable {
  /** the maximum number of results to return */
  def limit: Int
}

/** the query can be given a starting offset from which to return results */
trait StartAtOffset {
  /** the match index at which to begin returning results */
  def start: Int
}

/** the query can be filtered */
trait Filterable {
  /** */
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

/** the query can be filtered by date */
trait DateFilterable {
  /** */
  def filterDateRange: DateRange
}

/** the query can be told to report back in verbose mode */
trait Verboseable {
  /** */
  def verbose: Boolean
}

/** the query can be run over skinny indexes */
trait Skinnyable {
  /** */
  def skinny: Boolean
}

/** the query takes additional configuration parameters */
trait Configurable {
  /** */
  def queryParamsConfig: Option[QueryParamsConfig]
}

/** the root class of all query arguments */
sealed abstract class QueryArgs {
  /** */
  def epoch: String

  def epochParam: String = if (epoch.isEmpty) "_current" else "_" + epoch

  def epochOpt: Option[String] = Some(epoch)

  /** */
  def historical: Boolean

  def historicalOpt: Option[Boolean] = Some(historical)

  // other trait fields

  def limitOpt: Option[Int] = None

  def startOpt: Option[Int] = None

  def filtersOpt: Option[String] = None

  def filterDateRangeOpt: Option[DateRange] = None

  def verboseOpt: Option[Boolean] = None

  def skinnyOpt: Option[Boolean] = None

  def queryParamsConfigOpt: Option[QueryParamsConfig] = None

  // other uprn fields

  def uprnOpt: Option[String] = None

  // other partial fields

  def inputOpt: Option[String] = None

  def fallbackOpt: Option[Boolean] = None

  // other postcode fields

  def postcodeOpt: Option[String] = None

  // other address fields

  def tokensOpt: Option[Map[String, String]] = None

  def regionOpt: Option[Region] = None

  def isBulkOpt: Option[Boolean] = None

  // other bulk fields

  def requestsDataOpt: Option[Stream[BulkAddressRequestData]] = None

  def matchThresholdOpt: Option[Float] = None

  def includeFullAddressOpt: Option[Boolean] = None

  // used when assembling error reports

  def inputOrDefault: String = this.inputOpt.getOrElse("")

  def postcodeOrDefault: String = this.postcodeOpt.getOrElse("")

  def uprnOrDefault: String = this.uprnOpt.getOrElse("")

  def epochOrDefault: String = this.epochOpt.getOrElse("")

  def filterOrDefault: String = this.filtersOpt.getOrElse("")

  def historicalOrDefault: Boolean = this.historicalOpt.getOrElse(false)

  def limitOrDefault: Int = this.limitOpt.getOrElse(0)

  def offsetOrDefault: Int = this.startOpt.getOrElse(0)

  def startDateOrDefault: String = this.filterDateRangeOpt.map(_.start).getOrElse("")

  def endDateOrDefault: String = this.filterDateRangeOpt.map(_.end).getOrElse("")

  def verboseOrDefault: Boolean = this.verboseOpt.getOrElse(false)

  def rangeKMOrDefault: String = this.regionOpt.map(_.range.toString).getOrElse("")

  def latitudeOrDefault: String = this.regionOpt.map(_.lat.toString).getOrElse("")

  def longitudeOrDefault: String = this.regionOpt.map(_.lon.toString).getOrElse("")

  def matchThresholdOrDefault: Float = this.matchThresholdOpt.getOrElse(0f)
}

/**
  * Search by UPRN
  *
  * @param uprn the UPRN to search by
  */
final case class UPRNArgs(uprn: String,
                          historical: Boolean = true,
                          epoch: String = "",
                         ) extends QueryArgs {
  override def uprnOpt: Option[String] = Some(uprn)
}

sealed abstract class MultiResultArgs extends QueryArgs with Limitable with Filterable with Verboseable {
  override def limitOpt: Option[Int] = Some(limit)

  override def filtersOpt: Option[String] = Some(filters)

  override def verboseOpt: Option[Boolean] = Some(verbose)
}

/**
  * Search according to a partial search
  *
  * @param input    the partial search to search by
  * @param fallback whether to generate a fallback query instead of a
  */
final case class PartialArgs(input: String,
                             fallback: Boolean = false,
                             epoch: String = "",
                             historical: Boolean = true,
                             limit: Int,
                             start: Int = 0,
                             filters: String,
                             filterDateRange: DateRange = DateRange(),
                             verbose: Boolean = true,
                             skinny: Boolean = false,
                            ) extends MultiResultArgs with DateFilterable with StartAtOffset with Skinnyable {
  override def inputOpt: Option[String] = Some(input)

  override def fallbackOpt: Option[Boolean] = Some(fallback)

  override def filterDateRangeOpt: Option[DateRange] = Some(filterDateRange)

  override def startOpt: Option[Int] = Some(start)

  override def skinnyOpt: Option[Boolean] = Some(skinny)

  def inputNumbers: List[String] = input.split("\\D+").filter(_.nonEmpty).toList
}

/**
  * Search by postcode
  *
  * @param postcode the postcode to search by
  */
final case class PostcodeArgs(postcode: String,
                              epoch: String = "",
                              historical: Boolean = true,
                              limit: Int,
                              start: Int = 0,
                              filters: String,
                              verbose: Boolean = true,
                              skinny: Boolean = false,
                             ) extends MultiResultArgs with StartAtOffset with Skinnyable {
  override def postcodeOpt: Option[String] = Some(postcode)

  override def startOpt: Option[Int] = Some(start)

  override def skinnyOpt: Option[Boolean] = Some(skinny)
}

/**
  * Search at random
  */
final case class RandomArgs(epoch: String = "",
                            historical: Boolean = true,
                            filters: String,
                            limit: Int,
                            verbose: Boolean = true,
                            skinny: Boolean = false,
                           ) extends MultiResultArgs with Skinnyable {
  override def skinnyOpt: Option[Boolean] = Some(skinny)
}

/**
  * Search according to a list of tokens
  *
  * @param tokens address tokens to search by
  */
final case class AddressArgs(input: String,
                             tokens: Map[String, String],
                             region: Option[Region],
                             isBulk: Boolean = false,
                             epoch: String = "",
                             historical: Boolean = true,
                             limit: Int,
                             start: Int = 0,
                             filters: String,
                             filterDateRange: DateRange = DateRange(),
                             verbose: Boolean,
                             queryParamsConfig: Option[QueryParamsConfig] = None,
                            ) extends MultiResultArgs with StartAtOffset with DateFilterable with Configurable {
  override def inputOpt: Option[String] = Some(input)

  override def tokensOpt: Option[Map[String, String]] = Some(tokens)

  override def regionOpt: Option[Region] = region

  override def isBulkOpt: Option[Boolean] = Some(isBulk)

  override def startOpt: Option[Int] = Some(start)

  override def filterDateRangeOpt: Option[DateRange] = Some(filterDateRange)

  override def queryParamsConfigOpt: Option[QueryParamsConfig] = queryParamsConfig
}

/**
  * @param requestsData   data that will be used in the multi search request
  * @param matchThreshold required match quality, below which results are discarded
  */
final case class BulkArgs(requestsData: Stream[BulkAddressRequestData],
                          matchThreshold: Float,
                          includeFullAddress: Boolean = false,
                          epoch: String = "",
                          historical: Boolean = true,
                          limit: Int,
                          filterDateRange: DateRange = DateRange(),
                          queryParamsConfig: Option[QueryParamsConfig] = None,
                         ) extends QueryArgs with Limitable with DateFilterable with Configurable {
  override def requestsDataOpt: Option[Stream[BulkAddressRequestData]] = Some(requestsData)

  override def matchThresholdOpt: Option[Float] = Some(matchThreshold)

  override def includeFullAddressOpt: Option[Boolean] = Some(includeFullAddress)

  override def limitOpt: Option[Int] = Some(limit)

  override def filterDateRangeOpt: Option[DateRange] = Some(filterDateRange)

  override def queryParamsConfigOpt: Option[QueryParamsConfig] = queryParamsConfig
}
