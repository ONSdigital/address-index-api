package addressIndex.controllers

import javax.inject.{Inject, Singleton}

import addressIndex.modules.ElasticsearchRepository
import play.api.Logger
import play.api.mvc.{Action, AnyContent}
import play.api.libs.functional.syntax._

import scala.concurrent.{ExecutionContext, Future}
import com.sksamuel.elastic4s.ElasticDsl._
import play.api.libs.json.{JsPath, Json, Writes}
import uk.gov.ons.addressIndex.model.AddressScheme._
import uk.gov.ons.addressIndex.model.PostcodeAddressFile
import uk.gov.ons.addressIndex.model.db.index.PostcodeAddressFileAddress

import scala.util.matching.Regex

/**
  * Main API
  *
  * @param esRepo
  * @param ec
  */
@Singleton
class AddressController @Inject()(esRepo : ElasticsearchRepository)(implicit ec : ExecutionContext) extends AddressIndexController {

  val logger = Logger("address-index-server:AddressController")

  case class PostcodeAddressFileReplyUnit( recordIdentifier: String,
                                           changeType: String,
                                           proOrder: String,
                                           uprn: String,
                                           udprn: String,
                                           organizationName: String,
                                           departmentName: String,
                                           subBuildingName: String,
                                           buildingName: String,
                                           buildingNumber: String,
                                           dependentThoroughfare: String,
                                           thoroughfare: String,
                                           doubleDependentLocality: String,
                                           dependentLocality: String,
                                           postTown: String,
                                           postcode: String,
                                           postcodeType: String,
                                           deliveryPointSuffix: String,
                                           poBoxNumber: String,
                                           startDate: String,
                                           lastUpdateDate: String)

  /**
    * The initial data object contains more than 22 fields, hence it cannot be translated into json.
    * That's why we need this one
    */
  object PostcodeAddressFileReplyUnit {
    implicit val pafReplyUnitWrites = Json.format[PostcodeAddressFileReplyUnit]

    def fromPostcodeAddressFileAddress(other: PostcodeAddressFileAddress): PostcodeAddressFileReplyUnit =
      PostcodeAddressFileReplyUnit(
        other.recordIdentifier,
        other.changeType,
        other.proOrder,
        other.uprn,
        other.udprn,
        other.organizationName,
        other.departmentName,
        other.subBuildingName,
        other.buildingName,
        other.buildingNumber,
        other.dependentThoroughfare,
        other.thoroughfare,
        other.doubleDependentLocality,
        other.dependentLocality,
        other.postTown,
        other.postcode,
        other.postcodeType,
        other.deliveryPointSuffix,
        other.poBoxNumber,
        other.startDate,
        other.lastUpdateDate
      )
  }


  /**
    * Test elastic is connected
    * @return
    */
  def elasticTest() : Action[AnyContent] = Action async { implicit req =>
    esRepo.client execute {
      get cluster health
    } map { resp =>
      Ok(resp.toString)
    }
  }

  /**
    * Address query API
    *
    * @param format
    * @param input
    * @return
    */
  def addressQuery(
    input  : String,
    format : String
  ) : Action[AnyContent] = Action async {  implicit req =>
    logger info s"#addressQuery called with input $input"

    val regex : Regex = "(?:[A-Za-z]\\d ?\\d[A-Za-z]{2})|(?:[A-Za-z][A-Za-z\\d]\\d ?\\d[A-Za-z]{2})|(?:[A-Za-z]{2}\\d{2} ?\\d[A-Za-z]{2})|(?:[A-Za-z]\\d[A-Za-z] ?\\d[A-Za-z]{2})|(?:[A-Za-z]{2}\\d[A-Za-z] ?\\d[A-Za-z]{2})".r
    val postcode : String = regex.findFirstIn(input).getOrElse("Not recognised")
    val buildingNumber : Int = input.substring(0, 1).toInt

    logger info s"#addressQuery parsed: postcode: $postcode , buildingNumber: $buildingNumber"

    Future.fromTry(format.toAddressScheme()).flatMap {
      case PostcodeAddressFile() => esRepo.queryAddress(buildingNumber, postcode).map {
        addresses => logger info "#addressQuery got a response from es";Ok {
          Json.toJson(addresses.map(PostcodeAddressFileReplyUnit.fromPostcodeAddressFileAddress))
        }
      }
      case _ => Future.successful(Ok("Wrong format"))
    }
  }

  /**
    * UPRN query api
    *
    * @param uprn
    * @param format
    * @return
    */
  def uprnQuery(
                 uprn   : String,
                 format : String
  ) : Action[AnyContent] = Action async { implicit req =>
    logger info "#uprnQuery called"


    Future.fromTry(format.toAddressScheme()).flatMap {
      case PostcodeAddressFile() => esRepo.queryUprn(uprn).map {
        addresses => Ok {
          Json.toJson(addresses.map(PostcodeAddressFileReplyUnit.fromPostcodeAddressFileAddress))
        }
      }
      case _ => Future.successful(Ok("Wrong format"))
    }
  }

}