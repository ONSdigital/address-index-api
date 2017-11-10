package uk.gov.ons.addressIndex.server.modules

import java.util

//import com.carrotsearch.hppc.cursors.{ObjectCursor, ObjectObjectCursor}
import com.google.inject.{ImplementedBy, Inject, Singleton}
import com.sksamuel.elastic4s.http.index.alias.IndexAliases
import com.sksamuel.elastic4s.http.update.RequestFailure
//import com.sksamuel.elastic4s.ElasticDsl._
//import org.elasticsearch.action.admin.indices.alias.get.GetAliasesResponse
//import org.elasticsearch.cluster.metadata.AliasMetaData
//import org.elasticsearch.common.collect.ImmutableOpenMap
import com.sksamuel.elastic4s.http.ElasticDsl._
import uk.gov.ons.addressIndex.server.model.dao.ElasticClientProvider

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.io.Source
import collection.JavaConverters._
import play.api.Logger

@ImplementedBy(classOf[AddressIndexVersionModule])
trait VersionModule {
  def apiVersion: String
  def dataVersion: String
}

@Singleton
class AddressIndexVersionModule @Inject()(
  configProvider: ConfigModule,
  elasticClientProvider: ElasticClientProvider
) extends VersionModule{

  private val logger = Logger("address-index:VersionModule")

  lazy val apiVersion: String = {
    val filename = "version.app"
    val path = configProvider.config.pathToResources + filename
    val currentDirectory = new java.io.File(".").getCanonicalPath

    // `Source.fromFile` needs an absolute path to the file, and current directory depends on where sbt was lauched
    // `getResource` may return null, that's why we wrap it into an `Option`
    val resource =
    Option(getClass.getResource(path))
      .map(Source.fromURL)
      .getOrElse(Source.fromFile(currentDirectory + path))

    resource.getLines().mkString("")
  }

  lazy val dataVersion: String = {

    val alias = configProvider.config.elasticSearch.indexes.hybridIndex
    val requestForIdexes = elasticClientProvider.client.execute {
      getAlias(alias)
    }

    // yes, it is blocking, but it only does this request once and there is also timeout in case it goes wrong
    val indexes: Either[RequestFailure, IndexAliases] = Await.result(requestForIdexes, 10 seconds)
    val indexOpt: Option[IndexAliases] = indexes.right.toOption
  //  val index: String = indexOpt.getOrElse("").toString()

    val index: Option[String] = Option(indexes.right.get.mappings.toMap.keys.toString())

    logger.info("index name = " + index.getOrElse(""))


    index.map(removeBaseIndexName)
      .map(removeLetters)
      .filter(_.length >= 2) // epoch number should contain at least 2 numbers
      .map(_.substring(0, 2))
      .getOrElse("develop")

  }

  private def removeBaseIndexName(index: String): String = index.substring(index.indexOf('_') + 1).takeWhile(_ != '_')

  private def removeLetters(index: String): String = index.filter(_.isDigit)

}
