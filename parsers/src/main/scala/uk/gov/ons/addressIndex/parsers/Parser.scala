package uk.gov.ons.addressIndex.parsers

import uk.gov.ons.addressIndex.crfscala.CrfScalaJni

import scala.annotation.tailrec

/**
  * Main entry-point to parse input from user into tokens
  * @param tagger injected instance of a tagger (main instance should use native code)
  */
class Parser(val tagger: CrfScalaJni) {

  /**
    * Parse user's input into labelised tokens
    * @param input user's input
    * @return map with label -> tokens relationship, where tokens is one or more tokens
    *         separated by space
    */
  def parse(input: String): Map[String, String] = {
    val tokens: List[String] = Tokens.preTokenize(input)
    val crfInput: String = Parser.constructCrfInput(tokens)
    val taggerResult: String = tagger.tag(crfInput)
    val labeledTokens: Map[String, String] = Parser.parseTaggerResult(taggerResult, tokens)
    val postTokenizedTokens: Map[String, String] = Tokens.postTokenize(labeledTokens)

    postTokenizedTokens
  }

}

/**
  * Static, side-effect free methods are isolated here.
  * The `private[parsers]` bit is to say that those methods
  * are private, but we need to be able to unit-test them separately.
  * Those that are just `private`, are tested through other methods
  * TODO:
  * - replace prepend with something more describing (like "features")
  * - bug with previous/rawstart in case of 3 tokens
  * - maybe remove the sort step as it's no longer (allegedly) useful
  */
object Parser {

  private val tokensSeparator = "\t"
  private val linesSeparator = "\n"
  private val prepend = s"${tokensSeparator}RhysBradbury$tokensSeparator"

  /**
    * Transforms tokens into CRF input (that will be sent to the tagger)
    * @param tokens tokens to be transformed
    * @return string ready to be sent to the CRF model
    */
  private[parsers] def constructCrfInput(tokens: List[String]): String = {
    val crfLinesData: List[CrfLineData] = tokensToCrfLinesData(tokens)

    val crfInputLines: List[String] = crfLinesData.map(constructCrfInputLine)

    val crfInputLinesWithContext: List[String] = appendSpecialDataToCrfLines(crfInputLines)

    // This step may not be required anymore (it is only here so that the output is exactly the same as before)
    val crfInputSortedLines: List[String] = sortCrfInputLines(crfInputLinesWithContext)

    val crfInputLinesWithPrepend: List[String] = crfInputSortedLines.map(prepend + _)

    crfInputLinesWithPrepend.mkString(linesSeparator) + linesSeparator
  }

  /**
    * Small data-holding object that could be replaced by a Tuple3, but this way
    * it's more readable
    * @param previous optional previous token
    * @param current current token
    * @param next optional next token
    */
  private[parsers] case class CrfLineData(previous: Option[String], current: String, next: Option[String])

  /**
    * We need to transform a list of tokens into a list of CrfLineData structures
    * so that each element contains in addition to current token, also previous one
    * and the next one.
    * We will do it in two stages, fist we take care about the first element and
    * then a recursive function `tokensTailToCrfLinesData` will map the remaining ones
    *
    * Example:
    * tokens: List("31", "EXETER", "CLOSE")
    * result: List(
    * CrfLineData(None, "31", Some("EXETER")),
    * CrfLineData(Some("31"), "EXETER", Some("CLOSE")),
    * CrfLineData(Some("EXETER"), "CLOSE", None)
    * )
    *
    * @param tokens list of tokens from the input
    * @return list of `CrfLineData`s in which each element contains previous, current
    *         and the next token
    */
  private[parsers] def tokensToCrfLinesData(tokens: List[String]): List[CrfLineData] = tokens match {
    case Nil => Nil
    case first :: Nil => List(CrfLineData(None, first, None))
    case first :: second :: _ => tokensTailToCrfLinesData(tokens, List(CrfLineData(None, first, Some(second))))
  }

