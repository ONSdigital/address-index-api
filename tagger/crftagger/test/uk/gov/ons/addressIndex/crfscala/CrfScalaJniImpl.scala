package uk.gov.ons.addressIndex.crfscala

class CrfScalaJniImpl {
  @native def loadModel(modelPath : String) : Integer
  @native def tag(items : String) : String
  @native def unloadModel() : Void
}
