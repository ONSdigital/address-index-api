package uk.gov.ons.addressIndex.server.modules

import com.sksamuel.elastic4s.SearchDefinition
import uk.gov.ons.addressIndex.model.db.index.HybridIndex
import uk.gov.ons.addressIndex.model.{AddressScheme, BritishStandard7666, PostcodeAddressFile}

object ElasticDsl {
  case class Pagination(offset: Int, limit: Int)
  implicit class AutoPaginate(searchDefinition: SearchDefinition) {
    def paginate(implicit p: Pagination): SearchDefinition = {
      //searchDefinition.start(p.offset).limit(p.limit)
      searchDefinition start p.offset limit p.limit
    }
  }

  implicit class AutoSource(searchDefinition: SearchDefinition) {
    def format(implicit optFmt: Option[AddressScheme]) = {
      optFmt map {
        case _: PostcodeAddressFile => searchDefinition sourceExclude HybridIndex.Fields.lpi
        case _: BritishStandard7666 => searchDefinition sourceExclude HybridIndex.Fields.paf
      } getOrElse searchDefinition
    }
  }
}
