package uk.gov.ons.addressIndex.server.modules.presetClassificationFilters

import com.sksamuel.elastic4s.ElasticApi.boolQuery
import com.sksamuel.elastic4s.requests.searches.queries.PrefixQuery

object ResidentialClassificationFilter extends PresetClassificationFilter {

  override val queryFilter = Seq(boolQuery().should(
    Seq(PrefixQuery("classificationCode", "RD"),
      PrefixQuery("classificationCode", "RH"),
      PrefixQuery("classificationCode", "RI"),
    )).minimumShouldMatch(1))
}
