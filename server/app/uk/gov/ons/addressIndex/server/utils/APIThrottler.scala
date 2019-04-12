package uk.gov.ons.addressIndex.server.utils

import akka.pattern.CircuitBreaker
import uk.gov.ons.addressIndex.server.utils.ThrottlerStatus.ThrottleStatus

trait APIThrottler {
  def breaker: CircuitBreaker
  def currentStatus: ThrottleStatus
  def setStatus(status: ThrottleStatus)
}

object ThrottlerStatus extends Enumeration {
  type ThrottleStatus = Value
  val Open, Closed, HalfOpen = Value
}
