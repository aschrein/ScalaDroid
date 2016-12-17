package linalg
/**
  * Created by anton on 12/8/2016.
  */
case class quat( n : vec3, w : Float )
{
	def dir = n
	def *( that : quat ) : quat = quat(
		that.n * w + n * that.w + n ^ that.n ,
		w * that.w - n * that.n
	)
	def conj = quat( -n , w )
	def mod2 = n.mod2 + w * w
	def mod = Math.sqrt( mod2 ).toFloat
	def /( k : Float ) : quat = quat( n / k , w / k )
	def inv : quat = conj / mod2
	def /( that : quat ) = this * that.inv
	def norm = this / mod
	def *( a : vec3 ) : vec3 = ( this * quat( a , 0.0f ) * this.conj ).n
}
