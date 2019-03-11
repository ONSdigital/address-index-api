package uk.gov.ons.addressIndex.model.server.response.address

import play.api.libs.json.{Format, Json}

/**
  * Custodian object for list
  *
  * @param custCode AddressBase custodian code
  * @param custName AddressBase custodian name
  * @param laName   Local Authority name (can be slightly different to custodian Name)
  * @param regCode  Standard geographic code for region
  * @param regName  Region (or country for Welsh) name
  * @param laCode   Standard geographic code for local authority
  */
case class AddressResponseCustodian(custCode: String,
                                    custName: String,
                                    laName: String,
                                    regCode: String,
                                    regName: String,
                                    laCode: String)

object AddressResponseCustodian {
  implicit lazy val addressResponseCustodianFormat: Format[AddressResponseCustodian] = Json.format[AddressResponseCustodian]

}
