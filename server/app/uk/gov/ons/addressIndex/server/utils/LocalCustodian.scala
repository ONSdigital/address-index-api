package uk.gov.ons.addressIndex.server.utils

import javax.inject.{Inject, Singleton}

import play.api.i18n._

@Singleton
class LocalCustodian @Inject()(val messagesApi: MessagesApi)  {

  def analyseCustCode(code: String): String = {

    if (messagesApi.isDefinedAt("custodian." + code)) {
      s" $code |${messagesApi(code)}"
    } else {
      s" $code"
    }
  }
}
