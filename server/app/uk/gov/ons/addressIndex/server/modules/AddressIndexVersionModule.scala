package uk.gov.ons.addressIndex.server.modules

import com.google.inject.{Inject, Singleton}
import com.sksamuel.elastic4s.http.ElasticDsl._
import com.sksamuel.elastic4s.http.index.alias.IndexAliases
import com.sksamuel.elastic4s.http.{HttpClient, RequestFailure, RequestSuccess}
import uk.gov.ons.addressIndex.server.model.dao.ElasticClientProvider
import uk.gov.ons.addressIndex.server.utils.GenericLogger

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.io.Source
import scala.language.postfixOps
import scala.util.Try

@Singleton
class AddressIndexVersionModule @Inject()
(configProvider: ConfigModule, elasticClientProvider: ElasticClientProvider) extends VersionModule {

  private val logger = GenericLogger("address-index:VersionModule")
  private val gcp : Boolean = Try(configProvider.config.elasticSearch.gcp.toBoolean).getOrElse(false)
  val clientFullmatch: HttpClient = elasticClientProvider.clientFullmatch
  val client: HttpClient = elasticClientProvider.client

  lazy val apiVersion: String = {
    val filename = "version.app"
    val path = configProvider.config.pathToResources + filename
    val currentDirectory = new java.io.File(".").getCanonicalPath

    // `Source.fromFile` needs an absolute path to the file, and current directory depends on where sbt was lauched
    // `getResource` may return null, that's why we wrap it into an `Option`
    val resource = Option(getClass.getResource(path))
      .map(Source.fromURL)
      .getOrElse(Source.fromFile(currentDirectory + path))

    resource.getLines().mkString("")
  }

  // lazy to avoid application crash at startup if ES is down
  lazy val dataVersion: String = {
    val alias: String = configProvider.config.elasticSearch.indexes.hybridIndex + "_current"
    logger.warn("alias) = " + alias)
    val aliaseq: Seq[String] = Seq {
      alias
    }

    val requestForIndexes =
      if (gcp) clientFullmatch.execute {
        getAliases(Nil, aliaseq)
      }
      else client.execute {
        getAliases(Nil, aliaseq)
      }

    // yes, it is blocking, but it only does this request once and there is also timeout in case it goes wrong
    val indexes: Either[RequestFailure, RequestSuccess[IndexAliases]] = Await.result(requestForIndexes, 10 seconds)

    val index: String = indexes match {
      case Left(l) => l.error.reason
      case Right(r) => r.result.mappings.keys.toString()
    }

    logger.warn("index name(s) = " + index)

    Option(index).map(removeBaseIndexName)
      .map(removeLetters)
      .filter(_.length >= 2) // epoch number should contain at least 2 numbers
      .map(_.substring(0, 2))
      .getOrElse("NA")
  }

  private def removeBaseIndexName(index: String): String = index.substring(index.indexOf('_') + 1).takeWhile(_ != '_')

  private def removeLetters(index: String): String = index.filter(_.isDigit)

}
