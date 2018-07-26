package uk.gov.ons.addressIndex.server.utils.impl

import akka.actor.ActorSystem
import akka.pattern.CircuitBreaker
import javax.inject.{Inject, Singleton}
import play.api.Logger
import uk.gov.ons.addressIndex.server.modules.ConfigModule
import uk.gov.ons.addressIndex.server.utils.{Overload, ProtectorStatus}
import uk.gov.ons.addressIndex.server.utils.ProtectorStatus.ProtectorStatus

import scala.concurrent.ExecutionContext
import scala.language.postfixOps
import scala.concurrent.duration._


@Singleton
class OverloadProtector @Inject()(conf: ConfigModule)(implicit ec: ExecutionContext) extends Overload {
  private val logger = Logger("address-index-server:OverloadProtector")

  private val esConf = conf.config.elasticSearch
  private val system: ActorSystem = ActorSystem("ONS")
  private val circuitBreakerMaxFailures: Int = esConf.circuitBreakerMaxFailures
  private val circuitBreakerCallTimeout: Int = esConf.circuitBreakerCallTimeout
  private val circuitBreakerResetTimeout: Int = esConf.circuitBreakerResetTimeout

  private var status: ProtectorStatus = ProtectorStatus.Closed // scalastyle:ignore

  override def currentStatus: ProtectorStatus = {
    status
  }

  override def breaker: CircuitBreaker = {
    new CircuitBreaker(
      system.scheduler,
      maxFailures = circuitBreakerMaxFailures,
      callTimeout = circuitBreakerCallTimeout.milliseconds,
      resetTimeout = circuitBreakerResetTimeout milliseconds)
      .onOpen({
        logger.warn("Circuit breaker is now open")
        status = ProtectorStatus.Open
      })
      .onClose({
        logger.warn("circuit breaker is now closed")
        status = ProtectorStatus.Closed
      })
      .onHalfOpen({
        logger.warn("Circuit breaker is now half-open")
        status = ProtectorStatus.HalfOpen
      })
  }
}

