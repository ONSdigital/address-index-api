package uk.gov.ons.addressIndex

import com.google.common.base.CharMatcher
//TODO scaladoc
package object parsers {
  object Implicits {
    implicit class StringUtils(str : String) {
      def digitCount() : Int = CharMatcher.DIGIT.countIn(str)

      def containsDigitsBase() : Boolean = digitCount > 0

      def allDigits[T](fn : (Boolean => T)) : T = {
        val isAllDigits = if(str.length == 0) {
          false
        } else {
          digitCount == str.length
        }
        fn(isAllDigits)
      }

      def containsDigits[T](fn : (Boolean => T)) : T = fn(containsDigitsBase)

      def containsVowels[T](fn : (Boolean => T)) : T = {
        fn(
          CharMatcher.is('a')
            .or(CharMatcher.is('e'))
            .or(CharMatcher.is('i'))
            .or(CharMatcher.is('o'))
            .or(CharMatcher.is('u'))
            .countIn(str.toLowerCase) > 0
        )
      }

      def toOption(): Option[String] = {
        if (str.isEmpty) None else Some(str)
      }
    }
  }
}