package main.scala.rendering.gl

import java.nio.{Buffer, ByteBuffer, ByteOrder}

import android.opengl.GLES20._
/**
  * Created by anton on 12/13/2016.
  */
case class Attribute ( loc : Int, size : Int, `type` : Int, normalized : Boolean = false )
abstract class DevBuffer extends GLObject {
}
object BufferUtil {
	def genBuffer ( ) = {
		val buf = Array ( 0 )
		glGenBuffers ( 1, buf, 0 )
		buf ( 0 )
	}
}
class VertexBuffer ( buf : Buffer, usage : Int, attr : Attribute* ) extends DevBuffer {
	val attributes = attr.toList
	val handle = BufferUtil.genBuffer ( )
	glBindBuffer ( GL_ARRAY_BUFFER, handle )
	glBufferData ( GL_ARRAY_BUFFER, buf.limit, buf, usage )
	glBindBuffer ( GL_ARRAY_BUFFER, 0 )
	override def dispose ( ) : Unit = {
		glDeleteBuffers ( 1, Array ( handle ), 0 )
	}
	def bind ( ) = {
		glBindBuffer ( GL_ARRAY_BUFFER, handle )
		var offset : List[ Int ] = List ( 0 )
		var stride = 0
		def getSize = ( x : Int ) => x match {
			case GL_FLOAT | GL_INT | GL_UNSIGNED_INT => 4
			case GL_SHORT | GL_UNSIGNED_SHORT => 2
			case GL_UNSIGNED_BYTE | GL_BYTE => 1
			case _ => 0
		}
		for ( attribute <- attributes ) {
			stride += getSize ( attribute.`type` ) * attribute.size
			offset = offset ::: ( stride :: Nil )
		}
		for ( attribute <- attributes ) {
			glEnableVertexAttribArray ( attribute.loc )
			glVertexAttribPointer ( attribute.loc, attribute.size, attribute.`type`, attribute.normalized, stride, offset.head )
			offset = offset.tail
		}
	}
	def unbind ( ) = {
		for ( attribute <- attributes ) {
			glDisableVertexAttribArray ( attribute.loc )
		}
		glBindBuffer ( GL_ARRAY_BUFFER, 0 )
	}
}
class IndexBuffer ( buf : ByteBuffer, usage : Int ) extends DevBuffer {
	val handle = BufferUtil.genBuffer ( )
	glBindBuffer ( GL_ELEMENT_ARRAY_BUFFER, handle )
	glBufferData ( GL_ELEMENT_ARRAY_BUFFER, buf.limit, buf, usage )
	glBindBuffer ( GL_ELEMENT_ARRAY_BUFFER, 0 )
	override def dispose ( ) : Unit = {
		glDeleteBuffers ( 1, Array ( handle ), 0 )
	}
	def bind ( ) = {
		glBindBuffer ( GL_ELEMENT_ARRAY_BUFFER, handle )
	}
	def unbind ( ) = {
		glBindBuffer ( GL_ELEMENT_ARRAY_BUFFER, 0 )
	}
}