package uk.gov.ons.addressIndex.demoui.utils

import org.scalamock.function.FunctionAdapter2
import org.scalamock.scalatest.MockFactory
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.flatspec._
import org.scalatest.matchers._
import uk.gov.ons.addressIndex.client.AddressIndexClient
import uk.gov.ons.addressIndex.demoui.client.AddressIndexClientMock
import uk.gov.ons.addressIndex.demoui.client.AddressIndexClientMock.mockAddressResponseStatus
import uk.gov.ons.addressIndex.demoui.modules.DemouiConfigModuleMock
import uk.gov.ons.addressIndex.model.AddressIndexUPRNRequest
import uk.gov.ons.addressIndex.model.db.index.{ExpandedRelative, ExpandedSibling}
import uk.gov.ons.addressIndex.model.server.response.address.{AddressResponseAddress, AddressResponseRelative}
import uk.gov.ons.addressIndex.model.server.response.uprn.{AddressByUprnResponse, AddressByUprnResponseContainer}

import scala.concurrent.{ExecutionContext, Future}

class RelativesExpanderNoAppTest extends AnyFlatSpec with should.Matchers with MockFactory with ScalaFutures {
  private trait Fixture {
    val someApiKey = "api-key"
    val someLevel = 0
    val someParents = Seq.empty[Long]
    val uprnOne = 123L
    val uprnTwo = 456L
    val uprnThree = 789L
    val address1 = "1, Gate Reach, Exeter, EX1 1GA"
    val address2 = "2, Gate Reach, Exeter, EX2 2GA"
    val address3 = "3, Gate Reach, Exeter, EX3 3GA"

    implicit val executionContext: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global
    val addressIndexClient: AddressIndexClient = mock[AddressIndexClient]
    val conf: DemouiConfigModuleMock = new DemouiConfigModuleMock
    val relativesExpander = new RelativesExpander(addressIndexClient, conf)

