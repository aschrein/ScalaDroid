package main.scala.rendering.gl
import linalg.{Mat, vec2, vec3, vec4}
import android.opengl.GLES20._
import android.util.Log
/**
  * Created by anton on 12/9/2016.
  */
object Program {
	def enumerateLines ( text : String ) = text.split ( "\n" ).zipWithIndex.map ( pair => pair._2 + ":" + pair._1 + "\n" ).reduce ( _ + _ )
}
class Program ( frag_text : String, vert_text : String ) extends GLBindable {
	val vs = glCreateShader ( GL_VERTEX_SHADER )
	glShaderSource ( vs, vert_text )
	glCompileShader ( vs )
	val vs_validation = glGetShaderInfoLog ( vs )
	if ( vs_validation != null && vs_validation != "" ) {
		Log.w ( this.getClass.getSimpleName, Program.enumerateLines ( vert_text ) )
		Log.w ( this.getClass.getSimpleName, vs_validation )
		throw new Exception ( "invalid vertex shader source" )
	}
	val fs = glCreateShader ( GL_FRAGMENT_SHADER )
	glShaderSource ( fs, frag_text )
	glCompileShader ( fs )
	val fs_validation = glGetShaderInfoLog ( fs )
	if ( fs_validation != null && fs_validation != "" ) {
		Log.w ( this.getClass.getSimpleName, Program.enumerateLines ( frag_text ) )
		Log.w ( this.getClass.getSimpleName, fs_validation )
		throw new Exception ( "invalid fragment shader source" )
	}
	val program = glCreateProgram ( )
	glAttachShader ( program, vs )
	glAttachShader ( program, fs )
	glLinkProgram ( program )
	glValidateProgram ( program )
	val pstatus = Array(0)
	glGetProgramiv(program,GL_LINK_STATUS,pstatus,0)

	if ( pstatus( 0 ) == 0 ) {
		val prog_validation = glGetProgramInfoLog ( program )
		Log.w ( this.getClass.getSimpleName, Program.enumerateLines ( frag_text ) )
		Log.w ( this.getClass.getSimpleName, "__________________" )
		Log.w ( this.getClass.getSimpleName, Program.enumerateLines ( vert_text ) )
		Log.w ( this.getClass.getSimpleName, prog_validation )
		throw new Exception ( "program linkage error" )
	}
	def attribute ( name : String ) = {
		glGetAttribLocation ( program, name )
	}
	def uniform ( name : String ) = {
		glGetUniformLocation ( program, name )
	}
	def update ( loc : Int, v : Any ) = v match {
		case (texture : Texture, slot : Int) => {
			glActiveTexture ( GL_TEXTURE0 + slot )
			glBindTexture ( GL_TEXTURE_2D, texture.handle )
			glUniform1i ( loc, slot )
		}
		case vec3 ( x, y, z ) => glUniform3f ( loc, x, y, z )
		case vec2 ( x, y ) => glUniform2f ( loc, x, y )
		case vec4 ( x, y, z, w ) => glUniform4f ( loc, x, y, z, w )
		case x : Float => glUniform1f ( loc, x )
		case (x : Float, y : Float) => glUniform2f ( loc, x, y )
		case (x : Float, y : Float, z : Float) => glUniform3f ( loc, x, y, z )
		case (x : Float, y : Float, z : Float, w : Float) => glUniform4f ( loc, x, y, z, w )
		case x : Int => glUniform1i ( loc, x )
		case (x : Int, y : Int) => glUniform2i ( loc, x, y )
		case (x : Int, y : Int, z : Int) => glUniform3i ( loc, x, y, z )
		case (x : Int, y : Int, z : Int, w : Int) => glUniform4i ( loc, x, y, z, w )
		case Mat ( n, m, arr ) if n == m => n match {
			case 2 => glUniformMatrix2fv ( loc, 1 , true, arr ,0)
			case 3 => glUniformMatrix3fv (loc, 1 , true, arr ,0)
			case 4 =>
				glUniformMatrix4fv ( loc, 1 , false, arr ,0)
				//Log.w( "shader uniform" , arr.length.toString)
			case _ => throw new Exception ( "unsupported matrix size" )
		}

		case _ => throw new Exception ( "not implemented program.uniform match for" + v )
	}
	def update ( name : String, v : Any ) :Unit = update(uniform ( name ),v )
	def bind ( ) = {
		glUseProgram ( program )
	}
	def unbind ( ) = {
		glUseProgram ( 0 )
	}
	override def dispose ( ) : Unit = {
		glDeleteShader ( vs )
		glDeleteShader ( fs )
		glDeleteProgram ( program )
	}
}
