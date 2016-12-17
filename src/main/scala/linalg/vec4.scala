package linalg
/**
  * Created by anton on 12/8/2016.
  */
case class vec4( x : Float, y : Float, z : Float, w : Float )
{
	def +( that : vec4 ) : vec4 = vec4(
		x + that.x,
		y + that.y,
		z + that.z,
		w + that.w
	)
	def -( that : vec4 ) : vec4 = vec4(
		x - that.x,
		y - that.y,
		z - that.z,
		w - that.w
	)
	def *( k : Float ) : vec4 = vec4(
		x * k,
		y * k,
		z * k,
		w * k
	)
	def /( k : Float ) : vec4 = vec4(
		x / k,
		y / k,
		z / k,
		w / k
	)
	def *( that : vec4 ) : Float =
		x * that.x +
		y * that.y +
		z * that.z +
		w * that.w
	def mod2() : Float = this * this
	def mod() : Float = Math.sqrt( this mod2 ) toFloat
	def norm() : vec4 = this / this.mod
}
