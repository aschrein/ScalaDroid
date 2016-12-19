package main.scala.gl
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10
import android.content.Context
import android.graphics.Bitmap
import android.opengl.GLES20._
import android.opengl.GLSurfaceView
import android.view.MotionEvent
import linalg.{vec2, vec3}
import main.scala.rendering.{Rect, Style}
import utilities.Camera
import scala.collection.mutable.ArrayBuffer
/**
  * Created by anton on 12/19/2016.
  */
class MyGLSurfaceView ( implicit val context : Context ) extends GLSurfaceView ( context ) {
	setEGLContextClientVersion ( 2 )
	implicit val assets = context.getAssets
	val renderer = new MyGLRenderer ( )
	setRenderer ( renderer )
	var nodes = new ArrayBuffer[ (vec2, Texture) ]( )
	val queue = new ArrayBuffer[(vec2, Bitmap)]()
	class MyGLRenderer extends GLSurfaceView.Renderer {
		var renderer : RendererGL = null
		override def onSurfaceCreated ( unused : GL10, config : EGLConfig ) : Unit = {
			renderer = new RendererGL
		}
		override def onDrawFrame ( unused : GL10 ) : Unit = {
			queue.foreach ( x => {
				nodes += Tuple2(x._1, Texture( x._2 ))
			} )
			queue.clear()
			val viewproj = Camera.perspLook (
				vec3 ( -2.0f, -2.0f, 2.0f ),
				vec3 ( 0.0f, 0.0f, 0.0f ),
				vec3 ( 0.0f, 0.0f, 1.0f ),
				1.0f, 1.0f, 0.01f, 10000.0f
			)
			renderer render (viewproj, nodes.map ( x => {
				Rect ( x._1, vec2 ( 0.2f, 0.2f ), Style ( color = 0xFFFFFFFF, texture = x._2 ) )
			} ))
		}
		override def onSurfaceChanged ( unused : GL10, width : Int, height : Int ) : Unit = {
			glViewport ( 0, 0, width, height )
		}
	}
	def addNode ( img : Bitmap ) = {
		val r = Math.sqrt( Math.random() * 10 ).toFloat
		val phi = ( Math.random() * Math.PI ).toFloat * 2
		queue += Tuple2 ( vec2 ( Math.cos(phi).toFloat , Math.sin(phi).toFloat ) * r, img )
	}
	override def onTouchEvent ( e : MotionEvent ) : Boolean = {
		//mpos = vec2 ( e.getX / getWidth, -e.getY / getHeight ) * 2 - vec2 ( 1, -1 )
		true
	}
}