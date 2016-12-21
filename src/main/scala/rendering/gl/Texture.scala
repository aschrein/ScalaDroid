package main.scala.rendering.gl
import java.nio.{Buffer, ByteBuffer, ByteOrder, IntBuffer}
import android.graphics.Bitmap
import android.opengl.GLES20._
import android.opengl.GLUtils
import linalg.{ivec2, vec2}
import scala.collection.mutable
/**
  * Created by anton on 12/6/2016.
  */
object Texture {
	def genTexture = {
		val buf = Array ( 0 )
		glGenTextures ( 1, buf, 0 )
		buf ( 0 )
	}
	def apply ( data : Buffer, width : Int, height : Int, internal_format : Int = GL_RGB, format : Int = GL_UNSIGNED_BYTE ) : Texture = {
		val handle = genTexture
		glBindTexture ( GL_TEXTURE_2D, handle )
		glTexImage2D ( GL_TEXTURE_2D, 0, internal_format, width, height, 0, internal_format, format, data )
		glGenerateMipmap ( GL_TEXTURE_2D )
		glTexParameteri ( GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR )
		glTexParameteri ( GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR )
		glTexParameteri ( GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE )
		glTexParameteri ( GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE )
		new Texture ( handle, width, height )
	}
	def apply ( bitmap : Bitmap ) = {
		val handle = genTexture
		glBindTexture ( GL_TEXTURE_2D, handle )
		GLUtils.texImage2D ( GL_TEXTURE_2D, 0, bitmap, 0 )
		//glTexImage2D ( GL_TEXTURE_2D, 0, internal_format, width , height , 0 , internal_format, format, data )
		glGenerateMipmap ( GL_TEXTURE_2D )
		glTexParameteri ( GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR )
		glTexParameteri ( GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR )
		glTexParameteri ( GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE )
		glTexParameteri ( GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE )
		new Texture ( handle, width = bitmap.getWidth, height = bitmap.getHeight )
	}
	def empty ( width : Int, height : Int, internal_format : Int = GL_RGB, format : Int = GL_UNSIGNED_BYTE ) = {
		val handle = genTexture
		glBindTexture ( GL_TEXTURE_2D, handle )
		glTexImage2D ( GL_TEXTURE_2D, 0, internal_format, width, height, 0, internal_format, format, null )
		glTexParameteri ( GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR )
		glTexParameteri ( GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR )
		glTexParameteri ( GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE )
		glTexParameteri ( GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE )
		new Texture ( handle, width, height )
	}
	def genFBO = {
		val buf = Array ( 0 )
		glGenFramebuffers ( 1, buf, 0 )
		buf ( 0 )
	}
}
class Texture ( val handle : Int, val height : Int, val width : Int ) extends GLObject {
	override def dispose ( ) : Unit = {
		glDeleteTextures ( 0, Array ( handle ), 0 )
	}
}
class TextureAtlas ( width : Int, height : Int, cell_size : Int, internal_format : Int = GL_RGB, format : Int = GL_UNSIGNED_BYTE ) extends GLBindable {
	val attached_texture = Texture.empty ( width, height, internal_format, format )
	val fbo = Texture.genFBO
	lazy val rect_buffer = {
		val vertices = ByteBuffer.allocate ( 32 ).order ( ByteOrder.LITTLE_ENDIAN )
		vertices putFloat -1.0f putFloat -1.0f
		vertices putFloat -1.0f putFloat 1.0f
		vertices putFloat 1.0f putFloat 1.0f
		vertices putFloat 1.0f putFloat -1.0f
		vertices.rewind ( )
		new VertexBuffer ( vertices, GL_STATIC_DRAW, Attribute ( 0, 2, GL_FLOAT, false ) )
	}
	lazy val program = new Program (
		"""precision highp float;
		  |uniform sampler2D texture;
		  |varying vec2 uv;
		  |void main()
		  |{
		  |	gl_FragColor = texture2D( texture , uv );
		  |}""".stripMargin,
		"""
		  |uniform vec2 offset;
		  |uniform vec2 scale;
		  |attribute vec2 position;
		  |varying vec2 uv;
		  |void main()
		  |{
		  |	uv = vec2( 0.5 , -0.5 ) * position + 0.5;
		  | vec2 toffset = offset * 2.0 - vec2(1.0) + scale;
		  |	gl_Position = vec4( position * scale + toffset , 0.0 , 1.0 );
		  |}""".stripMargin
	)
	bind ( )
	glFramebufferTexture2D ( GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, attached_texture.handle, 0 )
	clear ( )
	unbind ( )
	def getSize = vec2 ( attached_texture.width, attached_texture.height )
	val bitset = new mutable.BitSet ( width * height / cell_size / cell_size )
	val map = new mutable.HashMap[ Texture, (Int, Int, Int, Int) ]
	def allocate ( texture : Texture, size : ivec2 ) : (Float, Float, Float, Float) = {
		def test ( x : Int, y : Int, dx : Int, dy : Int ) : Boolean = {
			if ( dx == 0 )
				if ( dy == 0 ) true
				else {
					if ( !bitset ( x + y * width / cell_size ) ) {
						test ( x, y + 1, dx, dy - 1 )
					} else {
						false
					}
				}
			else {
				if ( !bitset ( x + y * width / cell_size ) ) {
					test ( x + 1, y, dx - 1, dy )
				} else {
					false
				}
			}
		}
		def findNext ( x : Int, y : Int, v : Boolean ) : (Int, Int) = {
			if ( x >= width / cell_size ) {
				if ( y >= height / cell_size ) {
					(-1, -1)
				} else
					findNext ( 0, y + 1, v )
			} else if ( bitset ( x + y * width / cell_size ) != v ) {
				findNext ( x + 1, y, v )
			} else (x, y)
		}
		def findProper ( cells_x : Int, cells_y : Int ) : (Int, Int) = {
			var sp = (0, 0)
			while ( true ) {
				sp = findNext ( sp._1, sp._2, false )
				if ( sp._1 < 0 ) return (-1, -1)
				else if ( test ( sp._1, sp._2, cells_x, cells_y ) ) {
					return (sp._1, sp._2)
				}
				sp = findNext ( sp._1, sp._2, true )
			}
			(-1, -1)
		}
		val isize = ivec2 ( ( size.x + cell_size / 2 ) / cell_size, ( size.y + cell_size / 2 ) / cell_size )
		val p = findProper ( isize.x, isize.y )
		for ( i <- p._2 until p._2 + isize.y ) {
			for ( j <- p._1 until p._1 + isize.x ) {
				bitset.add ( j + i * width / cell_size )
			}
		}
		if ( p._1 >= 0 ) {
			map.put ( texture, (p._1, p._2, isize.x, isize.y) )
			bind ( )
			program.bind ( )
			rect_buffer.bind ( )
			val out = (
				p._1.toFloat * cell_size.toFloat / width,
				p._2.toFloat * cell_size.toFloat / height,
				isize.x.toFloat * cell_size.toFloat / width,
				isize.y.toFloat * cell_size.toFloat / height
				)
			program ( "offset" ) = vec2 ( out._1, out._2 )
			program ( "scale" ) = vec2 ( out._3, out._4 )
			program ( "texture" ) = (texture, 0)
			glDrawArrays ( GL_TRIANGLE_FAN, 0, 4 )
			rect_buffer.unbind ( )
			unbind ( )
			out
		} else (0.0f, 0.0f, 0.0f, 0.0f)
	}
	def draw ( ) = {
		program.bind ( )
		rect_buffer.bind ( )
		program ( "offset" ) = vec2 ( 0, 0 )
		program ( "scale" ) = vec2 ( 0.25f, 0.25f )
		program ( "texture" ) = (attached_texture, 0)
		glDrawArrays ( GL_TRIANGLE_FAN, 0, 4 )
		rect_buffer.unbind ( )
	}
	def getMapping ( texture : Texture ) = {
		val p = map.getOrElse ( texture, (0, 0, 0, 0) )
		(
			p._1.toFloat * cell_size.toFloat / width,
			p._2.toFloat * cell_size.toFloat / height,
			p._3.toFloat * cell_size.toFloat / width,
			p._4.toFloat * cell_size.toFloat / height
			)
	}
	def free ( key : Any ) = {
	}
	def put ( texture : Texture, origin : ivec2, size : ivec2 ) = {
	}
	override def dispose ( ) : Unit = {
		attached_texture.dispose ( )
		glDeleteFramebuffers ( 1, Array ( fbo ), 0 )
		rect_buffer.dispose ( )
	}
	override def bind ( ) : Unit = {
		glViewport ( 0, 0, width, height )
		glBindFramebuffer ( GL_FRAMEBUFFER, fbo )
	}
	def clear ( ) = {
		glClearColor ( 0.0f, 1.0f, 0.0f, 1.0f )
		glClear ( GL_COLOR_BUFFER_BIT )
	}
	override def unbind ( ) : Unit = glBindFramebuffer ( GL_FRAMEBUFFER, 0 )
}
