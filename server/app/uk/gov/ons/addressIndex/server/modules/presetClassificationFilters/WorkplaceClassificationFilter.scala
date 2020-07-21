package uk.gov.ons.addressIndex.server.modules.presetClassificationFilters

import com.sksamuel.elastic4s.requests.searches.queries.{BoolQuery, PrefixQuery, Query}
import com.sksamuel.elastic4s.requests.searches.queries.term.TermsQuery
import uk.gov.ons.addressIndex.parsers.Tokens

object WorkplaceClassificationFilter extends PresetClassificationFilter {

  override val queryFilter = createQueryFilter()

  def createQueryFilter(workplaceExclusions: Seq[String] = Tokens.workplaceExclusionClassificationList): Seq[Query] = {

    val (prefixCodes, termCodes) = workplaceExclusions.partition(_.endsWith("*"))

    val termsQuery = if (termCodes.isEmpty) Seq.empty else Seq(TermsQuery("classificationCode", termCodes))
    val prefixQueries = prefixCodes map { prefixCode => PrefixQuery("classificationCode", prefixCode.dropRight(1)) }

    val exclusionFilter =  termsQuery ++ prefixQueries

    if (exclusionFilter.isEmpty) Seq.empty else Seq(BoolQuery().withNot(exclusionFilter))
  }
}