package uk.gov.ons.addressIndex.demoui.utils

import java.util.UUID

import uk.gov.ons.addressIndex.model.db.index.{ExpandedRelative, ExpandedSibling, Relative}
import uk.gov.ons.addressIndex.model.server.response.{AddressByUprnResponseContainer, AddressResponseRelative}
import javax.inject.{Inject, Singleton}
import uk.gov.ons.addressIndex.demoui.client.AddressIndexClientInstance
import uk.gov.ons.addressIndex.model.AddressIndexUPRNRequest

import scala.language.postfixOps
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.concurrent.duration._
import scala.util.{Failure, Success, Try}

@Singleton
class RelativesExpander @Inject ()(
  apiClient: AddressIndexClientInstance
)(implicit ec: ExecutionContext) {

  def expandRelatives(apiKey: String, relatives: Seq[AddressResponseRelative]): Seq[ExpandedRelative] = {
    relatives.map{rel => expandRelative(apiKey,rel)}
  }

  def expandRelative (apiKey: String, rel: AddressResponseRelative):  ExpandedRelative = {
    ExpandedRelative (
      rel.level,
      getExpandedSiblings(apiKey, rel.siblings)
    )
  }

  def getExpandedSiblings(apiKey: String, uprns: Seq[Long]): Seq[ExpandedSibling] = {
    uprns.map(uprn => {
     ExpandedSibling(uprn,Await.result(getAddressFromUprn(apiKey,uprn), 1 seconds))
    })
  }

  def getAddressFromUprn(apiKey: String, uprn: Long): Future[String] = {
    val numericUPRN = BigInt(uprn)
    apiClient.uprnQuery(
      AddressIndexUPRNRequest(
        uprn = numericUPRN,
        id = UUID.randomUUID,
        historical = true,
        apiKey = apiKey
      )
    ).map { resp: AddressByUprnResponseContainer =>
      resp.response.address.map ({ add =>
        addressToMixedCase(add.formattedAddress)
      }).getOrElse(uprn + "not found")
    }
  }

  def addressToMixedCase(ucAddress: String): String =  {
   ucAddress.substring(0, ucAddress.lastIndexOf(",") + 1).toLowerCase.split(" ").map(_.capitalize).mkString(" ") +
     ucAddress.substring(ucAddress.lastIndexOf(",") + 1, ucAddress.length)
  }
}

