package main.scala.gl

import java.nio.ByteBuffer

import android.opengl.GLES20._
/**
  * Created by anton on 12/6/2016.
  */
class Texture( data : ByteBuffer , width : Int , height : Int ) extends GLObject
{
	def genTexture = {
		val buf = Array( 0 )
		glGenTextures( 1 , buf , 0 )
		buf( 0 )
	}
	val handle = genTexture
	glBindTexture ( GL_TEXTURE_2D, handle )
	glTexImage2D ( GL_TEXTURE_2D, 0, GL_RGB, width , height , 0 , GL_RGB, GL_UNSIGNED_BYTE, data )
	glGenerateMipmap( GL_TEXTURE_2D )
	glTexParameteri ( GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR )
	glTexParameteri ( GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR )
	glTexParameteri ( GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE )
	glTexParameteri ( GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE )
	override def dispose ( ) : Unit = {
		glDeleteTextures( 0 , Array( handle ) , 0 )
	}
	override def bind ( ) : Unit = ???
	override def unbind ( ) : Unit = ???
}
