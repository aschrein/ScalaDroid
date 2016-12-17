package org.scaladroidtest
import java.nio.{ByteBuffer, ByteOrder}
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

import android.app.Activity
import android.content.Context
import android.content.res.AssetManager
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.graphics.drawable.Animatable
import android.opengl.{GLES20, GLSurfaceView}
import android.opengl.GLES20._
import linalg._
import main.scala.gl.{Program, RendererGL, VertexBuffer}
import main.scala.rendering._
class MainActivity extends AppCompatActivity {
	implicit val context = this
	override def onCreate ( savedInstanceState : Bundle ) : Unit = {
		super.onCreate ( savedInstanceState )
		//val vh : TypedViewHolder.main = TypedViewHolder.setContentView ( this, TR.layout.main )
		setContentView ( new MyGLSurfaceView )
	}
}
class MyGLRenderer( implicit assets : AssetManager) extends GLSurfaceView.Renderer {
	var renderer : RendererGL = null
	override def onSurfaceCreated ( unused : GL10, config : EGLConfig ) : Unit = {
		renderer = new RendererGL
	}
	override def onDrawFrame ( unused : GL10 ) : Unit = {
		renderer render ( Rect ( vec2 ( 0, 0 ), vec2 ( 0.2f, 0.2f ), Style ( color = 0xffffffff , shadow_size = 0.01f ) ) :: Nil )
	}
	override def onSurfaceChanged ( unused : GL10, width : Int, height : Int ) : Unit = {
		glViewport ( 0, 0, width, height )
	}
}
class MyGLSurfaceView ( implicit val context : Context ) extends GLSurfaceView ( context ) {
	setEGLContextClientVersion ( 2 )
	implicit val assets = context.getAssets
	val renderer = new MyGLRenderer ( )
	setRenderer ( renderer )
}