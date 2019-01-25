package uk.gov.ons.addressIndex.demoui.utils

import org.scalamock.function.FunctionAdapter2
import org.scalamock.scalatest.MockFactory
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{FlatSpec, Matchers}
import uk.gov.ons.addressIndex.client.AddressIndexClient
import uk.gov.ons.addressIndex.demoui.client.AddressIndexClientMock
import uk.gov.ons.addressIndex.demoui.client.AddressIndexClientMock.mockAddressResponseStatus
import uk.gov.ons.addressIndex.demoui.modules.DemouiConfigModule
import uk.gov.ons.addressIndex.model.AddressIndexUPRNRequest
import uk.gov.ons.addressIndex.model.db.index.{ExpandedRelative, ExpandedSibling}
import uk.gov.ons.addressIndex.model.server.response.address.{AddressResponseAddress, AddressResponseRelative}
import uk.gov.ons.addressIndex.model.server.response.uprn.{AddressByUprnResponse, AddressByUprnResponseContainer}

import scala.concurrent.{ExecutionContext, Future}

class RelativesExpander_NoApplicationTest extends FlatSpec with Matchers with MockFactory with ScalaFutures {

  private trait Fixture {
    val SomeApiKey = "api-key"
    val SomeLevel = 0
    val SomeParents = Seq.empty[Long]
    val UprnOne = 123L
    val UprnTwo = 456L
    val UprnThree = 789L
    val Address1 = "1, Gate Reach, Exeter, EX1 1GA"
    val Address2 = "2, Gate Reach, Exeter, EX2 2GA"
    val Address3 = "3, Gate Reach, Exeter, EX3 3GA"

    implicit val executionContext = scala.concurrent.ExecutionContext.Implicits.global
    val addressIndexClient = mock[AddressIndexClient]
    val conf = mock[DemouiConfigModule]
    val relativesExpander = new RelativesExpander(addressIndexClient,conf)

    def anAddressByUprnResponseContainer(uprn: Long, formattedAddress: Option[String]): AddressByUprnResponseContainer = {
      val addressResponseAddressOpt = formattedAddress.map(anAddressResponseFor(uprn))
      AddressByUprnResponseContainer(
        "api-version",
        "data-version",
        response = AddressByUprnResponse(addressResponseAddressOpt,true, "", "","",true),
        status = mockAddressResponseStatus,
        errors = Seq.empty)
    }

    private def anAddressResponseFor(uprn: Long)(formattedAddress: String): AddressResponseAddress =
      AddressIndexClientMock.mockAddressResponseAddress.copy(uprn = uprn.toString, formattedAddress = formattedAddress)

    def aUprnRequest(withUprn: BigInt, withApiKey: String, withHistorical: Boolean): FunctionAdapter2[AddressIndexUPRNRequest, ExecutionContext, Boolean] =
      new FunctionAdapter2[AddressIndexUPRNRequest, ExecutionContext, Boolean](
        (uprnRequest, _) => uprnRequestMatches(withUprn, withApiKey, withHistorical)(uprnRequest)
      )

    private def uprnRequestMatches(uprn: BigInt, apiKey: String, historical: Boolean)
                                  (addressIndexUPRNRequest: AddressIndexUPRNRequest): Boolean =
      uprn == addressIndexUPRNRequest.uprn &&
        apiKey == addressIndexUPRNRequest.apiKey &&
        historical == addressIndexUPRNRequest.historical

    def mixedCaseAddressFor(formattedAddress: String): String =
      relativesExpander.addressToMixedCase(formattedAddress)
  }

  "A RelativesExpander" should "expand the sibling when a relative has a sole sibling" in new Fixture {
    val relativeWithSoleSibling = AddressResponseRelative(SomeLevel, siblings = Seq(UprnOne), SomeParents)
    (addressIndexClient.uprnQuery (_: AddressIndexUPRNRequest)(_: ExecutionContext)).expects(
      aUprnRequest(withUprn = BigInt(UprnOne), withApiKey = SomeApiKey, withHistorical = true)).returning(
      Future.successful(anAddressByUprnResponseContainer(UprnOne, Some(Address1))))

    whenReady(relativesExpander.futExpandRelatives(SomeApiKey, Seq(relativeWithSoleSibling))) { expandedRelatives =>
      expandedRelatives should contain theSameElementsAs Seq(
        ExpandedRelative(SomeLevel, Seq(ExpandedSibling(UprnOne, mixedCaseAddressFor(Address1))))
      )
    }
  }

  it should "expand all siblings when a relative has multiple siblings" in new Fixture {
    val relativeWithMultipleSiblings = AddressResponseRelative(SomeLevel, siblings = Seq(UprnOne, UprnTwo), SomeParents)
    (addressIndexClient.uprnQuery (_: AddressIndexUPRNRequest)(_: ExecutionContext)).expects(
      aUprnRequest(withUprn = BigInt(UprnOne), withApiKey = SomeApiKey, withHistorical = true)).returning(
      Future.successful(anAddressByUprnResponseContainer(UprnOne, Some(Address1))))
    (addressIndexClient.uprnQuery (_: AddressIndexUPRNRequest)(_: ExecutionContext)).expects(
      aUprnRequest(withUprn = BigInt(UprnTwo), withApiKey = SomeApiKey, withHistorical = true)).returning(
      Future.successful(anAddressByUprnResponseContainer(UprnTwo, Some(Address2))))

    whenReady(relativesExpander.futExpandRelatives(SomeApiKey, Seq(relativeWithMultipleSiblings))) { expandedRelatives =>
      expandedRelatives should contain theSameElementsAs Seq(
        ExpandedRelative(SomeLevel, Seq(
          ExpandedSibling(UprnOne, mixedCaseAddressFor(Address1)),
          ExpandedSibling(UprnTwo, mixedCaseAddressFor(Address2))))
      )
    }
  }

