package uk.gov.ons.addressIndex.crfscala


/**
  * scala wrapper of crfsuite
  *
  * todo describe this more
  */
object CrfScala {

  /**
    * This string is required to have a `\t` at the end for CrfScala to work.
    */
  val arbitraryString: String = "RhysBradbury\t"

  case class CrfParserResult(originalInput: String, crfLabel: String)

  /**
    * todo scaladoc
    */
  trait CrfTokenable {
    def apply(input : String) : Array[String]
    def normalise(tokens : Array[String]): Array[String]
  }

  trait CrfFeaturable

  case class CrfTokenResult(
    value: String,
    label: String
  )

  //todo scaladoc
  trait CrfType[T] {
    def value: T
  }
}