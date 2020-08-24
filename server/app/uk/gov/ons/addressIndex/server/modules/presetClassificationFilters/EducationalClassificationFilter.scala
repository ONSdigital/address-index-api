package uk.gov.ons.addressIndex.server.modules.presetClassificationFilters

import com.sksamuel.elastic4s.requests.searches.queries.PrefixQuery

object EducationalClassificationFilter extends PresetClassificationFilter {

  override val queryFilter = Seq(PrefixQuery("classificationCode", "CE"))

}
