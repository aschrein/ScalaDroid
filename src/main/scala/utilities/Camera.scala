package utilities
import linalg.{Mat, vec3, vec4}
/**
  * Created by anton on 12/8/2016.
  */

object Camera {
	def translate ( pos : vec3 ) = Mat (
		vec4 ( 1.0f, 0.0f, 0.0f, pos.x ),
		vec4 ( 0.0f, 1.0f, 0.0f, pos.y ),
		vec4 ( 0.0f, 0.0f, 1.0f, pos.z ),
		vec4 ( 0.0f, 0.0f, 0.0f, 1.0f )
	)
	def orient ( look : vec3, right : vec3, up : vec3 ) = Mat (
		right to4 0.0f,
		up to4 0.0f,
		look to4 0.0f,
		vec4 ( 0.0f, 0.0f, 0.0f, 1.0f )
	)
	def look ( pos : vec3, sight : vec3, upl : vec3 ) : Mat = {
		val look = -( sight - pos ).norm
		var up = upl.norm
		val right = ( look ^ up ).norm
		up = ( right ^ look ).norm
		orient( look , right , up ) * translate( -pos )
	}
	def perspective ( tanx : Float, tany : Float, nearp : Float, farp : Float ) : Mat = {
		Mat ( 4, 4, Array (
			1.0f / tanx, 0.0f, 0.0f, 0.0f,
			0.0f, 1.0f / tany, 0.0f, 0.0f,
			0.0f, 0.0f, -2.0f / ( farp - nearp ),0.0f,
			0.0f, 0.0f,  -( farp + nearp ) / ( farp - nearp ), 1.0f
		) )
	}
	def perspLook ( pos : vec3, sight : vec3, upl : vec3,
					  tanx : Float, tany : Float, nearp : Float, farp : Float ) = {
		perspective( tanx , tany , nearp , farp ) * look ( pos, sight, upl )
	}
}
