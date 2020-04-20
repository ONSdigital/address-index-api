package uk.gov.ons.addressIndex.server.utils

import akka.actor.ActorSystem
import akka.pattern.CircuitBreaker
import javax.inject.{Inject, Singleton}
import uk.gov.ons.addressIndex.server.modules.ConfigModule

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import scala.language.postfixOps

@Singleton
class APIThrottle @Inject()(conf: ConfigModule)(implicit ec: ExecutionContext) {

  private lazy val logger = GenericLogger("address-index-server:APIThrottle")

  private val esConf = conf.config.elasticSearch
  private val system: ActorSystem = ActorSystem("ONS")
  private val circuitBreakerMaxFailures: Int = esConf.circuitBreakerMaxFailures
  private val circuitBreakerCallTimeout: Int = esConf.circuitBreakerCallTimeout
  private val circuitBreakerResetTimeout: Int = esConf.circuitBreakerResetTimeout
  private val circuitBreakerMaxResetTimeout: Int = esConf.circuitBreakerMaxResetTimeout
  private val circuitBreakerExponentialBackoffFactor: Double = esConf.circuitBreakerExponentialBackoffFactor

  val breaker: CircuitBreaker = {
    new CircuitBreaker(
      system.scheduler,
      maxFailures = circuitBreakerMaxFailures,
      callTimeout = circuitBreakerCallTimeout.milliseconds,
      resetTimeout = circuitBreakerResetTimeout milliseconds,
      maxResetTimeout = circuitBreakerMaxResetTimeout.milliseconds,
      exponentialBackoffFactor = circuitBreakerExponentialBackoffFactor)
      .onOpen({
        logger.info("Circuit breaker is now open")
      })
      .onHalfOpen({
        logger.info("Circuit breaker is now half-open")
      })
      .onClose({
        logger.info("Circuit Breaker is now closed")
      })
  }
}

