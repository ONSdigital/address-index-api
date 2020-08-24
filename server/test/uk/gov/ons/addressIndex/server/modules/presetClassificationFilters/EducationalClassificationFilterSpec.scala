package uk.gov.ons.addressIndex.server.modules.presetClassificationFilters

import com.sksamuel.elastic4s.requests.searches.queries.PrefixQuery
import org.scalatest.Matchers._
import org.scalatest.WordSpec

class EducationalClassificationFilterSpec extends WordSpec {

  "EducationalClassificationFilter" should {

    "return a CE prefix query" in {

      // Given
      val expected = Seq(PrefixQuery("classificationCode", "CE"))

      // When
      val result = EducationalClassificationFilter.queryFilter

      // Then
      result shouldBe expected
    }
  }
}