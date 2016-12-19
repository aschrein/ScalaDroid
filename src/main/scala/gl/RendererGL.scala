package main.scala.gl
import java.nio.{ByteBuffer, ByteOrder, IntBuffer}
import java.util.logging.{Level, Logger}

import android.content.res.AssetManager
import android.graphics.{Bitmap, Canvas, Paint}
import android.opengl.GLES20._
import linalg._
import main.scala.rendering._
import main.scala.rendering.Definitions._
import GL._
import android.opengl.{GLU, GLUtils}

import scala.collection.mutable
/**
  * Created by anton on 12/16/2016.
  */
object TEMP {
	def constructCube ( offset : vec3, vertex_buffer : ByteBuffer, index_buffer : ByteBuffer ) = {
		val head = vertex_buffer.position / 12
		for ( i <- 0 until 8 ) {
			vertex_buffer putFloat ( ( i & 1 ) * 2 - 1 + offset.x )
			vertex_buffer putFloat ( ( i >> 1 & 1 ) * 2 - 1 + offset.y )
			vertex_buffer putFloat ( ( i >> 2 & 1 ) * 2 - 1 + offset.z )
		}
		val index_array = Array [ Short ](
			0, 2, 3, 3, 1, 0, 0, 5, 4, 0, 1, 5,
			0, 6, 2, 0, 4, 6, 2, 6, 7, 2, 7, 3,
			7, 5, 3, 5, 1, 3, 4, 7, 6, 4, 5, 7
		)
		for ( i <- index_array ) index_buffer putShort ( i + head ).toShort
	}
}
class RendererGL ( implicit assets : AssetManager ) {
	glDisable ( GL_DEPTH_TEST )
	//glDepthFunc ( GL_LEQUAL )
	glDisable ( GL_CULL_FACE )
	//glFrontFace ( GL_CW )
	//glCullFace ( GL_BACK )
	glEnable ( GL_BLEND )
	glBlendFunc ( GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA )
	glBlendEquation ( GL_FUNC_ADD )
	val program_map = new mutable.HashMap[ String, Program ]( )
	val buffers_map = new mutable.HashMap[ String, DevBuffer ]( )
	init ( )
	def init ( ) = {
		program_map ( "rect" ) = new Program (
			"""precision highp float;
			  |uniform vec4 color;
			  |uniform sampler2D texture;
			  |varying vec2 uv;
			  |void main()
			  |{
			  |float alpha = length( uv - 0.5 ) < 0.5 ? 1.0 : 0.0;
			  |	gl_FragColor = color * texture2D( texture , uv ) * vec4( vec3(1) , alpha );
			  |}""".stripMargin,
			"""
			  |uniform vec2 offset;
			  |uniform vec2 scale;
			  |uniform mat4 viewproj;
			  |attribute vec2 position;
			  |varying vec2 uv;
			  |void main()
			  |{
			  |	uv = vec2( 0.5 , -0.5 ) * position + 0.5;
			  |	gl_Position = viewproj * vec4( position * scale + offset , 0.0 , 1.0 );
			  |}""".stripMargin
		)

		val shader = "vert.vs.glsl".load
		program_map ( "rect_shadow" ) = new Program (
			shader,
			"""
			  |uniform vec2 offset;
			  |uniform vec2 scale;
			  |uniform vec2 uvscale;
			  |attribute vec2 position;
			  |varying float t;
			  |void main()
			  |{
			  |	t = dot( uvscale, 0.5 * position + 0.5);
			  |	gl_Position = vec4( position * scale + offset , 0.0 , 1.0 );
			  |}""".stripMargin
		);
		{
			val vertices = ByteBuffer.allocate ( 32 ).order ( ByteOrder.LITTLE_ENDIAN )
			vertices putFloat -1.0f putFloat -1.0f
			vertices putFloat -1.0f putFloat 1.0f
			vertices putFloat 1.0f putFloat 1.0f
			vertices putFloat 1.0f putFloat -1.0f
			vertices.rewind ( )
			buffers_map ( "rect_vbo" ) = new VertexBuffer ( vertices, GL_STATIC_DRAW, Attribute ( 0, 2, GL_FLOAT, false ) )
		}
	}
	def dispose ( ) = {
		program_map foreach {
			_._2.dispose ( )
		}
		buffers_map foreach {
			_._2.dispose ( )
		}
	}
	val font_tex = renderText ( "hello" )
	def renderText ( text : String ) = {
		val bitmap = Bitmap.createBitmap ( 256, 256, Bitmap.Config.ARGB_8888 )
		val canvas = new Canvas ( bitmap )
		bitmap.eraseColor ( 0 )
		val textPaint = new Paint
		textPaint.setTextSize ( 32 )
		textPaint.setAntiAlias ( true )
		textPaint.setARGB ( 0xff, 0x00, 0x00, 0x00 )
		canvas.drawText ( "Hello World", 16, 112, textPaint )
		val arr = new Array[ Int ]( 256 * 256 )
		bitmap.getPixels ( arr, 0, 256, 0, 0, 256, 256 )
		Texture ( IntBuffer.wrap ( arr ), 256, 256 , GL_RGBA )
	}
	def render ( viewproj : Mat , draw_list : Seq[ Command ] ) : Unit = {
		glClearColor ( 1, 0.9f, 1, 1 )
		glClearDepthf ( 1 )
		glClear ( GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT )
		val sorted_draw_list = draw_list.sortWith ( _.style.z > _.style.z )
		sorted_draw_list foreach {
			case Rect ( center, size, style ) =>
				val buffer = buffers_map ( "rect_vbo" )
				GL.using ( buffer ) { _ =>
					/*GL.using ( program_map ( "rect_shadow" ) ) {
						case program : Program =>
							program ( "offset" ) = center + vec2 ( 0.0f, size.y + style.shadow_size )
							program ( "scale" ) = vec2 ( size.x, style.shadow_size )
							program ( "color" ) = style.shadow_color.toVec4
							program ( "uvscale" ) = vec2 ( 0.0f, 1.0f )
							glDrawArrays ( GL_TRIANGLE_FAN, 0, 4 )
							program ( "offset" ) = center - vec2 ( 0.0f, size.y + style.shadow_size )
							program ( "scale" ) = vec2 ( size.x, style.shadow_size )
							program ( "uvscale" ) = vec2 ( 0.0f, -1.0f )
							glDrawArrays ( GL_TRIANGLE_FAN, 0, 4 )
					}*/
					GL.using ( program_map ( "rect" ) ) {
						case program : Program =>
							program( "viewproj" ) = viewproj.T
							program ( "offset" ) = center
							program ( "scale" ) = size
							program ( "color" ) = style.color.toVec4
							if( style.texture != null ) program ( "texture" ) = (style.texture.asInstanceOf[Texture], 0)
							else program( "texture" ) = ( 0 , 0 )
							glDrawArrays ( GL_TRIANGLE_FAN, 0, 4 )
					}
				}
			case _ =>
		}
	}
}
