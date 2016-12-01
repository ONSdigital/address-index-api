package uk.gov.ons.addressIndex.crfscala

import uk.gov.ons.addressIndex.crfscala.CrfScala._
import uk.gov.ons.addressIndex.crfscala.jni.{CrfScalaJni, CrfScalaJniImpl}

//TODO scaladoc
trait CrfParser {
  //TODO scaladoc
  def parse(i: Input, fas: CrfFeatures, tokenable: CrfTokenable): CrfParserResults = {
    val tokens = tokenable(i)
    val preprocessedTokens = tokenable normalise tokens
//    val x = preprocessedTokens map fas.analyse
    val crfJniInput = ""
    val tokenResults = new CrfScalaJniImpl tag crfJniInput split CrfScalaJni.newLine
    tokenResults map { tr => CrfParserResult(tr, tr)}
  }
}
