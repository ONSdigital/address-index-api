package uk.gov.ons.addressIndex

import com.google.common.base.CharMatcher

package object parsers {
  object Implicits {
    implicit class StringUtils(str : String) {

      def digitCount() : Int = CharMatcher.DIGIT.countIn(str)

      def containsDigitsBase() : Boolean = digitCount > 0

      def allDigits[T](fn : (Boolean => T)) : T = fn(digitCount == str.length)

      def containsDigits[T](fn : (Boolean => T)) : T = fn(containsDigitsBase)

      val VOWELS : List[Char] = List('a', 'e', 'i', 'o', 'u')

      def containsVowels[T](fn : (Boolean => T)) : T = fn(VOWELS exists(str contains _))
    }
  }
}
