package uk.gov.ons.addressIndex.server.modules

import com.google.inject.{ImplementedBy, Inject, Singleton}
import com.sksamuel.elastic4s.ElasticDsl._
import uk.gov.ons.addressIndex.server.model.dao.ElasticClientProvider

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.io.Source

@ImplementedBy(classOf[AddressIndexVersionModule])
trait VersionModule {
  def apiVersion: String
  def dataVersion: String
}

@Singleton
class AddressIndexVersionModule @Inject()(
  configProvider: AddressIndexConfigModule,
  elasticClientProvider: ElasticClientProvider
) extends VersionModule{

  lazy val apiVersion = {
    val filename = "version.app"
    val path = configProvider.config.pathToResources + filename
    val currentDirectory = new java.io.File(".").getCanonicalPath

    // `Source.fromFile` needs an absolute path to the file, and current directory depends on where sbt was lauched
    // `getResource` may return null, that's why we wrap it into an `Option`
    val resource = Option(getClass.getResource(path)).map(Source.fromURL).getOrElse(Source.fromFile(currentDirectory + path))

    resource.getLines().mkString("")
  }

  lazy val dataVersion = {
    val requestForIdexes = elasticClientProvider.client.execute {
      indexStats("*")
    }

    // yes, it is blocking, but it only does this request once and there is also timeout in case it goes wrong
    val indexes: List[String] = Await.result(requestForIdexes, 10 seconds).indexNames.toList

    println(indexes)
    // the format of index should be following s"${baseIndexName}_${epoch}_${date}_${System.currentTimeMillis()}"
    // and the important information for us is the epoch
    val sortedIndexes = indexes
      .map(removeBaseIndexName)
      .map(removeLetters)
      .filter(_.length >= 2)
      .sorted
    sortedIndexes.lastOption.map(_.substring(0, 2)).getOrElse("develop")
  }

  private def removeBaseIndexName(index: String): String = index.substring(index.indexOf('_') + 1)

  private def removeLetters(index: String): String = index.filter(_.isDigit)

}
