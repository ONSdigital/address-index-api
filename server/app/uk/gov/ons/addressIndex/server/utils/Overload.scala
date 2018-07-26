package uk.gov.ons.addressIndex.server.utils

import akka.pattern.CircuitBreaker
import uk.gov.ons.addressIndex.server.utils.ProtectorStatus.ProtectorStatus

trait Overload {
  def breaker: CircuitBreaker
  def currentStatus: ProtectorStatus
}

object ProtectorStatus extends Enumeration {
  type ProtectorStatus = Value
  val Open, Closed, HalfOpen = Value
}
