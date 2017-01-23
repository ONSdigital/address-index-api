package uk.gov.ons.addressIndex.demoui.views

import uk.gov.ons.addressIndex.demoui.utils.ClassHierarchy
import uk.gov.ons.addressIndex.model.server.response.{AddressInformation, NAGWithFormat}
import play.api.i18n.Messages

object SingleMatchHelper {

  /**
    * @param address
    * @return the approved NAG address
    */
  def getApprovedNAG(address: AddressInformation): Option[NAGWithFormat] = {
    address.nag.flatMap(_.find(_.nag.logicalStatus == "1"))
  }

  /**
    * @param address
    * @param classHierarchy
    * @param messages
    * @return
    */
  def getClassificationCode(
    address: AddressInformation,
    classHierarchy: Option[ClassHierarchy]
  )(implicit messages: Messages): String = {
    getApprovedNAG(address).flatMap { nag =>
      classHierarchy map(_ analyseClassCode nag.nag.classificationCode)
    } getOrElse messages("category.R")
  }
}
