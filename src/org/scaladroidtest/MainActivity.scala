package org.scaladroidtest
import java.io.BufferedInputStream
import java.net.URL
import java.nio.ByteBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

import android.content.{Context, Intent}
import android.graphics.{Bitmap, BitmapFactory}
import android.opengl.GLES20._
import android.opengl.GLSurfaceView
import android.os.{AsyncTask, Bundle}
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View.OnClickListener
import android.view.{MotionEvent, View}
import android.widget._
import com.vk.sdk.api.VKRequest.VKRequestListener
import com.vk.sdk.api._
import com.vk.sdk.{VKAccessToken, VKCallback, VKSdk}
import linalg._
import main.java.{BitmapCallback, DownloadImageTask}
import main.scala.gl.{MyGLSurfaceView, RendererGL}
import main.scala.rendering._
import org.json.JSONObject
import org.scaladroidtest.R

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer
class MainActivity extends AppCompatActivity {
	implicit val context = this
	lazy val text_view : TextView = findViewById ( R.id.textView ).asInstanceOf [ TextView ]
	lazy val graphics_view = new MyGLSurfaceView
	def downloadFriends( id : String = null ) : Unit = {
		val req = new VKRequest("friends.get",
			if( id != null ) VKParameters.from ( VKApiConst.FIELDS, "photo",VKApiConst.USER_ID, id )
			else VKParameters.from ( VKApiConst.FIELDS, "photo" ) )
		req.executeWithListener ( new VKRequestListener ( ) {
			override def onComplete ( response : VKResponse ) : Unit = {
				val json = new JSONObject ( response.responseString )
				val resp = json.getJSONObject ( "response" )
				val count = resp.getInt ( "count" )
				val items = resp.getJSONArray ( "items" )
				val printed = new java.lang.StringBuilder
				for ( i <- 0 until count ) {
					val item = items.getJSONObject ( i )
					val item_id = item.getString( "id" )
					val name = item.getString ( "first_name" )
					val avatar_url = item.getString ( "photo" )
					val image_ld = new DownloadImageTask( new BitmapCallback {
						override def consume ( bitmap : Bitmap ) : Unit = {
							graphics_view.addNode( bitmap )
						}
					} )
					image_ld.execute( avatar_url )
					if( id == null ) {
						downloadFriends( item_id )
					}
					printed append " { " append name append  " } "
				}
				text_view.setText ( response.responseString )
			}
			override def onError ( error : VKError ) {
				text_view.setText ( error.errorMessage )
			}
			override def attemptFailed ( request : VKRequest, attemptNumber : Int, totalAttempts : Int ) {
				text_view.setText ( "Attempt failed" )
			}
		} )
	}
	override def onCreate ( savedInstanceState : Bundle ) : Unit = {
		super.onCreate ( savedInstanceState )
		setContentView ( R.layout.main )
		context.findViewById(R.id.graph_container).asInstanceOf[FrameLayout].addView(graphics_view)
		val button = findViewById ( R.id.login_button ).asInstanceOf [ Button ]
		VKSdk.login ( context )
		button.setOnClickListener ( new OnClickListener {
			override def onClick ( v : View ) : Unit = {
				downloadFriends()
			}
		} )
	}
	override def onActivityResult ( requestCode : Int, resultCode : Int, data : Intent ) = {
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
	override def onDestroy ( ) = {
		super.onDestroy ( )
	}
}
