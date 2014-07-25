package com.sksamuel.elastic4s

import org.elasticsearch.action.admin.indices.alias.IndicesAliasesRequest
import org.elasticsearch.action.admin.indices.alias.get.GetAliasesRequest
import org.elasticsearch.cluster.metadata.AliasAction
import org.elasticsearch.index.query.FilterBuilder

trait AliasesDsl {

  def aliases = AliasesExpectsAction
  def aliases(aliasMutations: MutateAliasDefinition*) = new IndicesAliasesRequestDefinition(aliasMutations: _*)

  object AliasesExpectsAction {
    def add(alias: String) = new AddAliasExpectsIndex(alias)
    def remove(alias: String) = new RemoveAliasExpectsIndex(alias)
    def get(aliases: String*) = new GetAliasDefinition(aliases)
  }

  class AddAliasExpectsIndex(alias: String) {
    def on(index: String) = new MutateAliasDefinition(new AliasAction(AliasAction.Type.ADD, index, alias))
  }

  class RemoveAliasExpectsIndex(alias: String) {
    def on(index: String) = new MutateAliasDefinition(new AliasAction(AliasAction.Type.REMOVE, index, alias))
  }
}

class GetAliasDefinition(aliases: Seq[String]) {
  val request = new GetAliasesRequest(aliases.toArray)
  def build = request
  def on(indexes: String*): GetAliasDefinition = {
    request.indices(indexes: _*)
    this
  }
}

class MutateAliasDefinition(val aliasAction: AliasAction) {
  def routing(route: String) = new MutateAliasDefinition(aliasAction.routing(route))
  def filter(filter: FilterBuilder) = new MutateAliasDefinition(aliasAction.filter(filter))
  def build = new IndicesAliasesRequest().addAliasAction(aliasAction)
}

class IndicesAliasesRequestDefinition(aliasMutations: MutateAliasDefinition*) {
  def build = aliasMutations.foldLeft(new IndicesAliasesRequest())(
    (request, aliasDef) => request.addAliasAction(aliasDef.aliasAction))
}
