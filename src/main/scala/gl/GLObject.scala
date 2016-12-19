package main.scala.gl
import java.io.{BufferedReader, IOException, InputStreamReader}
import java.util.logging.{Level, Logger}
import android.content.Context
import android.content.res.AssetManager
/**
  * Created by anton on 12/9/2016.
  */
object GL {
	def using[ O <: GLObject, T ] ( obj : O )( body : O => T ) = if ( obj != null ) {
		obj.bind ( )
		body ( obj )
		obj.unbind ( )
	}
	def using[ T ] ( objects : GLObject* )( body : Seq[ GLObject ] => T ) = if ( !objects.contains ( null ) ) {
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
					Logger.getGlobal.log ( Level.WARNING, x.getLocalizedMessage )
			} finally {
				if ( reader != null ) {
					try {
						reader.close ( )
					} catch {
						case x : IOException =>
							Logger.getGlobal.log ( Level.WARNING, x.getLocalizedMessage )
					}
				}
			}
			""
		}
	}
}
trait GLObject {
	def dispose ( ) : Unit
	def bind ( ) : Unit
	def unbind ( ) : Unit
}
