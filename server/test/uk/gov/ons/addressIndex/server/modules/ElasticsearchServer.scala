package uk.gov.ons.addressIndex.server.modules


import java.nio.file.Files
import org.apache.commons.io.FileUtils
import org.elasticsearch.client._
//import org.elasticsearch.node._
//import org.elasticsearch.common.settings._
//import org.elasticsearch.node.NodeBuilder._

class ElasticsearchServer {

  private val clusterName = "elasticsearch"
  private val dataDir = Files.createTempDirectory("elasticsearch_data_").toFile
//  private val settings = ImmutableSettings.settingsBuilder
//    .put("path.data", dataDir.toString)
//    .put("cluster.name", clusterName)
//    .build
//
//  private lazy val node = nodeBuilder().local(true).settings(settings).build
//  def client: RestClient = node.client
//
//  def start(): Unit = {
//    node.start()
//  }
//
//  def stop(): Unit = {
//    node.close()
//
//    try {
//      FileUtils.forceDelete(dataDir)
//    } catch {
//      case e: Exception => // dataDir cleanup failed
//    }
//  }
//
//  def createAndWaitForIndex(index: String): Unit = {
//    client.admin.indices.prepareCreate(index).execute.actionGet()
//    client.admin.cluster.prepareHealth(index).setWaitForActiveShards(1).execute.actionGet()
//  }
}
