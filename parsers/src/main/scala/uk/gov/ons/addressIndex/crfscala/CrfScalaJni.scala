package uk.gov.ons.addressIndex.crfscala

//todo scaladoc
trait CrfScalaJni {
  /**
    *
    * @param modelPath
    * @param items
    * @return
    */
  def tag(modelPath: String, items : String) : String
}

class CrfScalaJniImpl extends CrfScalaJni {
  /**
    *
    * @param modelPath
    * @param items
    * @return
    */
  @native def tag(modelPath: String, items : String) : String
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