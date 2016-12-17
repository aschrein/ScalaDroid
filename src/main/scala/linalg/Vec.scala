package linalg

/**
  *
  * Created by anton on 12/8/2016.
  */
object Vec {
	def apply ( x : Float , y : Float , z : Float ) : Vec = new Vec ( Array( x , y , z ) )
}
case class Vec ( m : Array[ Float ] ) {
	def N = m.length
	def toVec2 = vec2 ( m ( 0 ), m ( 1 ) )
	def toVec3 = vec3 ( m ( 0 ), m ( 1 ), m ( 2 ) )
	def toVec4 = vec4 ( m ( 0 ), m ( 1 ), m ( 2 ), m ( 3 ) )
	def chechSize[ T ] ( a : Vec )( f : => T ) : T = if ( N == a.N ) f else {
		throw new Exception ( "different vector sizes" )
	}
	def + ( that : Vec ) = chechSize( that ){
		val tmp = new Array[ Float ]( N )
		for ( i <- 0 to N ) {
			tmp ( i ) = this.m ( i ) + that.m ( i )
		}
		new Vec (  tmp )
	}
	def - ( that : Vec ) = chechSize( that ){
		val tmp = new Array[ Float ]( N )
		for ( i <- 0 to N ) {
			tmp ( i ) = m ( i ) - that.m ( i )
		}
		new Vec ( tmp )
	}
	def map ( k : Float => Float ) = {
		val tmp = new Array[ Float ]( N )
		for ( i <- 0 to N ) {
			tmp ( i ) = k ( m ( i ) )
		}
		new Vec ( tmp )
	}
	def unary_- = map ( -_ )
	def * ( k : Float ) = map ( _ * k )
	def / ( k : Float ) = map ( _ / k )
	def * ( that : Vec ) = chechSize( that ){
		var acc = 0.0f
		for ( i <- 0 to N ) {
			acc += m ( i ) * that.m ( i )
		}
		acc
	}
	def mod2 ( ) = this * this
	def mod ( ) = Math.sqrt ( this mod2 ) toFloat
	def norm ( ) = this / this.mod
}
