package uk.gov.ons.addressIndex.demoui.modules

import com.google.inject.{ImplementedBy, Inject, Singleton}
import uk.gov.ons.addressIndex.demoui.client.AddressIndexClientInstance
import uk.gov.ons.addressIndex.model.server.response.address.AddressResponseVersion

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext}
import scala.language.postfixOps
import scala.util.Try

@ImplementedBy(classOf[DemoUIAddressIndexVersionModule])
trait VersionModule {
  def apiVersion: String

  def dataVersion: String
}

/**
  * Singleton class to get the API version number and data epoch once only
  *
  * @param apiClient
  */
@Singleton
class DemoUIAddressIndexVersionModule @Inject()(apiClient: AddressIndexClientInstance)
                                               (implicit ec: ExecutionContext) extends VersionModule {
  lazy val apiVersion: String = {
    Try(Await.result(
      apiClient.versionQuery().map { resp: AddressResponseVersion =>
        resp.apiVersion
      }, 6 seconds)).getOrElse(dummyVersion)
  }

  lazy val dataVersion: String = {
    Try(Await.result(
      apiClient.versionQuery().map { resp: AddressResponseVersion =>
        resp.dataVersion
      }, 6 seconds)).getOrElse(dummyVersion)
  }

  val dummyVersion = "not found"
}
