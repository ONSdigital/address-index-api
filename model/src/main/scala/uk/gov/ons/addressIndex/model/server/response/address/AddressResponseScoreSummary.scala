package uk.gov.ons.addressIndex.model.server.response.address

import play.api.libs.json.{Format, Json}

/**
  * Score summary object return at top level in response
  *
  * @param maxConfindenceScore 0-100
  * @param maxUnderlyingScore 0-about 50 (copied here from maxScore)
  * @param matchType N, S, M
  * @param confidenceThreshold default 10 (copied from matchThreshold)
  * @param topMatchConfidenceZone H, M, L
  * @param unambiguityScore (best match vs. second best match)
  * @param topMatchUnambiguityZone H, M, L
  * @param reccomendationCode 1, 2, 3
  * @param reccomendationText don't use
  */
case class AddressResponseScoreSummary(maxConfidenceScore: Double,
                                maxUnderlyingScore: Double,
                                matchType: String,
                                confidenceThreshold: Float,
                                topMatchConfidenceZone: String,
                                unambiguityScore: Double,
                                topMatchUnambiguityZone: String,
                                reccomendationCode: Int,
                                reccomendationText: String)
{
  def this() = this(0,0,"",0,"",0,"",0,"")
}

object AddressResponseScoreSummary {
  implicit lazy val addressResponseScoreSummaryFormat: Format[AddressResponseScoreSummary] = Json.format[AddressResponseScoreSummary]
}
