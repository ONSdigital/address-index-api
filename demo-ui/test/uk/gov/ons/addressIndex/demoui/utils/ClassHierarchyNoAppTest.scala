package uk.gov.ons.addressIndex.demoui.utils

import org.scalamock.scalatest.MockFactory
import org.scalatest.{BeforeAndAfterEach, FlatSpec, Matchers, OneInstancePerTest}
import play.api.i18n.{DefaultLangs, DefaultMessagesApi, Langs}
import uk.gov.ons.addressIndex.client.AddressIndexClient
import uk.gov.ons.addressIndex.demoui.modules.DemouiConfigModule

import scala.concurrent.ExecutionContext

class ClassHierarchyNoAppTest extends FlatSpec with BeforeAndAfterEach with Matchers with MockFactory with OneInstancePerTest {

  private trait Fixture {

    implicit val executionContext: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global
    val addressIndexClient: AddressIndexClient = mock[AddressIndexClient]
    val conf: DemouiConfigModule = mock[DemouiConfigModule]
    val messagesApi = new DefaultMessagesApi(
    Map("en" -> Map("category.C" -> "Commercial",
      "category.CL" -> "Leisure - Applicable to recreational sites and enterprises",
      "category.CL06" -> "Indoor / Outdoor Leisure / Sporting Activity / Centre",
      "category.CL06RG" -> "Recreation Ground",
      "category.M" -> "Military",
      "category.MF" -> "Air Force",
      "category.MF99UG" -> "Air Force Military Storage"))
    )
    val langs: Langs = new DefaultLangs()
    val classHierarchy = new ClassHierarchy(messagesApi, langs)
  }

  "ClassHierarchy" should
    "return the original code and the single classification" in new Fixture {

   // Given
    val expectedSeq = " [ C ] [ Commercial ]"

    // When
    val result: String = classHierarchy.analyseClassCode("C")

    result shouldBe expectedSeq
  }

  it should "return the original code and the two classifications in the hierarchy" in new Fixture {
      // Given
      val expectedSeq = " [ CL ] [ Commercial ] [ Leisure - Applicable to recreational sites and enterprises ]"

      // When
      val result: String = classHierarchy.analyseClassCode("CL")

      result shouldBe expectedSeq
  }

  it should "return the original code and the three classifications in the hierarchy" in new Fixture {
    // Given
      val expectedSeq = " [ CL06 ] [ Commercial ] [ Leisure - Applicable to recreational sites and enterprises ] [ Indoor / Outdoor Leisure / Sporting Activity / Centre ]"

     // When
      val result: String = classHierarchy.analyseClassCode("CL06")

    result shouldBe expectedSeq
  }

  it should "return the original code and the four classifications in the hierarchy" in new Fixture {
       // Given
      val expectedSeq = " [ CL06RG ] [ Commercial ] [ Leisure - Applicable to recreational sites and enterprises ] [ Indoor / Outdoor Leisure / Sporting Activity / Centre ] [ Recreation Ground ]"

      // When
      val result: String = classHierarchy.analyseClassCode("CL06RG")

    result shouldBe expectedSeq
  }

  it should "return the original unknown code" in new Fixture {
     // Given
     val expectedSeq = " [ AB12AB ]"
   // When
     val result: String = classHierarchy.analyseClassCode("AB12AB")
     result shouldBe expectedSeq

  }

  it should "return the original code and the three classifications in the (partial) hierarchy" in new Fixture {
     // Given
     val expectedSeq = " [ MF99UG ] [ Military ] [ Air Force ] [ Air Force Military Storage ]"
      // When
     val result: String = classHierarchy.analyseClassCode("MF99UG")

    result shouldBe expectedSeq
  }

}
