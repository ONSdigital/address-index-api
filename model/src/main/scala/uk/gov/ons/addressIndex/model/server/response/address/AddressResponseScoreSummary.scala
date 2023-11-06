package uk.gov.ons.addressIndex.model.server.response.address

import play.api.libs.json.{Format, Json}

/**
  * Score summary object return at top level in response
  *
  * @param maxConfindenceScore 0-100
  * @param maxUnderlyingScore 0-about 50 (moved here from maxScore)
  * @param matchType N, S, M
  * @param confidenceThreshold default 10 (duplicated for convenience)
  * @param topMatchConfidenceZone H, M, L
  * @param unambiguityScore (best match vs. second best match)
  * @param topMatchUnambiguityZone H, M, L
  */
case class AddressResponseScoreSummary(maxConfidenceScore: Double,
                                maxUnderlyingScore: Double,
                                matchType: String,
                                confidenceThreshold: Float,
                                topMatchConfidenceZone: String,
                                unambiguityScore: Double,
                                topMatchUnambiguityZone: String)

object AddressResponseScoreSummary {
  implicit lazy val addressResponseScoreSummaryFormat: Format[AddressResponseScoreSummary] = Json.format[AddressResponseScoreSummary]
}
