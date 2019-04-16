package uk.gov.ons.addressIndex.parsers

/**
  * A list of feature that token may take.
  * Those feature are needed to classify the token using machine learning algorithm (Conditional Random Fields or CRF)
  * The `private[parsers]` is needed to prevent calls from outside of the package (thus limiting it to package's
  * classes and tests)
  */
object Features {
  private val vowels = Seq('A', 'E', 'I', 'O', 'U')

  private def isAllDigits(token: String): Boolean = token.replaceAll("\\D", "").length() == token.length

  /**
    * Features whether or not the feature is present in the "business" list
    *
    * @param token token to be analysed
    * @return "business:1.0" if the token is present in the "business" list, "business:0.0" otherwise
    */
  private[parsers] def businessFeature(token: String): String = s"business:${if (Tokens.business.contains(token)) "1.0" else "0.0"}"

  /**
    * Features whether or not the feature is present in the "company" list
    *
    * @param token token to be analysed
    * @return "company:1.0" if the token is present in the "company" list, "company:0.0" otherwise
    */
  private[parsers] def companyFeature(token: String): String = s"company:${if (Tokens.company.contains(token)) "1.0" else "0.0"}"

  /**
    * Features whether or not the feature is present in the "directional" list
    *
    * @param token token to be analysed
    * @return "directional:1.0" if the token is present in the "directional" list, "directional:0.0" otherwise
    */
  private[parsers] def directionalFeature(token: String): String = s"directional:${if (Tokens.directions.contains(token)) "1.0" else "0.0"}"

  /**
    * Features whether or not the feature is present in the "flat" list
    *
    * @param token token to be analysed
    * @return "flat:1.0" if the token is present in the "flat" list, "flat:0.0" otherwise
    */
  private[parsers] def flatFeature(token: String): String = s"flat:${if (Tokens.flat.contains(token)) "1.0" else "0.0"}"

  /**
    * Features whether or not the feature is present in the "locational" list
    *
    * @param token token to be analysed
    * @return "locational:1.0" if the token is present in the "locational" list, "locational:0.0" otherwise
    */
  private[parsers] def locationalFeature(token: String): String = s"locational:${if (Tokens.locational.contains(token)) "1.0" else "0.0"}"

  /**
    * Features whether or not the feature is present in the "ordinal" list
    *
    * @param token token to be analysed
    * @return "ordinal:1.0" if the token is present in the "ordinal" list, "ordinal:0.0" otherwise
    */
  private[parsers] def ordinalFeature(token: String): String = s"ordinal:${if (Tokens.ordinal.contains(token)) "1.0" else "0.0"}"

  /**
    * Features whether or not the feature is present in the "outcode" list
    *
    * @param token token to be analysed
    * @return "outcode:1.0" if the token is present in the "outcode" list, "outcode:0.0" otherwise
    */
  private[parsers] def outCodeFeature(token: String): String = s"outcode:${if (Tokens.outcodes.contains(token)) "1.0" else "0.0"}"

  /**
    * Features whether or not the feature is present in the "posttown" list
    *
    * @param token token to be analysed
    * @return "posttown:1.0" if the token is present in the "posttown" list, "posttown:0.0" otherwise
    */
  private[parsers] def postTownFeature(token: String): String = s"posttown:${if (Tokens.postTown.contains(token)) "1.0" else "0.0"}"

  /**
    * Features whether or not the feature is present in the "residential" list
    *
    * @param token token to be analysed
    * @return "residential:1.0" if the token is present in the "residential" list, "residential:0.0" otherwise
    */
  private[parsers] def residentialFeature(token: String): String = s"residential:${if (Tokens.residential.contains(token)) "1.0" else "0.0"}"

  /**
    * Features whether or not the feature is present in the "road" list
    *
    * @param token token to be analysed
    * @return "road:1.0" if the token is present in the "road" list, "road:0.0" otherwise
    */
  private[parsers] def roadFeature(token: String): String = s"road:${if (Tokens.road.contains(token)) "1.0" else "0.0"}"

  /**
    * Features the presence of digits in a token
    *
    * @param token token to be analysed
    * @return different digits features depending on whether the token is a digit, contains a digit or has no digits in it
    */
  private[parsers] def digitsFeature(token: String): String = token.replaceAll("\\D", "").length() match {
    case digits if digits == token.length => "digits\\:all_digits:1.0"
    case 0 => "digits\\:no_digits:1.0"
    case _ => "digits\\:some_digits:1.0"
  }

  /**
    * Features the presence of a dot at the end of a token
    *
    * @param token token to be analysed
    * @return "endsinpunc:1.0" if the token ends with a dot, "endsinpunc:0.0" otherwise
    */
  private[parsers] def endsInPuncFeature(token: String): String = s"endsinpunc:${if (token.lastOption.contains('.')) "1.0" else "0.0"}"

  /**
    * Features the presence of vowels in a token
    *
    * @param token token to be analysed
    * @return "has.vowels:1.0" if the token has vowels, "has.vowels:0.0" otherwise
    */
  private[parsers] def hasVowelsFeature(token: String): String = s"has.vowels:${if (token.exists(vowels.contains)) "1.0" else "0.0"}"

  /**
    * Features the presence of hyphens in a token
    *
    * @param token token to be analysed
    * @return "hyphenations:x.0" where x is the number of hyphens
    */
  private[parsers] def hyphenationsFeature(token: String): String = s"hyphenations:${token.count(_ == '-')}.0"

  /**
    * Features the length of the token (also whether it's a digit or not)
    *
    * @param token token to be analysed
    * @return "length\\:x\\:y:1.0" where x is 'd' if it's a digit, 'w' otherwise and y is token's length
    */
  private[parsers] def lengthFeature(token: String): String =
    s"length\\:${if (token.nonEmpty && isAllDigits(token)) "d" else "w"}\\:${token.length}:1.0"

  /**
    * Features the word itself (if it's not a digit)
    *
    * @param token token to be analysed
    * @return "word\\:x:1.0" if x is not a digit, "word:0.0" otherwise
    */
  private[parsers] def wordFeature(token: String): String = s"word${if (isAllDigits(token)) ":0.0" else s"\\:$token:1.0"}"
}