    def anAddressByUprnResponseContainer(uprn: Long, formattedAddress: Option[String]): AddressByUprnResponseContainer = {
      val addressResponseAddressOpt = formattedAddress.map(anAddressResponseFor(uprn))
      AddressByUprnResponseContainer(
        "api-version",
        "data-version",
        response = AddressByUprnResponse(addressResponseAddressOpt, historical = true, "", verbose = true),
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
    val relativeWithSoleSibling = AddressResponseRelative(someLevel, siblings = Seq(uprnOne), someParents)
    (addressIndexClient.uprnQuery(_: AddressIndexUPRNRequest)(_: ExecutionContext)).expects(
      aUprnRequest(withUprn = BigInt(uprnOne), withApiKey = someApiKey, withHistorical = true)).returning(
      Future.successful(anAddressByUprnResponseContainer(uprnOne, Some(address1))))

    whenReady(relativesExpander.futExpandRelatives(someApiKey, Seq(relativeWithSoleSibling))) { expandedRelatives =>
      expandedRelatives should contain theSameElementsAs Seq(
        ExpandedRelative(someLevel, Seq(ExpandedSibling(uprnOne, mixedCaseAddressFor(address1))))
      )
    }
  }

  it should "expand all siblings when a relative has multiple siblings" in new Fixture {
    val relativeWithMultipleSiblings = AddressResponseRelative(someLevel, siblings = Seq(uprnOne, uprnTwo), someParents)
    (addressIndexClient.uprnQuery(_: AddressIndexUPRNRequest)(_: ExecutionContext)).expects(
      aUprnRequest(withUprn = BigInt(uprnOne), withApiKey = someApiKey, withHistorical = true)).returning(
      Future.successful(anAddressByUprnResponseContainer(uprnOne, Some(address1))))
    (addressIndexClient.uprnQuery(_: AddressIndexUPRNRequest)(_: ExecutionContext)).expects(
      aUprnRequest(withUprn = BigInt(uprnTwo), withApiKey = someApiKey, withHistorical = true)).returning(
      Future.successful(anAddressByUprnResponseContainer(uprnTwo, Some(address2))))

    whenReady(relativesExpander.futExpandRelatives(someApiKey, Seq(relativeWithMultipleSiblings))) { expandedRelatives =>
      expandedRelatives should contain theSameElementsAs Seq(
        ExpandedRelative(someLevel, Seq(
          ExpandedSibling(uprnOne, mixedCaseAddressFor(address1)),
          ExpandedSibling(uprnTwo, mixedCaseAddressFor(address2))))
      )
    }
  }

  it should "expand the relative without address retrievals when there are no siblings" in new Fixture {
    val relativeWithNoSiblings = AddressResponseRelative(someLevel, siblings = Seq.empty, someParents)

    whenReady(relativesExpander.futExpandRelatives(someApiKey, Seq(relativeWithNoSiblings))) { expandedRelatives =>
      expandedRelatives should contain theSameElementsAs Seq(
        ExpandedRelative(someLevel, Seq.empty)
      )
    }
  }

  it should "return a 'not found' address when a sibling address is not found" in new Fixture {
    val relativeWithSoleSibling = AddressResponseRelative(someLevel, siblings = Seq(uprnOne), someParents)
    (addressIndexClient.uprnQuery(_: AddressIndexUPRNRequest)(_: ExecutionContext)).expects(
      aUprnRequest(withUprn = BigInt(uprnOne), withApiKey = someApiKey, withHistorical = true)).returning(
      Future.successful(anAddressByUprnResponseContainer(uprnOne, None)))

    whenReady(relativesExpander.futExpandRelatives(someApiKey, Seq(relativeWithSoleSibling))) { expandedRelatives =>
      expandedRelatives should contain theSameElementsAs Seq(
        ExpandedRelative(someLevel, Seq(ExpandedSibling(uprnOne, s"${uprnOne}not found")))
      )
    }
  }

  it should "expand the siblings of all relatives when there are multiple relatives" in new Fixture {
    val relativeWithSoleSibling = AddressResponseRelative(someLevel, siblings = Seq(uprnOne), someParents)
    val relativeWithMultipleSiblings = AddressResponseRelative(someLevel, siblings = Seq(uprnTwo, uprnThree), someParents)
    (addressIndexClient.uprnQuery(_: AddressIndexUPRNRequest)(_: ExecutionContext)).expects(
      aUprnRequest(withUprn = BigInt(uprnOne), withApiKey = someApiKey, withHistorical = true)).returning(
      Future.successful(anAddressByUprnResponseContainer(uprnOne, Some(address1))))
    (addressIndexClient.uprnQuery(_: AddressIndexUPRNRequest)(_: ExecutionContext)).expects(
      aUprnRequest(withUprn = BigInt(uprnTwo), withApiKey = someApiKey, withHistorical = true)).returning(
      Future.successful(anAddressByUprnResponseContainer(uprnTwo, Some(address2))))
    (addressIndexClient.uprnQuery(_: AddressIndexUPRNRequest)(_: ExecutionContext)).expects(
      aUprnRequest(withUprn = BigInt(uprnThree), withApiKey = someApiKey, withHistorical = true)).returning(
      Future.successful(anAddressByUprnResponseContainer(uprnThree, Some(address3))))

    whenReady(relativesExpander.futExpandRelatives(someApiKey, Seq(relativeWithSoleSibling, relativeWithMultipleSiblings))) { expandedRelatives =>
      expandedRelatives should contain theSameElementsAs Seq(
        ExpandedRelative(someLevel, Seq(
          ExpandedSibling(uprnOne, mixedCaseAddressFor(address1)))),
        ExpandedRelative(someLevel, Seq(
          ExpandedSibling(uprnTwo, mixedCaseAddressFor(address2)),
          ExpandedSibling(uprnThree, mixedCaseAddressFor(address3))))
      )
    }
  }

  /*
   * The await based implementation will propagate any exception encountered by ANY of the futures.
   * We will return a failed Future if any future fails.
   */
  it should "return a Failure if any of the sibling address lookups fail" in new Fixture {
    val cause = new Exception("something went wrong")
    val relativeOne = AddressResponseRelative(someLevel, siblings = Seq(uprnOne), someParents)
    val relativeTwo = AddressResponseRelative(someLevel, siblings = Seq(uprnTwo), someParents)
    (addressIndexClient.uprnQuery(_: AddressIndexUPRNRequest)(_: ExecutionContext)).expects(
      aUprnRequest(withUprn = BigInt(uprnOne), withApiKey = someApiKey, withHistorical = true)).returning(
      Future.successful(anAddressByUprnResponseContainer(uprnOne, Some(address1))))
    (addressIndexClient.uprnQuery(_: AddressIndexUPRNRequest)(_: ExecutionContext)).expects(
      aUprnRequest(withUprn = BigInt(uprnTwo), withApiKey = someApiKey, withHistorical = true)).returning(
      Future.failed(cause))

    val result: Future[Seq[ExpandedRelative]] = relativesExpander.futExpandRelatives(someApiKey, Seq(relativeOne, relativeTwo))

    result.failed.futureValue shouldBe cause
  }
}
