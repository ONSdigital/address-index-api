package uk.gov.ons.addressIndex.crfscala

/**
  * scala wrapper of crfsuite
  *
  * todo describe this more
  */
object CrfScala {

  type Input = String
  type CrfToken = String
  type FeatureName = String
  type CrfJniInput = String
  type FeaturesResult = Map[FeatureName, _]
  type CrfParserResults = Seq[CrfParserResult]

  case class CrfParserResult(originalInput: CrfToken, crfLabel: String)

  case class CrfTokenResult(
    token: CrfToken,
    next: Option[CrfToken] = None,
    previous: Option[CrfToken] = None,
    results: FeaturesResult
  ) {
    def toCrfJniInput(): CrfJniInput = {
      ""
    }
  }

  //todo scaladoc
  trait CrfType[T] {
    def value: T
  }
}