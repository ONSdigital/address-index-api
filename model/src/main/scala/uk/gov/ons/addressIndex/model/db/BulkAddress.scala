package uk.gov.ons.addressIndex.model.db

import uk.gov.ons.addressIndex.crfscala.CrfScala.CrfTokenResult
import uk.gov.ons.addressIndex.model.db.index.HybridAddress

case class BulkAddress(
  tokens: Map[String, String],
  hybridAddress: HybridAddress
)

case class RejectedRequest(tokens: Seq[CrfTokenResult], exception: Throwable)

case class BulkAddresses(
  successfulBulkAddresses: Seq[BulkAddress],
  failedBulkAddresses: Seq[RejectedRequest]
)
