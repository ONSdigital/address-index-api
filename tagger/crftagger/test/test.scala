import scala.io.Source
import java.util.concurrent.atomic.AtomicInteger

import uk.gov.ons.addressIndex.crfscala._

object Test extends App {
    val pwd = System.getProperty("user.dir");

    System.load(s"$pwd/../libcrftagger-osx.so")

    val inputPath = "./test.txt"
    val modelPath = "./address.crfsuite"

    val items = Source.fromFile(inputPath).mkString

    val tagger = new CrfScalaJniImpl
    val t = new AtomicInteger

    tagger.loadModel(modelPath)

    for (i <- 1 to 1000) {
        val thread = new Thread {
            override def run {
              val tags = tagger.tag(items)

              t.getAndIncrement()

              println(Thread.currentThread().getId())
              println(tags)
            }
        }
        
        thread.start
    }

    Thread.sleep(100)

    tagger.unloadModel()
}
