package uk.gov.ons.addressIndex.demoui.model.formdata

/**
  * Backing class for the AiUser data form.
  * Requirements:
  * <ul>
  * <li> All fields are public,
  * <li> All fields are of type String.
  * </ul>
  */

case class LoginCredentials(userName: String, password: String)
