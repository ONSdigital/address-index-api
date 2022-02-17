package uk.gov.ons.addressIndex.server.controllers

import play.api.mvc.BaseController
import uk.gov.ons.addressIndex.server.modules.EpochOptionsModule
import uk.gov.ons.addressIndex.server.modules.response.Response

abstract class PlayHelperControllerEpochVersion(versionProvider: EpochOptionsModule) extends BaseController with Response {

  // lazy to avoid application crash at startup if ES is down
  override lazy val dataVersion: String = versionProvider.dataVersion
  override lazy val apiVersion: String = versionProvider.apiVersion
  override lazy val termsAndConditions = versionProvider.termsAndConditions

}
