package main.scala.rendering
import linalg._
/**
  * Created by anton on 12/16/2016.
  */
object Definitions {
	implicit class IColor( c : Int ) {
		def toVec4 = {
			vec4(
				( c >> 24 ) & 0xff ,
				( c >> 16 ) & 0xff ,
				( c >> 8 ) & 0xff ,
				c & 0xff
			) / 0xff
		}
	}
}
case class Style( z : Float = 0 , size : Float = 1 , color : Int = 0x000000ff , shadow_size : Float  = 1 , shadow_color : Int = 0x00000080 )
trait Renderer {
	def render( draw_list : Seq[ Command ] ) : Unit
}
class Command( val style : Style )
case class Rect( center : vec2 , size : vec2 , pstyle : Style ) extends Command( pstyle )
case class Line( start : vec2 , end : vec2 , pstyle : Style ) extends Command( pstyle )
case class CubicBezier( start : vec2 , start_tan : vec2 , end_tan : vec2 , end : vec2 , pstyle : Style ) extends Command( pstyle )
case class Circle( pos : vec2 , radius : Float , pstyle : Style ) extends Command( pstyle )
case class Text( pos : vec2 , text : String , pstyle : Style ) extends Command( pstyle )
