package uk.gov.ons.addressIndex.demoui.modules

import java.util.UUID

import com.google.inject.{ImplementedBy, Inject, Singleton}
import uk.gov.ons.addressIndex.demoui.client.AddressIndexClientInstance
import uk.gov.ons.addressIndex.model.AddressIndexSearchRequest
import uk.gov.ons.addressIndex.model.server.response.AddressResponseVersion
import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Await}
import scala.language.implicitConversions

@ImplementedBy(classOf[DemoUIAddressIndexVersionModule])
trait VersionModule {
  def apiVersion: String
  def dataVersion: String
}

/**
  * Singleton class to get the API version number and data epoch once only
  * @param apiClient
  */
@Singleton
class DemoUIAddressIndexVersionModule @Inject()(
  apiClient: AddressIndexClientInstance) (implicit ec : ExecutionContext) extends VersionModule{

  lazy val apiVersion: String = {

    Await.result(
    apiClient.verisonQuery()
      .map { resp: AddressResponseVersion =>
      resp.apiVersion
    }, 10 seconds)
  }

  lazy val dataVersion: String = {

    Await.result(
      apiClient.verisonQuery()
        .map { resp: AddressResponseVersion =>
          resp.dataVersion
        }, 10 seconds)
  }
}