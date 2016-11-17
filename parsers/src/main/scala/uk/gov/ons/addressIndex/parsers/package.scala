package uk.gov.ons.addressIndex

import com.google.common.base.CharMatcher
//TODO scaladoc
package object parsers {
  object Implicits {
    implicit class StringUtils(str : String) {
      def digitCount() : Int = CharMatcher.DIGIT.countIn(str)

      def containsDigitsBase() : Boolean = digitCount > 0

      def allDigits[T](fn : (Boolean => T)) : T = fn(digitCount == str.length)

      def containsDigits[T](fn : (Boolean => T)) : T = fn(containsDigitsBase)

      def containsVowels[T](fn : (Boolean => T)) : T = {
        fn(
          CharMatcher.is('a')
            .or(CharMatcher.is('e'))
            .or(CharMatcher.is('i'))
            .or(CharMatcher.is('o'))
            .or(CharMatcher.is('u'))
            .countIn(str) > 0
        )
      }
    }
  }
}