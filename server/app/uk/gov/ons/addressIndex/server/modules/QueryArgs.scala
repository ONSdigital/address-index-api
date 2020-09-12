package uk.gov.ons.addressIndex.server.modules

import com.sksamuel.elastic4s.requests.searches.queries.{PrefixQuery, Query}
import com.sksamuel.elastic4s.requests.searches.queries.term.TermsQuery
import uk.gov.ons.addressIndex.model.config.QueryParamsConfig
import uk.gov.ons.addressIndex.model.db.BulkAddressRequestData
import uk.gov.ons.addressIndex.server.modules.presetClassificationFilters._

import scala.util.Try

case class DateRange(start: String = "", end: String = "")

case class Region(range: Int, lat: Double, lon: Double)

object Region {
  def fromStrings(range: String, lat: String, lon: String): Option[Region] = {
    Try(Region(range.toInt, lat.toDouble, lon.toDouble)).toOption
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

  private val presetClassificationFilters = Map(
    "residential" -> ResidentialClassificationFilter,
    "commercial" -> CommercialClassificationFilter,
    "workplace" -> WorkplaceClassificationFilter,
    "educational"  -> EducationalClassificationFilter
  )

  def filtersType: String = filters match {
    case "residential" | "commercial" | "workplace" | "educational" => "preset"
    case f if f.endsWith("*") => "prefix"
    case _ => "term"
  }

  val queryFilter: Seq[Query] = if (filters.isEmpty) Seq.empty
    else if (filtersType == "preset") presetClassificationFilters(filters).queryFilter
    else if (filtersType == "prefix") Seq(PrefixQuery("classificationCode", filtersValuePrefix))
    else Seq(TermsQuery("classificationCode",  filters.toUpperCase.split(",")))

  private def filtersValuePrefix: String = filters match {
    case f if f.endsWith("*") => filters.substring(0, filters.length - 1).toUpperCase
    case f => f.toUpperCase()
  }
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

  def bucketPatternOpt: Option[String] = None

  // other address fields

  def tokensOpt: Option[Map[String, String]] = None

  def regionOpt: Option[Region] = None

  def isBulkOpt: Option[Boolean] = None

  // other bulk fields

  def requestsDataOpt: Option[Stream[BulkAddressRequestData]] = None

  def matchThresholdOpt: Option[Float] = None

  def includeFullAddressOpt: Option[Boolean] = None

  //def includeAuxiliarySearch: Boolean

  def includeAuxiliarySearchOpt: Option[Boolean] = None
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

  def includeAuxiliarySearchOrDefault: Boolean = this.includeAuxiliarySearchOpt.getOrElse(false)
}

/**
  * Search by UPRN
  *
  * @param uprn the UPRN to search by
  */
final case class UPRNArgs(uprn: String,
                          historical: Boolean = true,
                          epoch: String = "",
                          includeAuxiliarySearch: Boolean = false
                         ) extends QueryArgs {
  override def uprnOpt: Option[String] = Some(uprn)

  override def includeAuxiliarySearchOpt: Option[Boolean] = Some(includeAuxiliarySearch)
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
  * @param fallback whether to try a slow fallback query in the event of a normal query failing
  */
final case class PartialArgs(input: String,
                             fallback: Boolean = false,
                             epoch: String = "",
                             historical: Boolean = false,
                             limit: Int,
                             start: Int = 0,
                             filters: String,
                             filterDateRange: DateRange = DateRange(),
                             verbose: Boolean = false,
                             skinny: Boolean = false,
                             fromsource: String,
                             highlight: String = "on",
                             favourpaf: Boolean = true,
                             favourwelsh: Boolean = true,
                             eboost: Double = 1.0,
                             nboost: Double = 1.0,
                             sboost: Double = 1.0,
                             wboost: Double = 1.0
                            ) extends MultiResultArgs with DateFilterable with StartAtOffset with Skinnyable {
  override def inputOpt: Option[String] = Some(input)

  override def fallbackOpt: Option[Boolean] = Some(fallback)

  override def filterDateRangeOpt: Option[DateRange] = Some(filterDateRange)

  override def startOpt: Option[Int] = Some(start)

  override def skinnyOpt: Option[Boolean] = Some(skinny)

  def inputNumbers: List[String] = input.replaceAll("[A-Za-z][0-9]+","").split("\\D+").filter(_.nonEmpty).toList
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
                              favourpaf: Boolean = true,
                              favourwelsh: Boolean = true,
                              includeAuxiliarySearch: Boolean = false
                             ) extends MultiResultArgs with StartAtOffset with Skinnyable {
  override def postcodeOpt: Option[String] = Some(postcode)

  override def startOpt: Option[Int] = Some(start)

  override def skinnyOpt: Option[Boolean] = Some(skinny)

  override def includeAuxiliarySearchOpt: Option[Boolean] = Some(includeAuxiliarySearch)
}

/**
  * Search by partial postcode and group results
  *
  * @param postcode the postcode to search by
  */
final case class GroupedPostcodeArgs(postcode: String,
                              epoch: String = "",
                              historical: Boolean = true,
                              limit: Int,
                              start: Int = 0,
                              filters: String,
                              verbose: Boolean = true,
                              skinny: Boolean = false,
                              favourpaf: Boolean = true,
                              favourwelsh: Boolean = true
                             ) extends MultiResultArgs with StartAtOffset with Skinnyable {
  override def postcodeOpt: Option[String] = Some(postcode)

  override def startOpt: Option[Int] = Some(start)

  override def skinnyOpt: Option[Boolean] = Some(skinny)
}

/**
  * Search by bucket
  *
  * @param bucketpattern bucket string with possible wilcards
  */
final case class BucketArgs(bucketpattern: String,
                              epoch: String = "",
                              historical: Boolean = true,
                              limit: Int,
                              start: Int = 0,
                              filters: String,
                              verbose: Boolean = true,
                              skinny: Boolean = false,
                              favourpaf: Boolean = true,
                              favourwelsh: Boolean = true
                             ) extends MultiResultArgs with StartAtOffset with Skinnyable {
  override def bucketPatternOpt: Option[String] = Some(bucketpattern)

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
                            fromsource: String = "all",
                            eboost: Double = 1.0,
                            nboost: Double = 1.0,
                            sboost: Double = 1.0,
                            wboost: Double = 1.0
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
                             isBlank: Boolean = false,
                             epoch: String = "",
                             historical: Boolean = true,
                             limit: Int,
                             start: Int = 0,
                             filters: String,
                             filterDateRange: DateRange = DateRange(),
                             verbose: Boolean,
                             queryParamsConfig: Option[QueryParamsConfig] = None,
                             fromsource: String = "all",
                             includeAuxiliarySearch: Boolean = false,
                             eboost: Double = 1.0,
                             nboost: Double = 1.0,
                             sboost: Double = 1.0,
                             wboost: Double = 1.0
                            ) extends MultiResultArgs with StartAtOffset with DateFilterable with Configurable {
  override def inputOpt: Option[String] = Some(input)

  override def tokensOpt: Option[Map[String, String]] = Some(tokens)

  override def regionOpt: Option[Region] = region

  override def isBulkOpt: Option[Boolean] = Some(isBulk)

  override def startOpt: Option[Int] = Some(start)

  override def filterDateRangeOpt: Option[DateRange] = Some(filterDateRange)

  override def queryParamsConfigOpt: Option[QueryParamsConfig] = queryParamsConfig

  override def includeAuxiliarySearchOpt: Option[Boolean] = Some(includeAuxiliarySearch)
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
