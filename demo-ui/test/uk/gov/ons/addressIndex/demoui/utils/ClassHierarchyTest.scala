package uk.gov.ons.addressIndex.demoui.utils

import org.scalatest.{FlatSpec, Matchers}
import play.api.i18n._
import play.api.test.WithApplication

class ClassHierarchyTest extends FlatSpec with Matchers {
  "ClassHierarchy" should
    "return the original code and the single classification" in new WithApplication {
    val messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]
    val langs: Langs = app.injector.instanceOf[Langs]
    val classHierarchy = new ClassHierarchy(messagesApi, langs)

    // Given
    val expectedSeq = " [ C ] [ Commercial ]"

    // When
    val result: String = classHierarchy.analyseClassCode("C")

    result shouldBe expectedSeq
  }

  "ClassHierarchy" should
    "return the original code and the two classifications in the hierarchy" in new WithApplication {
    val messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]
    val langs: Langs = app.injector.instanceOf[Langs]
    val classHierarchy = new ClassHierarchy(messagesApi, langs)

    // Given
    val expectedSeq = " [ CL ] [ Commercial ] [ Leisure - Applicable to recreational sites and enterprises ]"

    // When
    val result: String = classHierarchy.analyseClassCode("CL")

    result shouldBe expectedSeq
  }

  "ClassHierarchy" should
    "return the original code and the three classifications in the hierarchy" in new WithApplication {
    val messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]
    val langs: Langs = app.injector.instanceOf[Langs]
    val classHierarchy = new ClassHierarchy(messagesApi, langs)

    // Given
    val expectedSeq = " [ CL06 ] [ Commercial ] [ Leisure - Applicable to recreational sites and enterprises ] [ Indoor / Outdoor Leisure / Sporting Activity / Centre ]"

    // When
    val result: String = classHierarchy.analyseClassCode("CL06")

    result shouldBe expectedSeq
  }

  "ClassHierarchy" should
    "return the original code and the four classifications in the hierarchy" in new WithApplication {
    val messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]
    val langs: Langs = app.injector.instanceOf[Langs]
    val classHierarchy = new ClassHierarchy(messagesApi, langs)

    // Given
    val expectedSeq = " [ CL06RG ] [ Commercial ] [ Leisure - Applicable to recreational sites and enterprises ] [ Indoor / Outdoor Leisure / Sporting Activity / Centre ] [ Recreation Ground ]"

    // When
    val result: String = classHierarchy.analyseClassCode("CL06RG")

    result shouldBe expectedSeq
  }

  "ClassHierarchy" should
    "return the original unknown code" in new WithApplication {
    val messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]
    val langs: Langs = app.injector.instanceOf[Langs]
    val classHierarchy = new ClassHierarchy(messagesApi, langs)

    // Given
    val expectedSeq = " [ AB12AB ]"

    // When
    val result: String = classHierarchy.analyseClassCode("AB12AB")

    result shouldBe expectedSeq
  }

  "ClassHierarchy" should
    "return the original code and the three classifications in the (partial) hierarchy" in new WithApplication {
    val messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]
    val langs: Langs = app.injector.instanceOf[Langs]
    val classHierarchy = new ClassHierarchy(messagesApi, langs)

    // Given
    val expectedSeq = " [ MF99UG ] [ Military ] [ Air Force ] [ Air Force Military Storage ]"

    // When
    val result: String = classHierarchy.analyseClassCode("MF99UG")

    result shouldBe expectedSeq
  }
}
