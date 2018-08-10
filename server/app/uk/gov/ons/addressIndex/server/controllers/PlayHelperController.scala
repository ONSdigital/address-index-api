package uk.gov.ons.addressIndex.server.controllers

import javax.inject.Singleton
import play.api.mvc.BaseController
import uk.gov.ons.addressIndex.server.modules.VersionModule
import uk.gov.ons.addressIndex.server.modules.response.Response

@Singleton
abstract class PlayHelperController(versionProvider: VersionModule) extends BaseController with Response {

  // lazy to avoid application crash at startup if ES is down
  override lazy val dataVersion: String = versionProvider.dataVersion
  override lazy val apiVersion: String = versionProvider.apiVersion

}
