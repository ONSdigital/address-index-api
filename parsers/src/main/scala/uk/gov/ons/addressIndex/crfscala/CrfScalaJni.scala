package uk.gov.ons.addressIndex.crfscala

//todo scaladoc
trait CrfScalaJni {
  /**
    *
    * @param items
    * @return
    */
  def tag(items: String): String
  def loadModel(modelPath: String): Integer
  def unloadModel(): Void
}

class CrfScalaJniImpl extends CrfScalaJni {
  /**
    *
    * @param modelPath
    * @return
    */
  @native def loadModel(modelPath: String): Integer
  @native def tag(items: String): String
  @native def unloadModel(): Void
}

object CrfScalaJni {
  val tab = "\t"
  val newLine = "\n"
  val lineStart = tab
  val delimiter = tab
  val lineEnd = newLine
  val previous = "previous\\:"
  val next = "next\\:"
}