package uk.gov.ons.addressIndex.server.utils

import org.scalatest.{FlatSpec, Matchers}
import play.api.i18n._
import play.api.test.WithApplication

class LocalCustodianTest extends FlatSpec with Matchers {
  "LocalCustodian" should
    "return the original code and the local authority name" in new WithApplication {
    val messagesApi = app.injector.instanceOf[MessagesApi]
    val localCustodian = new LocalCustodian(messagesApi)

    // Given
    val expectedSeq = "435 | MILTON KEYNES"

    // When
    val result = localCustodian.analyseCustCode("435")

    result shouldBe expectedSeq
  }

  "LocalCustodian" should
    "return the original unknown code" in new WithApplication {
    val messagesApi = app.injector.instanceOf[MessagesApi]
    val localCustodian = new LocalCustodian(messagesApi)

    // Given
    val expectedSeq = "42"

    // When
    val result = localCustodian.analyseCustCode("42")

    result shouldBe expectedSeq
  }

}
