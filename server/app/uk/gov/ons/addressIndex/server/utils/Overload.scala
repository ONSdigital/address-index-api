package uk.gov.ons.addressIndex.server.utils

import akka.pattern.CircuitBreaker

trait Overload {
  def breaker: CircuitBreaker
}