  /**
    * Tail recursive (won't cause stack-overflows) function that converts all tokens
    * in a list (except for the first one) into a data structure that has previous,
    * current and the next token
    *
    * @param tokens a list of tokens from the input (including the first one, we need it
    * to set the `previous` token for the second token)
    * @param result result storage (will be returned in the end). It should already
    * (on the function call) contain the data structure for the first token
    * @return list of `CrfLineData`s in which each element contains previous, current
    *         and the next token
    */
  @tailrec
  private def tokensTailToCrfLinesData(tokens: List[String], result: List[CrfLineData]): List[CrfLineData] = tokens match {
    case previous :: current :: next :: tail =>
      tokensTailToCrfLinesData(current :: next :: tail, result :+ CrfLineData(Some(previous), current, Some(next)))
    case previous :: current :: Nil => result :+ CrfLineData(Some(previous), current, None)
    case _ => throw new IllegalArgumentException(
      s"`tokensTailToCrfLinesData()` method is called with illegal parameters (`tokens` should have at least 2 elements): $tokens"
    )
  }

  /**
    * Constructs the full string (with the exception for a few features like "singleton"
    * or "rawstring.start" which will be added in the `constructCrfInput` step
    *
    * @param lineData data about the line containing optional previous/next tokens
    * @return features data not only about the current token, but also about its neighboutrs
    *         example:
    *         features\tbusiness:0.0\tcompany:0.0\tdigits\\:no_digits:1.0\tdirectional:0.0\tendsinpunc:0.0
    *         \tflat:0.0\thas.vowels:1.0\thyphenations:0.0\tlength\\:w\\:6:1.0\tlocational:0.0
    *         \tordinal:0.0\toutcode:0.0\tposttown:0.0\tresidential:0.0\troad:0.0\tword\\:CURLEW:1.0
    *         \tnext\\:business:0.0\tnext\\:company:0.0\tnext\\:digits\\:no_digits:1.0\tnext\\:directional:0.0
    *         \tnext\\:endsinpunc:0.0\tnext\\:flat:0.0\tnext\\:has.vowels:1.0\tnext\\:hyphenations:0.0
    *         \tnext\\:length\\:w\\:3:1.0\tnext\\:locational:0.0\tnext\\:ordinal:0.0\tnext\\:outcode:0.0
    *         \tnext\\:posttown:0.0\tnext\\:residential:0.0\tnext\\:road:1.0\tnext\\:word\\:WAY:1.0
    *         \tprevious\\:business:0.0\tprevious\\:company:0.0\tprevious\\:digits\\:all_digits:1.0
    *         \tprevious\\:directional:0.0\tprevious\\:endsinpunc:0.0\tprevious\\:flat:0.0\tprevious\\:has.vowels:0.0
    *         \tprevious\\:hyphenations:0.0\tprevious\\:length\\:d\\:2:1.0\tprevious\\:locational:0.0
    *         \tprevious\\:ordinal:0.0\tprevious\\:outcode:0.0\tprevious\\:posttown:0.0\tprevious\\:residential:0.0
    *         \tprevious\\:road:0.0\tprevious\\:word:0.0"
    */
  private[parsers] def constructCrfInputLine(lineData: CrfLineData): String = {
    val current = tokenToFeatures(lineData.current)
    val next = lineData.next.map(token => tokensSeparator + tokenToFeatures(token, "next\\:")).getOrElse("")
    val previous = lineData.previous.map(token => tokensSeparator + tokenToFeatures(token, "previous\\:")).getOrElse("")

    current + next + previous
  }

  /**
    * Constructs Crf string (string that contains all the features of the token)
    *
    * @param token token to be analysed
    * @param prefix optional prefix for each feature (used for "previous" and "next" features)
    * @return string containing all the features of a token separated by tab, example:
    *         "business:0.0\tcompany:0.0\tdigits\\:all_digits:1.0\tdirectional:0.0\tendsinpunc:0.0
    *         \tflat:0.0\thas.vowels:0.0\thyphenations:0.0\tlength\\:d\\:2:1.0\tlocational:0.0
    *         \tordinal:0.0\toutcode:0.0\tposttown:0.0\tresidential:0.0\troad:0.0\tword:0.0"
    *         for a token "31"
    */
  private[parsers] def tokenToFeatures(token: String, prefix: String = ""): String = {
    val features = Seq(
      Features.businessFeature(token),
      Features.companyFeature(token),
      Features.digitsFeature(token),
      Features.directionalFeature(token),
      Features.endsInPuncFeature(token),
      Features.flatFeature(token),
      Features.hasVowelsFeature(token),
      Features.hyphenationsFeature(token),
      Features.lengthFeature(token),
      Features.locationalFeature(token),
      Features.ordinalFeature(token),
      Features.outCodeFeature(token),
      Features.postTownFeature(token),
      Features.residentialFeature(token),
      Features.roadFeature(token),
      Features.wordFeature(token)
    )

    features.map(prefix + _).mkString("\t")
  }

