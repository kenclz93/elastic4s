package com.sksamuel.elastic4s

import com.sksamuel.elastic4s.query._
import org.apache.lucene.search.join.ScoreMode
import org.elasticsearch.common.geo.GeoPoint
import org.elasticsearch.index.query._

import scala.language.{implicitConversions, reflectiveCalls}

trait QueryDsl {

  implicit def string2query(string: String): SimpleStringQueryDefinition = SimpleStringQueryDefinition(string)
  implicit def tuple2query(kv: (String, String)): TermQueryDefinition = TermQueryDefinition(kv._1, kv._2)

  def query = this

  def boostingQuery(positiveQuery: QueryDefinition,
                    negativeQuery: QueryDefinition): BoostingQueryDefinition = BoostingQueryDefinition(positiveQuery, negativeQuery)

  def commonQuery(field: String) = new CommonQueryExpectsText(field)
  def commonQuery(field: String, text: String) = CommonTermsQueryDefinition(field, text)
  class CommonQueryExpectsText(name: String) {
    def text(q: String): CommonTermsQueryDefinition = CommonTermsQueryDefinition(name, q)
    def query(q: String): CommonTermsQueryDefinition = text(q)
  }

  def constantScoreQuery(q: QueryDefinition) = ConstantScoreDefinition(QueryBuilders.constantScoreQuery(q.builder))

  def dismax(queries: Iterable[QueryDefinition]): DisMaxDefinition = new DisMaxDefinition(queries.toSeq)
  def dismax(first: QueryDefinition, rest: QueryDefinition*): DisMaxDefinition = dismax(first +: rest)

  def existsQuery(field: String) = ExistsQueryDefinition(field)

  def functionScoreQuery(query: QueryDefinition): FunctionScoreQueryDefinition =
    FunctionScoreQueryDefinition().withQuery(query)

  def functionScoreQuery(query: QueryDefinition, functions: Seq[FilterFunctionDefinition]): FunctionScoreQueryDefinition =
    FunctionScoreQueryDefinition().withQuery(query).withFunctions(functions)

  def geoBoxQuery(field: String) = GeoBoundingBoxQueryDefinition(field)

  def geoDistanceQuery(field: String): GeoDistanceQueryDefinition = GeoDistanceQueryDefinition(field)

  def geoHashCell(field: String, value: String): GeoHashCellQueryDefinition =
    GeoHashCellQueryDefinition(field, value)

  def geoHashCell(field: String, value: GeoPoint): GeoHashCellQueryDefinition =
    GeoHashCellQueryDefinition(field, value.geohash)

  def geoPolyonQuery(field: String) = new {
    def points(first: GeoPoint, rest: GeoPoint*) = points(first +: rest)
    def points(points: Iterable[GeoPoint]) = geoPolygonQuery(field, points)
  }

  def geoPolygonQuery(field: String, first: GeoPoint, rest: GeoPoint*): GeoPolygonQueryDefinition =
    geoPolygonQuery(field, first +: rest)

  def geoPolygonQuery(field: String, points: Iterable[GeoPoint]): GeoPolygonQueryDefinition =
    GeoPolygonQueryDefinition(field, points.toSeq)

  def geoDistanceRangeQuery(field: String, geoPoint: GeoPoint) = GeoDistanceRangeQueryDefinition(field, geoPoint)

  def hasChildQuery(`type`: String) = new {
    def query(q: QueryDefinition) = new {
      def scoreMode(scoreMode: ScoreMode) = hasChildQuery(`type`, q, scoreMode)
    }
  }

  def hasChildQuery(`type`: String, query: QueryDefinition, scoreMode: ScoreMode) =
    HasChildQueryDefinition(`type`, query, scoreMode)

  def hasParentQuery(`type`: String) = new {
    def query(query: QueryDefinition) = new {
      def score(score: Boolean) = hasParentQuery(`type`, query, score)
    }
  }

  def hasParentQuery(`type`: String, query: QueryDefinition, score: Boolean) =
    HasParentQueryDefinition(`type`, query, score)


  def matchQuery(tuple: (String, Any)): MatchQueryDefinition = matchQuery(tuple._1, tuple._2)
  def matchQuery(field: String, value: Any): MatchQueryDefinition = MatchQueryDefinition(field, value)

  def matchPhraseQuery(field: String, value: Any): MatchPhraseDefinition = MatchPhraseDefinition(field, value)

  def matchPhrasePrefixQuery(field: String, value: Any) = MatchPhrasePrefixDefinition(field, value)

  def multiMatchQuery(text: String) = MultiMatchQueryDefinition(text)

  def matchAllQuery = new MatchAllQueryDefinition

  def moreLikeThisQuery(first: String, rest: String*) = moreLikeThisQuery(first +: rest)
  def moreLikeThisQuery(fields: Iterable[String]) = new {

    def texts(first: String, rest: String*) = texts(first +: rest)
    def texts(texts: Iterable[String]) = MoreLikeThisQueryDefinition(fields.toSeq, texts.toSeq, Nil)

    def items(first: MoreLikeThisItem, rest: MoreLikeThisItem*) = items(first +: rest)
    def items(items: Iterable[MoreLikeThisItem]) = MoreLikeThisQueryDefinition(fields.toSeq, Nil, items.toSeq)
  }

