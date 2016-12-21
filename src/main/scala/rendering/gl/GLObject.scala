package main.scala.rendering.gl
import java.io.{BufferedReader, IOException, InputStreamReader}

import android.content.Context
import android.content.res.AssetManager
import android.util.Log
/**
  * Created by anton on 12/9/2016.
  */
object GL {
	def using[ O <: GLBindable, T ] ( obj : O )( body : O => T ) = if ( obj != null ) {
		obj.bind ( )
		body ( obj )
		obj.unbind ( )
	}
	def using[ T ] ( objects : GLBindable* )( body : Seq[ GLBindable ] => T ) = if ( !objects.contains ( null ) ) {
		objects.foreach ( o => o.bind ( ) )
		body ( objects )
		objects.foreach ( o => o.unbind ( ) )
	}
	implicit class Asset ( name : String )( implicit assets : AssetManager ) {
		def load : String = {
			var reader : BufferedReader = null
			try {
				reader = new BufferedReader ( new InputStreamReader ( assets.open ( name ), "UTF-8" ) )
				val stream = new StringBuilder ( )
				var line : String = null
				def appendLine() : Unit = reader.readLine match {
					case null =>
					case l : String =>
						stream.append ( l ).append ("\n" )
						appendLine()
				}
				appendLine()
				return stream.toString
			} catch {
				case x : IOException =>
					Log.w ( this.getClass.getSimpleName, x.getLocalizedMessage )
			} finally {
				if ( reader != null ) {
					try {
						reader.close ( )
					} catch {
						case x : IOException =>
							Log.w ( this.getClass.getSimpleName, x.getLocalizedMessage )
					}
				}
			}
			""
		}
	}
}
trait GLObject {
	def dispose ( ) : Unit
}
trait GLBindable extends GLObject {
	def bind ( ) : Unit
	def unbind ( ) : Unit
}
