package uk.gov.ons.addressIndex

package object parsers {
  object Implicts {
    implicit class StringUtils(str : String) {

      val DIGITS : List[String] = (0 to 9).toList map(_.toString)

      def allDigits[T](fn : (Boolean => T)) : T = {
        //TODO rename me
        val length1eqTrueIfAllDigits : List[Boolean] = DIGITS.map(str contains _).distinct
        val allDigits : Boolean = length1eqTrueIfAllDigits.length == 1 && length1eqTrueIfAllDigits.head
        fn(allDigits)
      }

      def containsDigits[T](fn : (Boolean => T)) : T = fn(DIGITS exists(str contains _))

      val VOWELS : List[Char] = List('a', 'e', 'i', 'o', 'u')

      def containsVowels[T](fn : (Boolean => T)) : T = fn(VOWELS exists(str contains _))
    }
  }
}
