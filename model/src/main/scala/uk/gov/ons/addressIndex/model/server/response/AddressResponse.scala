package uk.gov.ons.addressIndex.model.server.response

import java.util
import com.sksamuel.elastic4s.{HitAs, RichSearchHit}
import play.api.libs.json.{Format, Json}
import uk.gov.ons.addressIndex.model.db.index.{HybridIndex, NationalAddressGazetteer, PostcodeAddressFile}
import uk.gov.ons.addressIndex.crfscala.CrfScala.CrfTokenResult
import scala.util.Try
import Model.HybridResponse

object Model {

  case class HybridResponse(
    uprn: String,
    lpi: Option[Seq[NationalAddressGazetteer]],
    paf: Option[Seq[PostcodeAddressFile]],
    score: Float
  )

  implicit class StringAnyRefAugmenter(map: Map[String, AnyRef]) {
    def getType[T](field: String, default: T): T = map.getOrElse(field, default).asInstanceOf[T]
  }

  implicit object HybridResponse extends HitAs[HybridResponse] {
    import scala.collection.JavaConverters._

    override def as(hit: RichSearchHit): HybridResponse = {
      val map = hit.sourceAsMap

      def getSeqMap[T](fieldName: String)(fn: Seq[Map[String, AnyRef]] => Seq[T]): Option[Seq[T]] = {
        Try {
          fn(
            map(fieldName)
              .asInstanceOf[util.ArrayList[java.util.HashMap[String, AnyRef]]]
              .asScala
              .map(_.asScala.toMap)
          )
        }.toOption
      }
      HybridResponse(
        uprn = map(HybridIndex.Fields.uprn).toString,
        score = hit.score,
        lpi = getSeqMap(HybridIndex.Fields.lpi) { seqMap =>
          seqMap map { nag =>
            NationalAddressGazetteer(
              uprn = nag.getType("uprn", ""),
              postcodeLocator = nag.getType("postcodeLocator", ""),
              addressBasePostal = nag.getType("addressBasePostal", ""),
              latitude = nag.getType("latitude", ""),
              longitude = nag.getType("longitude", ""),
              easting = nag.getType("easting", ""),
              northing = nag.getType("northing", ""),
              organisation = nag.getType("organisation", ""),
              legalName = nag.getType("legalName", ""),
              classificationCode = nag.getType("classificationCode", ""),
              usrn = nag.getType("usrn", ""),
              lpiKey = nag.getType("lpiKey", ""),
              paoText = nag.getType("paoText", ""),
              paoStartNumber = nag.getType("paoStartNumber", ""),
              paoStartSuffix = nag.getType("paoStartSuffix", ""),
              paoEndNumber = nag.getType("paoEndNumber", ""),
              paoEndSuffix = nag.getType("paoEndSuffix", ""),
              saoText = nag.getType("saoText", ""),
              saoStartNumber = nag.getType("saoStartNumber", ""),
              saoStartSuffix = nag.getType("saoStartSuffix", ""),
              saoEndNumber = nag.getType("saoEndNumber", ""),
              saoEndSuffix = nag.getType("saoEndSuffix", ""),
              level = nag.getType("level", ""),
              officialFlag = nag.getType("officialFlag", ""),
              logicalStatus = nag.getType("logicalStatus", ""),
              streetDescriptor = nag.getType("streetDescriptor", ""),
              townName = nag.getType("townName", ""),
              locality = nag.getType("locality", "")
            )
          }
        },
        paf = getSeqMap(HybridIndex.Fields.paf) { seqMap =>
          seqMap map { paf =>
            PostcodeAddressFile(
              recordIdentifier = paf.getType("recordIdentifier", ""),
              changeType = paf.getType("recordIdentifier", ""),
              proOrder = paf.getType("proOrder", ""),
              uprn = paf.getType("uprn", ""),
              udprn = paf.getType("udprn", ""),
              organizationName = paf.getType("organizationName", ""),
              departmentName = paf.getType("departmentName", ""),
              subBuildingName = paf.getType("subBuildingName", ""),
              buildingName = paf.getType("buildingName", ""),
              buildingNumber = paf.getType("buildingNumber", ""),
              dependentThoroughfare = paf.getType("dependentThoroughfare", ""),
              thoroughfare = paf.getType("thoroughfare", ""),
              doubleDependentLocality = paf.getType("doubleDependentLocality", ""),
              dependentLocality = paf.getType("dependentLocality", ""),
              postTown = paf.getType("postTown", ""),
              postcode = paf.getType("postcode", ""),
              postcodeType = paf.getType("postcodeType", ""),
              deliveryPointSuffix = paf.getType("deliveryPointSuffix", ""),
              welshDependentThoroughfare = paf.getType("welshDependentThoroughfare", ""),
              welshThoroughfare = paf.getType("welshThoroughfare", ""),
              welshDoubleDependentLocality = paf.getType("welshDoubleDependentLocality", ""),
              welshDependentLocality = paf.getType("welshDependentLocality", ""),
              welshPostTown = paf.getType("welshPostTown", ""),
              poBoxNumber = paf.getType("poBoxNumber", ""),
              processDate = paf.getType("processDate", ""),
              startDate = paf.getType("startDate", ""),
              endDate = paf.getType("endDate", ""),
              lastUpdateDate = paf.getType("lastUpdateDate", ""),
              entryDate = paf.getType("entryDate", "")
            )
          }
        }
      )
    }
  }
}

