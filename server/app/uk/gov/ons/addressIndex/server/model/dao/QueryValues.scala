package uk.gov.ons.addressIndex.server.model.dao

case class QueryValues(input: Option[String] = None,
                       fallback: Option[Boolean] = None,
                       postcode: Option[String] = None,
                       streetname: Option[String] = None,
                       townname: Option[String] = None,
                       uprn: Option[String] = None,
                       uprns: Option[List[String]] = None,
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
                       highlight: Option[String] = None,
                       favourpaf: Option[Boolean] = None,
                       favourwelsh: Option[Boolean] = None,
                       addressType: Option[String] = None,
                       includeAuxiliarySearch: Option[Boolean] = None,
                       eboost: Option[Double] = None,
                       nboost: Option[Double] = None,
                       sboost: Option[Double] = None,
                       wboost: Option[Double] = None,
                       groupFullPostcodes: Option[String] = None,
                       timeout: Option[Int] = None,
                       pafDefault: Option[Boolean] = None
                      )
{
  def inputOrDefault: String = this.input.getOrElse("")

  def fallbackOrDefault: Boolean = this.fallback.getOrElse(false)

  def postcodeOrDefault: String = this.postcode.getOrElse("")

  def streetnameOrDefault: String = this.streetname.getOrElse("")

  def townnameOrDefault: String = this.townname.getOrElse("")

  def uprnOrDefault: String = this.uprn.getOrElse("")

  def epochOrDefault: String = this.epoch.getOrElse("")

  def filterOrDefault: String = this.filter.getOrElse("")

  def historicalOrDefault: Boolean = this.historical.getOrElse(false)

  def limitOrDefault: Int = this.limit.getOrElse(0)

  def offsetOrDefault: Int = this.offset.getOrElse(0)

  def timeoutOrDefault: Int = this.timeout.getOrElse(0)

  def startDateOrDefault: String = this.startDate.getOrElse("")

  def endDateOrDefault: String = this.endDate.getOrElse("")

  def verboseOrDefault: Boolean = this.verbose.getOrElse(false)

  def rangeKMOrDefault: String = this.rangeKM.getOrElse("")

  def latitudeOrDefault: String = this.latitude.getOrElse("")

  def longitudeOrDefault: String = this.longitude.getOrElse("")

  def matchThresholdOrDefault: Float = this.matchThreshold.getOrElse(0f)

  def eboostOrDefault: Double = this.eboost.getOrElse(1.0D)

  def nboostOrDefault: Double = this.nboost.getOrElse(1.0D)

  def sboostOrDefault: Double = this.sboost.getOrElse(1.0D)

  def wboostOrDefault: Double = this.wboost.getOrElse(1.0D)

  def highlightOrDefault: String = this.highlight.getOrElse("on")

  def favourPafOrDefault: Boolean = this.favourpaf.getOrElse(true)

  def favourWelshOrDefault: Boolean = this.favourwelsh.getOrElse(false)

  def addressTypeOrDefault: String = this.addressType.getOrElse("")

  def includeAuxiliarySearchOrDefault: Boolean = this.includeAuxiliarySearch.getOrElse(false)

  def groupFullPostcodesOrDefault: String = this.groupFullPostcodes.getOrElse("no")

  def pafDefaultOrDefault: Boolean = this.pafDefault.getOrElse(false)
}
