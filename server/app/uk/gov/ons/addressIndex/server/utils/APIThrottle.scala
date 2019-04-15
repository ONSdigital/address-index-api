package uk.gov.ons.addressIndex.server.utils

import akka.actor.ActorSystem
import akka.pattern.CircuitBreaker
import javax.inject.{Inject, Singleton}
import uk.gov.ons.addressIndex.server.modules.ConfigModule
import uk.gov.ons.addressIndex.server.utils.ThrottlerStatus.ThrottleStatus

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import scala.language.postfixOps

@Singleton
class APIThrottle @Inject()(conf: ConfigModule)(implicit ec: ExecutionContext) extends APIThrottler {

  private lazy val logger = GenericLogger("address-index-server:APIThrottle")

  private val esConf = conf.config.elasticSearch
  private val system: ActorSystem = ActorSystem("ONS")
  private val circuitBreakerMaxFailures: Int = esConf.circuitBreakerMaxFailures
  private val circuitBreakerCallTimeout: Int = esConf.circuitBreakerCallTimeout
  private val circuitBreakerResetTimeout: Int = esConf.circuitBreakerResetTimeout

  var currentStatus: ThrottleStatus = {
    if (breaker.isOpen) {
      ThrottlerStatus.Open
    } else {
      if (breaker.isClosed) {
        ThrottlerStatus.Closed
      } else {
        ThrottlerStatus.HalfOpen
      }
    }
  }

  def setStatus(status:ThrottleStatus) = {
    currentStatus = status
    if (status == ThrottlerStatus.Closed) {
      logger.warn("Circuit breaker is now closed")
    }
  }

  override def breaker: CircuitBreaker = {
    new CircuitBreaker(
      system.scheduler,
      maxFailures = circuitBreakerMaxFailures,
      callTimeout = circuitBreakerCallTimeout.milliseconds,
      resetTimeout = circuitBreakerResetTimeout milliseconds)
      .onOpen({
        logger.warn("Circuit breaker is now open")
        currentStatus = ThrottlerStatus.Open
      })
      .onHalfOpen({
        logger.warn("Circuit breaker is now half-open")
        currentStatus = ThrottlerStatus.HalfOpen
      })
      .onClose({
        logger.warn("circuit breaker is now closed")
        currentStatus = ThrottlerStatus.Closed
      })
  }
}