/**
  * Container for all responses
  *
  * @param response - results from ES
  * @param status - status from server
  * @param errors - any errors which occurred
  */
case class Container(
  response: Option[Results] = None,
  status: Status,
  errors: Option[Seq[Error]] = None
)

object Container {

  implicit lazy val fmt = Json.format[Container]

  def fromHybridResponse(
    optAddresses: Option[Seq[HybridResponse]],
    tokens: Seq[CrfTokenResult],
    status: Status,
    errors: Option[Seq[Error]] = None,
    limit: Int,
    offset: Int,
    total: Int,
    maxScore: Float
  ): Container = {
    Container(
      response = Some(
        Results(
          tokens = tokens,
          addresses = optAddresses.map(
            _.map { sHybrid =>
              AddressInformation(
                uprn = sHybrid.uprn,
                paf = sHybrid.paf.map(_.map(_.toPAFWithFormat)),
                nag = sHybrid.lpi.map(_.map(_.toNagWithFormat)),
                underlyingScore = sHybrid.score,
                underlyingMaxScore = maxScore
              )
            }
          ),
          limit = limit,
          offset = offset,
          total = total
        )
      ),
      status = status,
      errors = errors
    )
  }
}

case class Results(
  tokens: Seq[CrfTokenResult],
  addresses: Option[Seq[AddressInformation]],
  limit: Int,
  offset: Int,
  total: Int
)

object Results {
  implicit lazy val trFmt: Format[CrfTokenResult] = Json.format[CrfTokenResult]
  implicit lazy val rFmt: Format[Results] = Json.format[Results]
}

case class AddressInformation(
  uprn: String,
  paf: Option[Seq[PAFWithFormat]],
  nag: Option[Seq[NAGWithFormat]],
  underlyingScore: Float,
  underlyingMaxScore: Float
)

object AddressInformation {
  implicit lazy val fmt: Format[AddressInformation] = Json.format[AddressInformation]
}

case class PAFWithFormat(formattedAddress: String, paf: PAF)
object PAFWithFormat {
  implicit lazy val fmt: Format[PAFWithFormat] = Json.format[PAFWithFormat]
}

case class PAF(
  udprn: String,
  organisationName: String,
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
  welshDependentThoroughfare: String,
  welshThoroughfare: String,
  welshDoubleDependentLocality: String,
  welshDependentLocality: String,
  welshPostTown: String,
  poBoxNumber: String,
  startDate: String,
  endDate: String
)

object PAF {
  implicit lazy val fmt: Format[PAF] = Json.format[PAF]
}

case class NAGWithFormat(formattedAddress: String, nag: NAG)
object NAGWithFormat {
  implicit lazy val fmt: Format[NAGWithFormat] = Json.format[NAGWithFormat]
}

case class NAG(
  uprn: String,
  postcodeLocator: String,
  addressBasePostal: String,
  usrn: String,
  lpiKey: String,
  pao: PrimaryAddressableObject,
  sao: SecondaryAddressableObject,
  geo: Geo,
  level: String,
  officialFlag: String,
  logicalStatus: String,
  streetDescriptor: String,
  townName: String,
  locality: String,
  organisation: String,
  legalName: String,
classificationCode: String
)

object NAG {
  implicit lazy val fmt: Format[NAG] = Json.format[NAG]
}

case class PrimaryAddressableObject(
  text: String,
  startNumber: String,
  paoStartSuffix: String,
  paoEndNumber: String,
  paoEndSuffix: String
)

object PrimaryAddressableObject {
  implicit lazy val fmt: Format[PrimaryAddressableObject] = Json.format[PrimaryAddressableObject]
}

case class SecondaryAddressableObject(
  text: String,
  startNumber: String,
  startSuffix: String,
  endNumber: String,
  endSuffix: String
)

object SecondaryAddressableObject {
  implicit lazy val fmt: Format[SecondaryAddressableObject] = Json.format[SecondaryAddressableObject]
}

case class Geo(
  latitude: Double,
  longitude: Double,
  easting: Double,
  northing: Double
)

object Geo {
  implicit lazy val fmt: Format[Geo] = Json.format[Geo]
}

case class Status(
  code: Int,
  message: String
)

object Status {
  implicit lazy val fmt: Format[Status] = Json.format[Status]
}

case class Error(
  code: Int,
  message: String
)

object Error {
  implicit lazy val fmt: Format[Error] = Json.format[Error]
}