  it should "expand the relative without address retrievals when there are no siblings" in new Fixture {
    val relativeWithNoSiblings = AddressResponseRelative(SomeLevel, siblings = Seq.empty, SomeParents)

    whenReady(relativesExpander.futExpandRelatives(SomeApiKey, Seq(relativeWithNoSiblings))) { expandedRelatives =>
      expandedRelatives should contain theSameElementsAs Seq(
        ExpandedRelative(SomeLevel, Seq.empty)
      )
    }
  }

  it should "return a 'not found' address when a sibling address is not found" in new Fixture {
    val relativeWithSoleSibling = AddressResponseRelative(SomeLevel, siblings = Seq(UprnOne), SomeParents)
    (addressIndexClient.uprnQuery (_: AddressIndexUPRNRequest)(_: ExecutionContext)).expects(
      aUprnRequest(withUprn = BigInt(UprnOne), withApiKey = SomeApiKey, withHistorical = true)).returning(
      Future.successful(anAddressByUprnResponseContainer(UprnOne, None)))

    whenReady(relativesExpander.futExpandRelatives(SomeApiKey, Seq(relativeWithSoleSibling))) { expandedRelatives =>
      expandedRelatives should contain theSameElementsAs Seq(
        ExpandedRelative(SomeLevel, Seq(ExpandedSibling(UprnOne, s"${UprnOne}not found")))
      )
    }
  }

  it should "expand the siblings of all relatives when there are multiple relatives" in new Fixture {
    val relativeWithSoleSibling = AddressResponseRelative(SomeLevel, siblings = Seq(UprnOne), SomeParents)
    val relativeWithMultipleSiblings = AddressResponseRelative(SomeLevel, siblings = Seq(UprnTwo, UprnThree), SomeParents)
    (addressIndexClient.uprnQuery (_: AddressIndexUPRNRequest)(_: ExecutionContext)).expects(
      aUprnRequest(withUprn = BigInt(UprnOne), withApiKey = SomeApiKey, withHistorical = true)).returning(
      Future.successful(anAddressByUprnResponseContainer(UprnOne, Some(Address1))))
    (addressIndexClient.uprnQuery (_: AddressIndexUPRNRequest)(_: ExecutionContext)).expects(
      aUprnRequest(withUprn = BigInt(UprnTwo), withApiKey = SomeApiKey, withHistorical = true)).returning(
      Future.successful(anAddressByUprnResponseContainer(UprnTwo, Some(Address2))))
    (addressIndexClient.uprnQuery (_: AddressIndexUPRNRequest)(_: ExecutionContext)).expects(
      aUprnRequest(withUprn = BigInt(UprnThree), withApiKey = SomeApiKey, withHistorical = true)).returning(
      Future.successful(anAddressByUprnResponseContainer(UprnThree, Some(Address3))))

    whenReady(relativesExpander.futExpandRelatives(SomeApiKey, Seq(relativeWithSoleSibling, relativeWithMultipleSiblings))) { expandedRelatives =>
      expandedRelatives should contain theSameElementsAs Seq(
        ExpandedRelative(SomeLevel, Seq(
          ExpandedSibling(UprnOne, mixedCaseAddressFor(Address1)))),
        ExpandedRelative(SomeLevel, Seq(
          ExpandedSibling(UprnTwo, mixedCaseAddressFor(Address2)),
          ExpandedSibling(UprnThree, mixedCaseAddressFor(Address3))))
      )
    }
  }

  /*
   * The await based implementation will propagate any exception encountered by ANY of the futures.
   * We will return a failed Future if any future fails.
   */
  it should "return a Failure if any of the sibling address lookups fail" in new Fixture {
    val cause = new Exception("something went wrong")
    val relativeOne = AddressResponseRelative(SomeLevel, siblings = Seq(UprnOne), SomeParents)
    val relativeTwo = AddressResponseRelative(SomeLevel, siblings = Seq(UprnTwo), SomeParents)
    (addressIndexClient.uprnQuery (_: AddressIndexUPRNRequest)(_: ExecutionContext)).expects(
      aUprnRequest(withUprn = BigInt(UprnOne), withApiKey = SomeApiKey, withHistorical = true)).returning(
      Future.successful(anAddressByUprnResponseContainer(UprnOne, Some(Address1))))
    (addressIndexClient.uprnQuery (_: AddressIndexUPRNRequest)(_: ExecutionContext)).expects(
      aUprnRequest(withUprn = BigInt(UprnTwo), withApiKey = SomeApiKey, withHistorical = true)).returning(
        Future.failed(cause))

    val result = relativesExpander.futExpandRelatives(SomeApiKey, Seq(relativeOne, relativeTwo))

    result.failed.futureValue shouldBe cause
  }
}
