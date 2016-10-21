package addressIndex

import javax.inject.{Inject, Singleton}
import scala.concurrent.{Await, Future}
import scala.concurrent.duration._
import addressIndex.modules.{AddressIndexConfigModule, ElasticsearchRepository}
import com.google.inject.ImplementedBy
import play.api.Logger
import scala.concurrent.ExecutionContext.Implicits.global

@ImplementedBy(classOf[SystemBootstrap])
trait Bootstrap {
  /**
    * Defines what should happen at application start.
    */
  def applicationStart(): Unit
  applicationStart()
}

/**
  * Eager singleton.
  */
@Singleton
class SystemBootstrap @Inject()(conf : AddressIndexConfigModule, esRepo : ElasticsearchRepository) extends Bootstrap {

  val logger = Logger("address-index:SystemBootstrap")

  def applicationStart(): Unit = {
    Await result(
      conf.config.runMode match {
        case "dev"  =>
          logger info "running in dev mode"
          Future sequence Seq(
            esRepo deleteAll,
            esRepo createAll
          )
        case "prod" =>
          logger info "running in prod mode"
          Future successful ()
      },
      5.seconds
    )
    logger info "`SystemBootstrap` complete"
  }
}