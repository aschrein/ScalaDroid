package org.scaladroidtest
import java.io.BufferedInputStream
import java.net.URL
import java.nio.ByteBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

import android.app.Activity
import android.content.{Context, Intent}
import android.graphics.{Bitmap, BitmapFactory}
import android.opengl.GLES20._
import android.opengl.GLSurfaceView
import android.os.{AsyncTask, Bundle}
import main.java.Natives
import main.scala.social.graph.RelationGraph
import main.scala.vkapi.SocialMapper
import utilities.Camera
//import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View.OnClickListener
import android.view.{MotionEvent, View}
import android.widget._
import com.vk.sdk.api.VKRequest.VKRequestListener
import com.vk.sdk.api._
import com.vk.sdk.{VKAccessToken, VKCallback, VKSdk}
import linalg._
import main.java.{BitmapCallback, DownloadTask}
import main.scala.rendering.gl.{MyGLSurfaceView, RendererGL}
import main.scala.rendering._
import org.json.JSONObject
import org.scaladroidtest.R
import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer
class MainActivity extends Activity {
	implicit val context = this
	lazy val text_view : TextView = findViewById ( R.id.textView ).asInstanceOf [ TextView ]
	lazy val graphics_view = new MyGLSurfaceView
	//Log.w("NATIVE!!!!",Natives.getNum().toString)
	val social_graph = new RelationGraph
	override def onCreate ( savedInstanceState : Bundle ) : Unit = {
		super.onCreate ( savedInstanceState )
		setContentView(graphics_view)
		//setContentView ( R.layout.main )
		//context.findViewById ( R.id.graph_container ).asInstanceOf [ FrameLayout ].addView ( graphics_view )
		SocialMapper.init ( this )
		SocialMapper.mapUser ( social_graph )
		graphics_view.linkModel ( social_graph )

	}
	/*override def onActivityResult ( requestCode : Int, resultCode : Int, data : Intent ) = {
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
	}*/
	override def onDestroy ( ) = {
		super.onDestroy ( )
	}
}