  /**
    * First two and last two tokens require special features to be appended to the feature-list
    *
    * @param crfInputLines feature lines for all input tokens
    * @return same crfInputLines, but first two and last two feature lines will have additional
    *         features
    */
  private def appendSpecialDataToCrfLines(crfInputLines: List[String]): List[String] = crfInputLines match {
    case Nil => Nil

    // one token
    case singleton :: Nil => List(singleton + tokensSeparator + "singleton:1.0")

    // two tokens
    case first :: last :: Nil => List(
      first + tokensSeparator + "rawstring.start:1.0" + tokensSeparator + "next\\:rawstring.end:1.0",
      last + tokensSeparator + "rawstring.end:1.0" + tokensSeparator + "previous\\:rawstring.start:1.0"
    )

    // three tokens
    case first :: second :: last :: Nil => List(
      first + tokensSeparator + "rawstring.start:1.0",
      second + tokensSeparator + "next\\:rawstring.end:1.0", // + tokensSeparator + "previous\\:rawstring.start:1.0", // this is a bug in previous version, uncomment to fix
      last + tokensSeparator + "rawstring.end:1.0"
    )

    // four tokens
    case first :: second :: third :: last :: Nil => List(
      first + tokensSeparator + "rawstring.start:1.0",
      second + tokensSeparator + "previous\\:rawstring.start:1.0",
      third + tokensSeparator + "next\\:rawstring.end:1.0",
      last + tokensSeparator + "rawstring.end:1.0"
    )

    // five and more tokens
    case first :: second :: (middleTokens :+ penultimate :+ last) =>
      (first + tokensSeparator + "rawstring.start:1.0") :: (second + tokensSeparator + "previous\\:rawstring.start:1.0") ::
        (middleTokens :+ (penultimate + tokensSeparator + "next\\:rawstring.end:1.0") :+ last + tokensSeparator + "rawstring.end:1.0")
  }

  /**
    * Sorts features in the CRF input lines.
    * This may be removed as it is here only to be compatible with the previous version of parser
    *
    * @param crfInputLines feature lines for all input tokens
    * @return same feature lines, but now features are sorted alphabetically
    */
  private def sortCrfInputLines(crfInputLines: List[String]): List[String] = {
    crfInputLines.map { line =>
      val sortedFeatures = line.split(tokensSeparator).sorted
      sortedFeatures.mkString(tokensSeparator)
    }
  }

  /**
    * Transforms result from the CRFTagger (the C code that uses machine learning) into
    * manageable labeled tokens.
    *
    * Example of a result from tagger:
    * BuildingNumber: 0.999236
    * StreetName: 1.000000
    * StreetName: 0.999999
    * TownName: 0.999996
    * Postcode: 1.000000
    * Postcode: 1.000000
    *
    * @param taggerResult result from the native code
    * @param initialTokens initial tokens that will be mapped to the result labels
    * @return map containing label -> token. If there were more than one token per label,
    *         then the map will contain label -> token1 token2  (tokens are separated by space)
    */
  private[parsers] def parseTaggerResult(taggerResult: String, initialTokens: List[String]): Map[String, String] = {

    val labels: Array[String] = taggerResult.split(linesSeparator).filter(_.nonEmpty).map{ label =>
      label.split(':').headOption.getOrElse("NO LABEL") // the getOrElse part should not happen, but exception would be too restrictive
    }

    val groupedTokens: Map[String, Array[String]] =
      labels
        .zip(initialTokens) // now we have Array( (label1, token1.1), (label1, token1.2), (label2, token2) )
        .groupBy { case (label, _) => label } // now we have Array( (label1, Array((label1, token1.1), (label1, token1.2))), (label2, Array((label2, token2))) )
        .mapValues(_.map { case (_, token) => token }) // finally we have Array( (label1, Array(token1.1, token1.2)), (label2, Array(token2)) )

    groupedTokens.map { case (label, tokens) =>
      label -> tokens.mkString(" ")
    }

  }

}

