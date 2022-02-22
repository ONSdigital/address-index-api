package uk.gov.ons.addressIndex.server.controllers

import javax.inject.{Inject, Singleton}
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import uk.gov.ons.addressIndex.model.server.response.address.{AddressResponseEpoch, AddressResponseEpochList}
import uk.gov.ons.addressIndex.server.modules.VersionModule
import uk.gov.ons.addressIndex.server.modules.response.AddressControllerResponse

/**
  * Returns Available Epoch List
  */
@Singleton
class EpochOptionsController @Inject()(val controllerComponents: ControllerComponents,
                                       versionProvider: VersionModule
                                      ) extends PlayHelperController(versionProvider) with AddressControllerResponse {

  // lazy to avoid application crash at startup if ES is down
  lazy val epochs: Seq[AddressResponseEpoch] = epochList.map{
    epoch => {
      val epochDate: String = epochDates.getOrElse(epoch,"not found")
      val isDefault: Boolean = (epoch == dataVersion)
      AddressResponseEpoch(epoch,isDefault.toString,epochDate)
    }
  }

  lazy val epochResults = new AddressResponseEpochList(epochs)


  def epochQuery(): Action[AnyContent] = Action {
    Ok(Json.toJson(epochResults))
  }

}
