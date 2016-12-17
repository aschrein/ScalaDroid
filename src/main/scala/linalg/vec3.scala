package linalg
/**
  * Created by anton on 12/8/2016.
  */
case class vec3( x : Float, y : Float, z : Float )
{
	def +( that : vec3 ) = vec3(
		x + that.x,
		y + that.y,
		z + that.z
	)
	def -( that : vec3 ) = vec3(
		x - that.x,
		y - that.y,
		z - that.z
	)
	def *( that : vec3 ) : Float =
		x * that.x +
			y * that.y +
			z * that.z
	def unary_- = vec3( -x , -y , -z )
	def /( k : Float ) = vec3(
		x / k,
		y / k,
		z / k
	)
	def *( k : Float ) = vec3(
		x * k,
		y * k,
		z * k
	)
	def mod2 = this * this
	def mod = Math.sqrt( this mod2 ) toFloat
	def norm = this / this.mod
	def ^( that : vec3 ) = vec3(
		y * that.z - z * that.y,
		z * that.x - x * that.z,
		x * that.y - y * that.x
	)
	def angle( that : vec3 ) : Float = {
		val d = this * that
		val vx = this ^ that mod
		val acos = Math.acos( d toDouble ) toFloat;
		if( vx > 0.0f ) acos
		else Math.PI.toFloat * 2 - acos
	}
	def to4( w : Float = 0.0f ) = vec4( x , y , z , w )
}