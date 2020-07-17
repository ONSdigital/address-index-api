package uk.gov.ons.addressIndex.server.modules.presetClassificationFilters

import com.sksamuel.elastic4s.requests.searches.queries.{PrefixQuery, Query}

object ResidentialClassificationFilter extends PresetClassificationFilter {

  override val queryFilter: Seq[Query] = Seq(PrefixQuery("classificationCode", "R"))

}