  def nestedQuery(path: String) = new {
    def query(query: QueryDefinition) = NestedQueryDefinition(path, query)
  }

  def query(queryString: String): QueryStringQueryDefinition = queryStringQuery(queryString)
  def queryStringQuery(queryString: String): QueryStringQueryDefinition = QueryStringQueryDefinition(queryString)

  def rangeQuery(field: String): RangeQueryDefinition = RangeQueryDefinition(field)

  def regexQuery(tuple: (String, Any)): RegexQueryDefinition = regexQuery(tuple._1, tuple._2)
  def regexQuery(field: String, value: Any): RegexQueryDefinition = RegexQueryDefinition(field, value)

  def prefixQuery(tuple: (String, Any)): PrefixQueryDefinition = prefixQuery(tuple._1, tuple._2)
  def prefixQuery(field: String, value: Any): PrefixQueryDefinition = PrefixQueryDefinition(field, value)

  def scriptQuery(script: String): ScriptQueryDefinition = ScriptQueryDefinition(script)

  def simpleStringQuery(q: String): SimpleStringQueryDefinition = SimpleStringQueryDefinition(q)
  def stringQuery(q: String): QueryStringQueryDefinition = QueryStringQueryDefinition(q)

  def spanFirstQuery = new {
    def query(spanQuery: SpanQueryDefinition) = new {
      def end(end: Int) = SpanFirstQueryDefinition(spanQuery, end)
    }
  }

  def spanNearQuery(defs: Iterable[SpanQueryDefinition], slop: Int): SpanNearQueryDefinition = SpanNearQueryDefinition(defs.toSeq, slop)

  def spanOrQuery(iterable: Iterable[SpanQueryDefinition]): SpanOrQueryDefinition = SpanOrQueryDefinition(iterable.toSeq)
  def spanOrQuery(first: SpanQueryDefinition, rest: SpanQueryDefinition*): SpanOrQueryDefinition = spanOrQuery(first +: rest)

  def spanTermQuery(field: String, value: Any): SpanTermQueryDefinition = new SpanTermQueryDefinition(field, value)

  def spanNotQuery(include: SpanQueryDefinition, exclude: SpanQueryDefinition): SpanNotQueryDefinition =
    SpanNotQueryDefinition(include, exclude)

  def spanMultiTermQuery(query: MultiTermQueryDefinition) = SpanMultiTermQueryDefinition(query)

  def termQuery(tuple: (String, Any)): TermQueryDefinition = termQuery(tuple._1, tuple._2)
  def termQuery(field: String, value: Any): TermQueryDefinition = TermQueryDefinition(field, value)

  def termsQuery(field: String, values: AnyRef*) = TermsQueryDefinition(field, values.map(_.toString))
  def termsQuery(field: String, values: Int*) = IntTermsQueryDefinition(field, values)
  def termsQuery(field: String, values: Long*) = LongTermsQueryDefinition(field, values)
  def termsQuery(field: String, values: Float*) = FloatTermsQueryDefinition(field, values)
  def termsQuery(field: String, values: Double*) = DoubleTermsQueryDefinition(field, values)

  def wildcardQuery(tuple: (String, Any)): WildcardQueryDefinition = wildcardQuery(tuple._1, tuple._2)
  def wildcardQuery(field: String, value: Any): WildcardQueryDefinition = WildcardQueryDefinition(field, value)

  def typeQuery(`type`: String) = TypeQueryDefinition(`type`)

  def idsQuery(ids: Iterable[String]): IdQueryDefinition = IdQueryDefinition(ids.toSeq)
  def idsQuery(id: String, rest: String*): IdQueryDefinition = IdQueryDefinition(id +: rest)

  // -- bool query dsl ---
  def bool(block: => BoolQueryDefinition): BoolQueryDefinition = block
  def bool(mustQueries: Seq[QueryDefinition],
           shouldQueries: Seq[QueryDefinition],
           notQueries: Seq[QueryDefinition]): BoolQueryDefinition = {
    must(mustQueries).should(shouldQueries).not(notQueries)
  }
  def must(queries: QueryDefinition*): BoolQueryDefinition = new BoolQueryDefinition().must(queries: _*)
  def must(queries: Iterable[QueryDefinition]): BoolQueryDefinition = new BoolQueryDefinition().must(queries)
  def filter(first: QueryDefinition, rest: QueryDefinition*): BoolQueryDefinition = filter(first +: rest)
  def filter(queries: Iterable[QueryDefinition]): BoolQueryDefinition = new BoolQueryDefinition().filter(queries)
  def should(queries: QueryDefinition*): BoolQueryDefinition = new BoolQueryDefinition().should(queries: _*)
  def should(queries: Iterable[QueryDefinition]): BoolQueryDefinition = new BoolQueryDefinition().should(queries)
  def not(queries: QueryDefinition*): BoolQueryDefinition = new BoolQueryDefinition().not(queries: _*)
  def not(queries: Iterable[QueryDefinition]): BoolQueryDefinition = new BoolQueryDefinition().not(queries)
}

trait QueryDefinition {
  def builder: org.elasticsearch.index.query.QueryBuilder
}
