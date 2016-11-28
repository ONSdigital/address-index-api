package uk.gov.ons.addressIndex.crfscala.jni

trait CrfScalaJni {
  def tag(input : String) : String
  def modelLabels() : Array[String]
}

class CrfScalaJniImpl extends CrfScalaJni {
  @native def tag(input : String) : String
  @native def modelLabels() : Array[String]
}