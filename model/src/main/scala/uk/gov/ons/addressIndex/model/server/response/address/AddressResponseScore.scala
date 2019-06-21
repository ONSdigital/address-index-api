package uk.gov.ons.addressIndex.model.server.response.address

import play.api.libs.json.{Format, Json}

/**
  * Hopper Score - this class contains debug fields that may not be in final product
  *
  * @param objectScore
  * @param structuralScore
  * @param buildingScore
  * @param localityScore
  * @param unitScore
  * @param buildingScoreDebug
  * @param localityScoreDebug
  * @param unitScoreDebug
  */
case class AddressResponseScore(objectScore: Double,
                                structuralScore: Double,
                                buildingScore: Double,
                                localityScore: Double,
                                unitScore: Double,
                                buildingScoreDebug: String,
                                localityScoreDebug: String,
                                unitScoreDebug: String,
                                ambiguityPenalty: Double)

object AddressResponseScore {
  implicit lazy val addressResponseScoreFormat: Format[AddressResponseScore] = Json.format[AddressResponseScore]
}
