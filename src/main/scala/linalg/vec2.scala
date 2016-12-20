package linalg

/**
  * Created by anton on 12/8/2016.
  */
case class vec2( x : Float, y : Float )
{
	def +( that : vec2 ) : vec2 = vec2(
		x + that.x,
		y + that.y
	)
	def -( that : vec2 ) : vec2 = vec2(
		x - that.x,
		y - that.y
	)
	def *( that : vec2 ) : Float =
		x * that.x +
			y * that.y
	def /( k : Float ) : vec2 = vec2(
		x / k,
		y / k
	)
	def *( k : Float ) : vec2 = vec2(
		x * k,
		y * k
	)
	def mod2 = this * this
	def mod = Math.sqrt( this.mod2 ) toFloat
	def norm  = this / this.mod
	def ^( that : vec2 ) = x * that.y - y * that.x
	def angle( that : vec2 ) = {
		val d = this * that
		val x = this ^ that
		val acos = Math.acos( d toDouble ) toFloat;
		if( x > 0.0f ) acos
		else Math.PI.toFloat * 2 - acos
	}
}
case class ivec2( x : Int, y : Int )
{
	def +( that : ivec2 ) : ivec2 = ivec2(
		x + that.x,
		y + that.y
	)
	def -( that : ivec2 ) : ivec2 = ivec2(
		x - that.x,
		y - that.y
	)
	def *( that : ivec2 ) : Int =
		x * that.x +
			y * that.y
	def /( k : Float ) : vec2 = vec2(
		x / k,
		y / k
	)
	def *( k : Int ) : ivec2 = ivec2(
		x * k,
		y * k
	)
	def mod2 = this * this
	def mod = Math.sqrt( this mod2 ) toFloat
	def norm = this / this.mod
	def ^( that : ivec2 ) = x * that.y - y * that.x
	def angle( that : ivec2 ) = {
		val d = this * that
		val x = this ^ that
		val acos = Math.acos( d toDouble ) toFloat;
		if( x > 0.0f ) acos
		else Math.PI.toFloat * 2 - acos
	}
}
