package uk.gov.ons.addressIndex.crfscala

/**
  * scala wrapper of crfsuite
  *
  * todo describe this more
  */
object CrfScala {

  type Input = String
  type CrfToken = String
  type CrfTokens = Array[CrfToken]
  type FeatureName = String
  type CrfJniInput = String
  type FeaturesResult = Map[FeatureName, _]
  type CrfParserResults = Seq[CrfParserResult]

  case class CrfParserResult(originalInput: CrfToken, crfLabel: String)

  /**
    * todo scaladoc
    */
  trait CrfTokenable {
    def apply(input : CrfToken) : CrfTokens
    def normalise(tokens : CrfTokens): CrfTokens
  }

  trait CrfFeaturable

  case class CrfTokenResult(
    token: CrfToken,
    next: Option[CrfToken] = None,
    previous: Option[CrfToken] = None,
    results: FeaturesResult
  ) {
    def toCrfJniInput(): CrfJniInput = {
      "" //TODO
    }
  }

  //todo scaladoc
  trait CrfType[T] {
    def value: T
  }
}