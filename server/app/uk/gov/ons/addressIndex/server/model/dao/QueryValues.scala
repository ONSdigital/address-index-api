package uk.gov.ons.addressIndex.server.model.dao

case class QueryValues(input: Option[String] = None,
                       postcode: Option[String] = None,
                       uprn: Option[String] = None,
                       epoch: Option[String] = None,
                       filter: Option[String] = None,
                       historical: Option[Boolean] = None,
                       limit: Option[Int] = None,
                       offset: Option[Int] = None,
                       startDate: Option[String] = None,
                       endDate: Option[String] = None,
                       verbose: Option[Boolean] = None,
                       rangeKM: Option[String] = None,
                       latitude: Option[String] = None,
                       longitude: Option[String] = None,
                       matchThreshold: Option[Float] = None) {
  def getInput(): String = this.input.getOrElse("")
  def getFilter(): String = this.filter.getOrElse("")
}
