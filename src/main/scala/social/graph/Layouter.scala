package main.scala.social.graph
import linalg.vec2
/**
  * Created by anton on 12/20/2016.
  */
class Layouter( relationGraphView: RelationGraphView ) {
	def force( p0 : vec2 , p1 : vec2 ) = Math.max(
		Math.min( 1.0f , -1.0f / ( 1.0f + ( p1 - p0 ).mod2 ) + ( p1 - p0 ).mod2 )
		, -1.0f )
	var last_time = 0.0f
	var dt = 0.1f
	def tick() = {
		val relations = relationGraphView.relation_graph.getRelations
			.map( p => ( relationGraphView.getView(p._1) , relationGraphView.getView(p._2) ) )
		relations.foreach( pair => {
			val dr = ( pair._1.pos - pair._2.pos ).norm
			val fmod = force( pair._1.pos , pair._2.pos ) * dt
			pair._1.pos -= dr * fmod
			pair._2.pos += dr * fmod
		} )
	}
}
