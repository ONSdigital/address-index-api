package uk.gov.ons.addressIndex.demoui.utils

import java.util.UUID

import javax.inject.{Inject, Singleton}
import uk.gov.ons.addressIndex.client.AddressIndexClient
import uk.gov.ons.addressIndex.model.AddressIndexUPRNRequest
import uk.gov.ons.addressIndex.model.db.index.{ExpandedRelative, ExpandedSibling}
import uk.gov.ons.addressIndex.model.server.response.{AddressByUprnResponseContainer, AddressResponseRelative}

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.language.postfixOps

@Singleton
class RelativesExpander @Inject ()(
  apiClient: AddressIndexClient
)(implicit ec: ExecutionContext) {

  def futExpandRelatives(apiKey: String, relatives: Seq[AddressResponseRelative]): Future[Seq[ExpandedRelative]] =
    Future.sequence(relatives.map(futExpandRelative(apiKey)))

  private def futExpandRelative(apiKey: String)(rel: AddressResponseRelative): Future[ExpandedRelative] =
    getFutAllExpandedSiblings(apiKey, rel.siblings).map(ExpandedRelative(rel.level, _))

  def expandRelatives(apiKey: String, relatives: Seq[AddressResponseRelative]): Seq[ExpandedRelative] = {
    relatives.map{rel => expandRelative(apiKey,rel)}
  }

  private def expandRelative(apiKey: String, rel: AddressResponseRelative): ExpandedRelative = {
    ExpandedRelative (
      rel.level,
      getExpandedSiblings(apiKey, rel.siblings)
    )
  }

  private def getFutAllExpandedSiblings(apiKey: String, uprns: Seq[Long]): Future[Seq[ExpandedSibling]] =
    Future.sequence(uprns.map(getFutExpandedSibling(apiKey, _)))

  private def getExpandedSiblings(apiKey: String, uprns: Seq[Long]): Seq[ExpandedSibling] = {
    uprns.map(uprn => {
     ExpandedSibling(uprn,Await.result(getAddressFromUprn(apiKey,uprn), 1 seconds))
    })
  }

  private def getFutExpandedSibling(apiKey: String, uprn: Long): Future[ExpandedSibling] =
    getFutAddressByUprn(apiKey, uprn).map {
      (toMixedCase _).andThen(toExpandedSibling(uprn))
    }

  private def getAddressFromUprn(apiKey: String, uprn: Long): Future[String] = {
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

  private def getFutAddressByUprn(apiKey: String, uprn: Long): Future[AddressByUprnResponseContainer] = {
    val numericUPRN = BigInt(uprn)
    apiClient.uprnQuery(
      AddressIndexUPRNRequest(
        uprn = numericUPRN,
        id = UUID.randomUUID,
        historical = true,
        apiKey = apiKey
      )
    )
  }

  private def toExpandedSibling(uprn: Long)(addressOpt: Option[String]): ExpandedSibling =
    ExpandedSibling(uprn, addressOpt.getOrElse(uprn + "not found"))

  private def toMixedCase(container: AddressByUprnResponseContainer): Option[String] =
    container.response.address.map { ara =>
      addressToMixedCase(ara.formattedAddress)
    }

  private [utils] def addressToMixedCase(ucAddress: String): String = {
    ucAddress.substring(0, ucAddress.lastIndexOf(",") + 1).toLowerCase.split(" ").map(_.capitalize).mkString(" ") +
      ucAddress.substring(ucAddress.lastIndexOf(",") + 1, ucAddress.length)
  }
}
