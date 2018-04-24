package uk.gov.ons.addressIndex.demoui.utils

import java.util.UUID

import javax.inject.{Inject, Singleton}
import uk.gov.ons.addressIndex.client.AddressIndexClient
import uk.gov.ons.addressIndex.model.AddressIndexUPRNRequest
import uk.gov.ons.addressIndex.model.db.index.{ExpandedRelative, ExpandedSibling}
import uk.gov.ons.addressIndex.model.server.response.{AddressByUprnResponseContainer, AddressResponseRelative}

import scala.concurrent.{ExecutionContext, Future}
import scala.language.postfixOps

@Singleton
class RelativesExpander @Inject ()(
  apiClient: AddressIndexClient
)(implicit ec: ExecutionContext) {

  def futExpandRelatives(apiKey: String, relatives: Seq[AddressResponseRelative]): Future[Seq[ExpandedRelative]] =
    Future.sequence(relatives.map(futExpandRelative(apiKey)))

  private def futExpandRelative(apiKey: String)(rel: AddressResponseRelative): Future[ExpandedRelative] =
    getFutAllExpandedSiblings(apiKey, rel.siblings).map(ExpandedRelative(rel.level, _))

  private def getFutAllExpandedSiblings(apiKey: String, uprns: Seq[Long]): Future[Seq[ExpandedSibling]] =
    Future.sequence(uprns.map(getFutExpandedSibling(apiKey, _)))

  private def getFutExpandedSibling(apiKey: String, uprn: Long): Future[ExpandedSibling] =
    getFutAddressByUprn(apiKey, uprn).map {
      (toMixedCase _).andThen(toExpandedSibling(uprn))
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
