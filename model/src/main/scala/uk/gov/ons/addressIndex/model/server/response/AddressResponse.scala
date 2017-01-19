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
    paf: Option[Seq[PostcodeAddressFile]]
  )

  implicit class StringAnyRefAugmenter(map: Map[String, AnyRef]) {
    def getType[T](field: String, default: T): T = map.getOrElse(field, default).asInstanceOf[T]
  }

  implicit object HybridResponse extends HitAs[HybridResponse] {
    import scala.collection.JavaConverters._

    implicit lazy val fmt = Json.format[HybridResponse]

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
              locality = nag.getType("locality", ""),
              score = nag.getType("score", 0f)
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
              entryDate = paf.getType("entryDate", ""),
              score = paf.getType("score", 0f)
            )
          }
        }
      )
    }
  }
}

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
    errors: Option[Seq[Error]] = None
  ): Container = {



    optAddresses.getOrElse(Seq.empty).map { sHybrid =>

      AddressInformation(
        uprn = sHybrid.uprn,
        paf = None,
        nag = sHybrid.lpi.map(x => x),
        underlyingScore = 0f,
        underlyingMaxScore = 0f
      )

    }

    Container(
      response = Some(
        Results(
          tokens = tokens,
          addresses = None,
          limit = 1,
          offset = 1,
          total = 1
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
  implicit lazy val tokenResultFmt = Json.format[CrfTokenResult]
  implicit lazy val addressBySearchResponseFormat = Json.format[Results]
}

case class AddressInformation(
  uprn: String,
  paf: Option[Seq[PAF]],
  nag: Option[Seq[NAG]],
  underlyingScore: Float,
  underlyingMaxScore: Float
)

object AddressInformation {
  implicit lazy val addressResponseAddressFormat: Format[AddressInformation] = Json.format[AddressInformation]
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
  endDate: String,
  formattedAddress: String
)

object PAF {
  implicit lazy val fmt = Json.format[PAF]
}

case class NAG(
  uprn: String,
  postcodeLocator: String,
  addressBasePostal: String,
  usrn: String,
  lpiKey: String,
  pao: PAO,
  sao: SAO,
  geo: GEO,
  level: String,
  officialFlag: String,
  logicalStatus: String,
  streetDescriptor: String,
  townName: String,
  locality: String,
  organisation: String,
  legalName: String,
  classificationCode: String,
  formattedAddress: String
)

object NAG {
  implicit lazy val fmt = Json.format[NAG]
}

case class PAO(
  text: String,
  startNumber: String,
  paoStartSuffix: String,
  paoEndNumber: String,
  paoEndSuffix: String
)

object PAO {
  implicit lazy val fmt = Json.format[PAO]
}

case class SAO(
  text: String,
  startNumber: String,
  startSuffix: String,
  endNumber: String,
  endSuffix: String
)

object SAO {
  implicit lazy val fmt = Json.format[SAO]
}

case class GEO(
  latitude: Double,
  longitude: Double,
  easting: Int,
  northing: Int
)

object GEO {
  implicit lazy val fmt = Json.format[GEO]
}

case class Status(
  code: Int,
  message: String
)

object Status {
  implicit lazy val fmt = Json.format[Status]
}

case class Error(
  code: Int,
  message: String
)

object Error {
  implicit lazy val fmt = Json.format[Error]
}
