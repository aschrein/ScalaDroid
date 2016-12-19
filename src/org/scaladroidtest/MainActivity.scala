package org.scaladroidtest

import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

import android.content.{Context, Intent}
import android.opengl.GLES20._
import android.opengl.GLSurfaceView
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View.OnClickListener
import android.view.{MotionEvent, View}
import android.widget.{Button, TextView}
import com.vk.sdk.api.VKError
import com.vk.sdk.{VKAccessToken, VKCallback, VKSdk}
import linalg._
import main.scala.gl.RendererGL
import main.scala.rendering._
import org.scaladroidtest.R
class MainActivity extends AppCompatActivity {
	implicit val context = this
	lazy val text_view : TextView = findViewById ( R.id.textView ).asInstanceOf [ TextView ]

	override def onCreate ( savedInstanceState : Bundle ) : Unit = {
		super.onCreate ( savedInstanceState )
		//val vh : TypedViewHolder.main = TypedViewHolder.setContentView ( this, TR.layout.main )
		setContentView ( R.layout.main ) //new MyGLSurfaceView )
		val button = findViewById ( R.id.login_button ).asInstanceOf [ Button ]

		//VKUIHelper.onCreate( this )
		VKSdk.login ( context )
		button.setOnClickListener ( new OnClickListener {
			override def onClick ( v : View ) : Unit = {



			}
		} )
		//button.setOnClickListener( e => {} )
	}
	override def onActivityResult ( requestCode : Int, resultCode : Int, data : Intent ) = {
		//VKUIHelper.onActivityResult(this, requestCode, resultCode, data);
		if ( !VKSdk.onActivityResult ( requestCode, resultCode, data, new VKCallback[ VKAccessToken ]( ) {
			override def onResult ( res : VKAccessToken ) = {
				text_view.setText ( "Success" )
			}
			override def onError ( error : VKError ) = {
				text_view.setText ( "Fail" )
			}
		} ) ) {
			super.onActivityResult ( requestCode, resultCode, data )
		}
	}
	override def onDestroy() ={
		super.onDestroy()
		//VKUIHelper.onDestroy(this)
	}
}
class MyGLSurfaceView ( implicit val context : Context ) extends GLSurfaceView ( context ) {
	setEGLContextClientVersion ( 2 )
	implicit val assets = context.getAssets
	val renderer = new MyGLRenderer ( )
	setRenderer ( renderer )
	var mpos = vec2 ( 0, 0 )
	class MyGLRenderer extends GLSurfaceView.Renderer {
		var renderer : RendererGL = null
		override def onSurfaceCreated ( unused : GL10, config : EGLConfig ) : Unit = {
			renderer = new RendererGL
		}
		override def onDrawFrame ( unused : GL10 ) : Unit = {
			renderer render ( Rect ( mpos, vec2 ( 0.2f, 0.2f ), Style ( color = 0x7BE8D0FF, shadow_size = 0.01f ) ) :: Nil )
		}
		override def onSurfaceChanged ( unused : GL10, width : Int, height : Int ) : Unit = {
			glViewport ( 0, 0, width, height )
		}
	}
	override def onTouchEvent ( e : MotionEvent ) : Boolean = {
		mpos = vec2 ( e.getX / getWidth, -e.getY / getHeight ) * 2 - vec2 ( 1, -1 )
		true
	}
}