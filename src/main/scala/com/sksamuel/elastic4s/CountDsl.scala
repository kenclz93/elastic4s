package com.sksamuel.elastic4s

import org.elasticsearch.action.count.CountRequestBuilder
import org.elasticsearch.action.support.QuerySourceBuilder
import org.elasticsearch.index.query.{ QueryBuilder, QueryBuilders }

/** @author Stephen Samuel */
trait CountDsl {

  def count = new CountExpectsIndex
  class CountExpectsIndex {
    def from(indexesTypes: IndexesTypes): CountDefinition = new CountDefinition(indexesTypes)
    def from(indexes: Iterable[String]): CountDefinition = from(IndexesTypes(indexes))
    def from(indexes: String*): CountDefinition = from(IndexesTypes(indexes))
  }
  def count(indexesTypes: IndexesTypes): CountDefinition = new CountDefinition(indexesTypes)
  def count(indexes: String*): CountDefinition = count(IndexesTypes(indexes))

  class CountDefinition(indexesTypes: IndexesTypes) {

    val _builder = new CountRequestBuilder(ProxyClients.client)
      .setIndices(indexesTypes.indexes: _*)
      .setTypes(indexesTypes.types: _*)
      .setQuery(QueryBuilders.matchAllQuery())

    def build = _builder.request()

    def routing(routing: String): CountDefinition = {
      _builder.setRouting(routing)
      this
    }
    def minScore(minScore: Double): CountDefinition = {
      _builder.setMinScore(minScore.toFloat)
      this
    }

    def where(string: String): CountDefinition = query(new SimpleStringQueryDefinition(string))
    def where(block: => QueryDefinition): CountDefinition = javaquery(block.builder)
    def query(string: String): CountDefinition = query(new SimpleStringQueryDefinition(string))
    def query(block: => QueryDefinition): CountDefinition = javaquery(block.builder)
    def javaquery(block: => QueryBuilder): CountDefinition = {
      _builder.request.source(new QuerySourceBuilder().setQuery(block))
      //_builder.setQuery(block)
      this
    }

    def types(iterable: Iterable[String]): CountDefinition = types(iterable.toSeq: _*)
    def types(types: String*): CountDefinition = {
      _builder.setTypes(types: _*)
      this
    }
  }

}