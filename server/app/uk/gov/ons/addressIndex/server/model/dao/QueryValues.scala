package uk.gov.ons.addressIndex.server.model.dao

case class QueryValues(input: Option[String] = None,
                       fallback: Option[Boolean] = None,
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
                       matchThreshold: Option[Float] = None,
                       fromsource: Option[String] = None,
                       highverbose: Option[Boolean] = None,
                       favourpaf: Option[Boolean] = None,
                       favourwelsh: Option[Boolean] = None,
                       addressType: Option[String] = None)
{
  def inputOrDefault: String = this.input.getOrElse("")

  def fallbackOrDefault: Boolean = this.fallback.getOrElse(true)

  def postcodeOrDefault: String = this.postcode.getOrElse("")

  def uprnOrDefault: String = this.uprn.getOrElse("")

  def epochOrDefault: String = this.epoch.getOrElse("")

  def filterOrDefault: String = this.filter.getOrElse("")

  def historicalOrDefault: Boolean = this.historical.getOrElse(false)

  def limitOrDefault: Int = this.limit.getOrElse(0)

  def offsetOrDefault: Int = this.offset.getOrElse(0)

  def startDateOrDefault: String = this.startDate.getOrElse("")

  def endDateOrDefault: String = this.endDate.getOrElse("")

  def verboseOrDefault: Boolean = this.verbose.getOrElse(false)

  def rangeKMOrDefault: String = this.rangeKM.getOrElse("")

  def latitudeOrDefault: String = this.latitude.getOrElse("")

  def longitudeOrDefault: String = this.longitude.getOrElse("")

  def matchThresholdOrDefault: Float = this.matchThreshold.getOrElse(0f)

  def fromSourceOrDefault: String = this.fromsource.getOrElse("all")

  def highVerboseOrDefault: Boolean = this.highverbose.getOrElse(false)

  def favourParOrDefault: Boolean = this.favourpaf.getOrElse(false)

  def favourWelshOrDefault: Boolean = this.favourpaf.getOrElse(false)

  def addressTypeOrDefault: String = this.addressType.getOrElse("")
}
