package uk.gov.ons.addressIndex.crfscala

/**
  * A JNI interface
  */
trait CrfScalaJni {
  /**
    * Tag an IWA string to a string which contains the results.
    *
    * @param items IWA string
    * @return model results
    */
  def tag(items: String): String

  /**
    * Load the .crfsuite model
    *
    * @param modelPath the file path to model.
    * @return success
    */
  def loadModel(modelPath: String): Integer

  /**
    * Unload the .crfsuite model file
    *
    * @return
    */
  def unloadModel(): Void
}

class CrfScalaJniImpl extends CrfScalaJni {
  /**
    * Load the .crfsuite model
    *
    * @param modelPath the file path to model.
    * @return success
    */
  @native def loadModel(modelPath: String): Integer

  /**
    * Tag an IWA string to a string which contains the results.
    *
    * @param items IWA string
    * @return model results
    */
  @native def tag(items: String): String

  /**
    * Unload the .crfsuite model file
    *
    * @return
    */
  @native def unloadModel(): Void
}
