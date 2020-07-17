package uk.gov.ons.addressIndex.server.modules.presetClassificationFilters

import com.sksamuel.elastic4s.requests.searches.queries.Query

trait PresetClassificationFilter {

  val queryFilter: Seq[Query]

}
