package uk.gov.ons.addressIndex.demoui.model.ui

import org.scalatest.Matchers
import org.scalatestplus.play.PlaySpec
import play.api.test.WithApplication

class NavigationSpec extends PlaySpec with Matchers {

  object Resources {
    object EN {
      val HomeLinkExpected = Link(
        href = "/",
        label = "Home"
      )
      val SingleMatchLinkExpected = Link(
        href = "/addresses",
        label = "Single Match"
      )
      val MultipleMatchLinkExpected = Link(
        href = "/bulkAddresses",
        label = "Multiple Match"
      )
    }
  }

  "Naviagtion" should {
    "have a home link; first" in new WithApplication {
      val expected = Resources.EN.HomeLinkExpected
      val actual = Navigation.default.links.head
      actual shouldBe expected
    }

    "have a single match link; second" in {
      val expected = Resources.EN.SingleMatchLinkExpected
      val actual = Navigation.default.links(1)
      actual shouldBe expected
    }

    "have a multiple match link; third" in {
      val expected = Resources.EN.MultipleMatchLinkExpected
      val actual = Navigation.default.links(2)
      actual shouldBe expected
    }
  }
}
