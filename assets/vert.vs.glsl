precision highp float;
uniform vec4 color;
varying float t;
void main()
{
	//vec2 radius = uv - 0.5;
	//vec2 dist = abs( radius ) * 2.0;
	gl_FragColor = vec4( color ) * vec4( vec3( 1.0 ) , 1.0 - abs(t) );/*vec4( vec3(0) , 1.0 - pow(
		clamp(
			max( dist.x , dist.y )
			, 0.0
			, 1.0
		)
	, 10.0 ) );*/
}