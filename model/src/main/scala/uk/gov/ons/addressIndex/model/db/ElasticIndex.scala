package uk.gov.ons.addressIndex.model.db

import com.sksamuel.elastic4s.{CreateIndexDefinition, DeleteIndexDefinition, ElasticClient, ElasticDsl}
import com.sksamuel.elastic4s.mappings.MappingDefinition
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse
import org.elasticsearch.action.admin.indices.delete.DeleteIndexResponse
import scala.concurrent.{ExecutionContext, Future}
import com.sksamuel.elastic4s.ElasticDsl._

/**
  * Implement this trait and use ElasticIndexSugar.
  */
trait ElasticIndex[T <: Product with Serializable] {
  /**
    * The name of the index
    */
  def name() : String

  /**
    * The mapping definition for this index
    * @return
    */
  def mappingDefinitions() : Seq[MappingDefinition]

  def deleteIndex() : DeleteIndexDefinition = ElasticDsl.delete index name

  def createIndex() : CreateIndexDefinition = ElasticDsl.create.index(name).mappings(mappingDefinitions:_*)
}

trait ElasticIndexSugar {

  /**
    * Creates indexes.
    *
    * @param indexes
    * @param client
    * @return
    */
  def createIndex(indexes : ElasticIndex[_]*)(client : ElasticClient)(implicit ec : ExecutionContext) : Future[Seq[CreateIndexResponse]] = {
    Future sequence indexes.map(i => client execute i.createIndex)
  }

  /**
    * Deletes indexes.
    *
    * @param indexes
    * @param client
    * @return
    */
  def deleteIndex(indexes: ElasticIndex[_]*)(client : ElasticClient)(implicit ec : ExecutionContext) : Future[Seq[DeleteIndexResponse]] = {
    Future sequence indexes.map(i => client execute i.deleteIndex)
  }
}