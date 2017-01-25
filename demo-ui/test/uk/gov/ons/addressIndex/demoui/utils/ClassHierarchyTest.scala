package uk.gov.ons.addressIndex.demoui.utils

import org.scalatestplus.play.PlaySpec
import play.api.i18n._
import play.api.test.WithApplication

class ClassHierarchyTest extends PlaySpec {
  "ClassHierarchy" should {
    new WithApplication {
      val classHierarchy = new ClassHierarchy(
        messagesApi = app.injector.instanceOf[MessagesApi]
      )

      "return the original code and the single classification" in {
        val expected = " [ C ] [ Commercial ]"
        val actual = classHierarchy.analyseClassCode("C")
        actual mustBe expected
      }

      "return the original code and the two classifications in the hierarchy" in {
        val expected = " [ CL ] [ Commercial ] [ Leisure - Applicable to recreational sites and enterprises ]"
        val actual = classHierarchy.analyseClassCode("CL")
        actual mustBe expected
      }

      "return the original code and the four classifications in the hierarchy" in {
        val expected = " [ CL06RG ] [ Commercial ] [ Leisure - Applicable to recreational sites and enterprises ] [ Indoor / Outdoor Leisure / Sporting Activity / Centre ] [ Recreation Ground ]"
        val actual = classHierarchy.analyseClassCode("CL06RG")
        actual mustBe expected
      }

      "return the original unknown code" in {
        val expected = " [ AB12AB ]"
        val actual = classHierarchy.analyseClassCode("AB12AB")
        actual mustBe expected
      }

      "return the original code and the three classifications in the (partial) hierarchy" in {
        val expected = " [ MF99UG ] [ Military ] [ Air Force ] [ Air Force Military Storage ]"
        val actual = classHierarchy.analyseClassCode("MF99UG")
        actual mustBe expected
      }
    }
    ()
  }
}
