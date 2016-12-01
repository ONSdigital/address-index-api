package uk.gov.ons.addressIndex.crfscala

import uk.gov.ons.addressIndex.crfscala.CrfScala._
import uk.gov.ons.addressIndex.crfscala.jni.{CrfScalaJni, CrfScalaJniImpl}
import uk.gov.ons.addressIndex.parsers.Tokens

//TODO scaladoc
trait CrfParser {
  //TODO scaladoc
  def parse(i : Input, fas : CrfFeatures): CrfParserResults = {
    val tokens = Tokens(i)//TODO INTERDEPENDENCY!!! BAD MOVE TO CRF LIB.
    val preprocessedTokens = Tokens normalise tokens

    //TODO
//    val x = preprocessedTokens map fas.analyse
    val crfJniInput = ""
    val tokenResults = new CrfScalaJniImpl tag crfJniInput split CrfScalaJni.newLine
    tokenResults map { tr => CrfParserResult(tr, tr)}
  }
}
