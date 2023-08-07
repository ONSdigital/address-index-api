package uk.gov.ons.addressIndex.server.modules

import com.google.inject.{Inject, Singleton}
import com.sksamuel.elastic4s.ElasticDsl._
import com.sksamuel.elastic4s.handlers.alias.Alias
import com.sksamuel.elastic4s.{ElasticClient, Index}
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
  val clientFullmatch: ElasticClient = elasticClientProvider.clientFullmatch
  val client: ElasticClient = elasticClientProvider.client
  val termsAndConditions = configProvider.config.termsAndConditionsLink

  lazy val epochDates: Map[String,String] = {
    Map("39" -> "Exeter Sample",
      "95" -> "August 2022",
      "96" -> "October 2022",
      "97" -> "November 2022",
      "98" -> "January 2023",
      "99" -> "February 2023",
      "100" -> "March 2023",
      "101" -> "May 2023",
      "102" -> "June 2023",
      "103" -> "July 2023",
      "104" -> "September 2023",
      "105" -> "October 2023",
      "106" -> "November 2023",
       "NA" -> "test index")
  }

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
  lazy val epochList: List[String] = {
    val alias: String = configProvider.config.elasticSearch.indexes.hybridIndex + "*"
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
    val indexes = Try(Await.result(requestForIndexes, 10 seconds)).toEither

    val message: String = indexes match {
      case Left(l) => l.getMessage
      case Right(_) => "NA"
    }

    val indexMap: Map[Index, Seq[Alias]] = indexes match {
      case Left(_) => null
      case Right(r) => r.result.mappings
    }

    if (indexMap == null) List(message)
    else
    indexMap.map{case (key, _) =>
      Option(key.toString()).map(removeBaseIndexName)
        .map(removeLetters)
        .filter(_.length >= 2) // epoch number should contain at least 2 numbers
        .map(_.substring(0))
        .getOrElse("NA")
    }.toList
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
    val indexes = Try(Await.result(requestForIndexes, 10 seconds)).toEither

    val index: String = indexes match {
      case Left(l) => l.getMessage
      case Right(r) => r.result.mappings.keys.toString()
    }

    logger.warn("index name(s) = " + index)

    Option(index).map(removeBaseIndexName)
      .map(removeLetters)
      .filter(_.length >= 2) // epoch number should contain at least 2 numbers
      .map(_.substring(0))
      .getOrElse("NA")
  }

  private def removeBaseIndexName(index: String): String = index.substring(index.indexOf('_') + 1).takeWhile(_ != '_')

  private def removeLetters(index: String): String = index.filter(_.isDigit)

}
