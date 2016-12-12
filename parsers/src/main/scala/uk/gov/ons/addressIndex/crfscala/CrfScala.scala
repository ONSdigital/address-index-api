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
    value: CrfToken,
    label: FeatureName
  )

  //todo scaladoc
  trait CrfType[T] {
    def value: T
  }
}