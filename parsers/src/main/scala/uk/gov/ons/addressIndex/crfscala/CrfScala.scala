package uk.gov.ons.addressIndex.crfscala


/**
  * scala wrapper of crfsuite
  *
  */
object CrfScala {

  /**
    * This string is required to have a `\t` at the end for CrfScala to work.
    */
  val arbitraryString: String = "RhysBradbury\t"

  case class CrfParserResult(originalInput: String, crfLabel: String)

  /**
    * A description of how to transform a String to tokens.
    */
  trait CrfTokenable {
    def apply(input : String) : Array[String]
    def normalise(tokens : Array[String]): Array[String]
  }

  trait CrfFeaturable

  /**
    * A DTO for the result of one token being parsed and labeled.
    * @param value the original token value.
    * @param label the label the CRF model deduced.
    */
  case class CrfTokenResult(
    value: String,
    label: String
  )

  /**
    * A type class which can be used to extend the IWA string builder.
    * @tparam T the underlying type of the value.
    */
  trait CrfType[T] {
    def value: T
  }
}