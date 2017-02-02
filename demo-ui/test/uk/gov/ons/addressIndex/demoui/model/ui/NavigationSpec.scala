package uk.gov.ons.addressIndex.demoui.model.ui

import org.scalatest.{FlatSpec, Matchers}

class NavigationSpec extends FlatSpec with Matchers {
  new Application {

  }
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

  it should "have a home link; first" in {
    val expected = Resources.EN.HomeLinkExpected
    val actual = Navigation.default.links.head
    actual shouldBe expected
  }

  it should "have a single match link; second" in {
    val expected = Resources.EN.SingleMatchLinkExpected
    val actual = Navigation.default.links(1)
    actual shouldBe expected
  }

  it should "have a multiple match link; thrid" in {
    val expected = Resources.EN.MultipleMatchLinkExpected
    val actual = Navigation.default.links(2)
    actual shouldBe expected
  }
}
