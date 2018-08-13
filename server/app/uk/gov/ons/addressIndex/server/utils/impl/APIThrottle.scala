package uk.gov.ons.addressIndex.server.utils.impl

import akka.actor.ActorSystem
import akka.pattern.CircuitBreaker
import javax.inject.{Inject, Singleton}
import uk.gov.ons.addressIndex.server.modules.ConfigModule
import uk.gov.ons.addressIndex.server.utils.ThrottlerStatus.ThrottleStatus
import uk.gov.ons.addressIndex.server.utils.{APIThrottler, ThrottlerStatus}

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

  override def currentStatus: ThrottleStatus = {
    if (breaker.isOpen) ThrottlerStatus.Open
    if (breaker.isClosed) ThrottlerStatus.Closed
    ThrottlerStatus.HalfOpen
  }

  override def breaker: CircuitBreaker = {
    new CircuitBreaker(
      system.scheduler,
      maxFailures = circuitBreakerMaxFailures,
      callTimeout = circuitBreakerCallTimeout.milliseconds,
      resetTimeout = circuitBreakerResetTimeout milliseconds)
      .onOpen({
        logger.warn("Circuit breaker is now open")
      })
      .onClose({
        logger.warn("circuit breaker is now closed")
      })
      .onHalfOpen({
        logger.warn("Circuit breaker is now half-open")
      })
  }
}

