package uk.gov.ons.addressIndex.server.modules.presetClassificationFilters

import com.sksamuel.elastic4s.requests.searches.queries.PrefixQuery

object CommercialClassificationFilter extends PresetClassificationFilter {

  override val queryFilter = Seq(PrefixQuery("classificationCode", "C"))

}
