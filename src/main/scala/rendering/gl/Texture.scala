package main.scala.rendering.gl

import java.nio.{Buffer, ByteBuffer, IntBuffer}

import android.graphics.Bitmap
import android.opengl.GLES20._
import android.opengl.GLUtils
/**
  * Created by anton on 12/6/2016.
  */
object Texture {
	def genTexture = {
		val buf = Array( 0 )
		glGenTextures( 1 , buf , 0 )
		buf( 0 )
	}
	def apply ( data : Buffer, width : Int, height : Int, internal_format : Int = GL_RGB, format : Int = GL_UNSIGNED_BYTE ) : Texture = {
		val handle = genTexture
		glBindTexture ( GL_TEXTURE_2D, handle )
		glTexImage2D ( GL_TEXTURE_2D, 0, internal_format, width , height , 0 , internal_format, format, data )
		glGenerateMipmap( GL_TEXTURE_2D )
		glTexParameteri ( GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR )
		glTexParameteri ( GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR )
		glTexParameteri ( GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE )
		glTexParameteri ( GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE )
		new Texture( handle )
	}
	def apply( bitmap: Bitmap ) = {
		val handle = genTexture
		glBindTexture ( GL_TEXTURE_2D, handle )
		GLUtils.texImage2D(GL_TEXTURE_2D,0,bitmap,0)
		//glTexImage2D ( GL_TEXTURE_2D, 0, internal_format, width , height , 0 , internal_format, format, data )
		glGenerateMipmap( GL_TEXTURE_2D )
		glTexParameteri ( GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR )
		glTexParameteri ( GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR )
		glTexParameteri ( GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE )
		glTexParameteri ( GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE )
		new Texture( handle )
	}
}
class Texture( val handle : Int ) extends GLObject
{
	override def dispose ( ) : Unit = {
		glDeleteTextures( 0 , Array( handle ) , 0 )
	}
	override def bind ( ) : Unit = ???
	override def unbind ( ) : Unit = ???
}
