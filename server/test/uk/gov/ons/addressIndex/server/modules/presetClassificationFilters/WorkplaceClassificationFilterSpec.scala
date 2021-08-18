package uk.gov.ons.addressIndex.server.modules.presetClassificationFilters

import com.sksamuel.elastic4s.requests.searches.queries.term.TermsQuery
import com.sksamuel.elastic4s.requests.searches.queries.{BoolQuery, PrefixQuery, Query}
import org.scalatest._
import flatspec._
import matchers._
import org.scalatest.matchers.should.Matchers
import org.scalatest.matchers.must.Matchers
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import org.scalatest.wordspec.AnyWordSpec

class WorkplaceClassificationFilterSpec extends AnyWordSpec {

  "WorkplaceClassificationFilter" should {

    "return an empty list when no codes in the classification list" in {

      // Given
      val workplaceExclusionClassificationList = Seq.empty

      val expected = Seq.empty

      // When
      val result = WorkplaceClassificationFilter.createQueryFilter(workplaceExclusionClassificationList)

      // Then
      result shouldBe expected
    }

    "return exclusion prefix filters when prefix codes are supplied in the classification list" in {

      // Given
      val workplaceExclusionClassificationList = Seq("CR*", "R*")

      val expected = Seq(BoolQuery().withNot(
          Seq(
            PrefixQuery("classificationCode", "CR"),
            PrefixQuery("classificationCode", "R")
          )
      ))

      // When
      val result = WorkplaceClassificationFilter.createQueryFilter(workplaceExclusionClassificationList)

      // Then
      result shouldBe expected
    }

    "return exclusion terms filter when term codes are supplied in the classification list" in {

      // Given
      val workplaceExclusionClassificationList = Seq("CR01", "RD20")

      val expected = Seq(BoolQuery().withNot(
        Seq(
          TermsQuery("classificationCode", Seq("CR01", "RD20"))
        )
      ))

      // When
      val result = WorkplaceClassificationFilter.createQueryFilter(workplaceExclusionClassificationList)

      // Then
      result shouldBe expected
    }

    "return exclusion prefix and terms filter when prefix and term codes are supplied in the classification list" in {

      // Given
      val workplaceExclusionClassificationList = Seq("RD20", "CR*", "CR01", "R*")

      val expected = Seq(BoolQuery().withNot(
        Seq(
          TermsQuery("classificationCode", Seq("RD20", "CR01")),
          PrefixQuery("classificationCode", "CR"),
          PrefixQuery("classificationCode", "R")
        )
      ))

      // When
      val result: Seq[Query] = WorkplaceClassificationFilter.createQueryFilter(workplaceExclusionClassificationList)

      // Then
      result shouldBe expected
    }
  }
}