package uk.gov.ons.addressIndex.server.modules


import java.nio.file.Files

class ElasticsearchServer {

  private val clusterName = "elasticsearch"
  private val dataDir = Files.createTempDirectory("elasticsearch_data_").toFile

}
