package main.scala.rendering.gl
import java.nio.{ByteBuffer, ByteOrder}
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10
import android.content.Context
import android.graphics.Bitmap
import android.opengl.GLES20._
import android.opengl.GLSurfaceView
import android.util.Log
import android.view.MotionEvent
import linalg.{ivec2, vec2, vec3, vec4}
import main.java.Natives
import main.scala.rendering.{Rect, Style}
import main.scala.social.graph._
import main.scala.utilities.DownloadImageTask
import utilities.Camera
import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer
/**
  * Created by anton on 12/19/2016.
  */
class MyGLSurfaceView ( implicit val context : Context ) extends GLSurfaceView ( context ) {
	implicit class IColor ( c : Int ) {
		def toVec4 = {
			vec4 (
				( c >> 24 ) & 0xff,
				( c >> 16 ) & 0xff,
				( c >> 8 ) & 0xff,
				c & 0xff
			) / 0xff
		}
	}
	setEGLContextClientVersion ( 2 )
	implicit val assets = context.getAssets
	val renderer = new MyGLRenderer ( )
	setRenderer ( renderer )
	var relationGraph : RelationGraph = null
	class MyGLRenderer extends GLSurfaceView.Renderer {
		lazy val circles_program = new Program (
			"""precision highp float;
			  |uniform vec4 color;
			  |uniform sampler2D texture;
			  |varying vec2 uv;
			  |void main()
			  |{
			  |//float alpha = length( uv - 0.5 ) < 0.5 ? 1.0 : 0.0;
			  |	gl_FragColor = color * texture2D( texture , uv );// * vec4( vec3(1) , alpha );
			  |}""".stripMargin,
			"""
			  |//uniform vec2 offset;
			  |//uniform vec2 scale;
			  |uniform mat4 viewproj;
			  |attribute vec2 position;
			  |attribute vec2 vertex_uv;
			  |varying vec2 uv;
			  |void main()
			  |{
			  |	uv = vertex_uv;//vec2( 0.5 , -0.5 ) * position + 0.5;
			  |	gl_Position = viewproj * vec4( position /* scale + offset*/ , 0.0 , 1.0 );
			  |}""".stripMargin
		)
		lazy val line_program = new Program (
			"""precision highp float;
			  |uniform vec4 color;
			  |void main()
			  |{
			  |	gl_FragColor = color;
			  |}""".stripMargin,
			"""
			  |uniform mat4 viewproj;
			  |attribute vec2 position;
			  |void main()
			  |{
			  |	gl_Position = viewproj * vec4( position , 0.0 , 1.0 );
			  |}""".stripMargin
		)
		override def onSurfaceCreated ( unused : GL10, config : EGLConfig ) : Unit = {
			Natives.init()
		}
		//lazy val atlas = new TextureAtlas ( 2048, 2048, 32 )
		var uv_queue = new ArrayBuffer[ (Int, Bitmap) ]( )
		def updateUVMapping ( person_id : Int, person_view_id : Int, lod : Float ) = {
			val image_ld = new DownloadImageTask ( bitmap => {
				renderer.synchronized {
					uv_queue += Tuple2 ( person_view_id, bitmap )
				}
			} )
			image_ld.execute ( relationGraph.getUsers ( person_id ).image_url ( 0 ) )
		}
		val uv_requests = ByteBuffer.allocateDirect ( 4 + 4 * 4 * 1024 * 512 ).order ( ByteOrder.LITTLE_ENDIAN )
		//var vertices = ByteBuffer.allocateDirect ( ( 8 + 8 ) * 6 * 1024 ).order ( ByteOrder.LITTLE_ENDIAN )
		//val edges = ByteBuffer.allocateDirect ( 128 * 16 * 1024 ).order ( ByteOrder.LITTLE_ENDIAN )
		val relation_map = new mutable.HashMap[(Person,Person),Int]()

		override def onDrawFrame ( unused : GL10 ) : Unit = {
			if ( relationGraph != null ) {
				/*glClearColor ( 1, 1, 1, 1 )
				glClearDepthf ( 1 )
				glClear ( GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT )
				glViewport ( 0, 0, getWidth, getHeight )*/
				var newPesons : Seq[ Person ] = null
				var newPairs : Seq[ (Person,Person)] = null
				socialSubscriber synchronized {
					newPesons = socialSubscriber.persons_added.clone ( )
					socialSubscriber.persons_added.clear ( )
					newPairs = socialSubscriber.pairs_added.clone ( )
					socialSubscriber.pairs_added.clear ( )
				}
				newPesons.foreach ( u => if( u.view_id < 0 ) u.view_id = Natives.createView ( u.id ) )
				newPairs.foreach( r => relation_map.getOrElseUpdate(r,Natives.createRelationView(r._1.view_id,r._2.view_id)))

				var new_uv_queue = new ArrayBuffer[ (Int, Bitmap) ]( )
				renderer.synchronized {
					val old_uv_queue = uv_queue
					uv_queue = new_uv_queue
					new_uv_queue = old_uv_queue
				}
				val uv_responses = ByteBuffer.allocateDirect ( 4 + 4 * 4 * new_uv_queue.length ).order ( ByteOrder.LITTLE_ENDIAN )
				uv_responses putInt new_uv_queue.length
				new_uv_queue.foreach ( p => {
					val texture = Texture( p._2 )
					uv_responses putInt p._1 putInt texture.handle putInt texture.width putInt texture.height
				} )
				uv_requests.position ( 0 )
				uv_requests putInt (0, 0)

				Natives.render ( cam_point.x, cam_point.y, cam_z,getWidth , getHeight , uv_requests, uv_responses )

				/*edges.position( 0 )
				edges.limit( socialSubscriber.pairs_count * 16 )
				val edge_buffer = new VertexBuffer ( edges, GL_STATIC_DRAW, Attribute ( 0, 2, GL_FLOAT, false ) )
				line_program.bind ( )
				edge_buffer.bind ( )
				line_program ( "viewproj" ) = viewproj
				line_program ( "color" ) = 0x000000FF.toVec4
				glViewport ( 0, 0, getWidth, getHeight )
				glDrawArrays ( GL_LINES, 0, socialSubscriber.pairs_count * 2 )
				edge_buffer.unbind ( )
				edge_buffer.dispose ( )

				vertices.position( 0 )
				vertices.limit(( 8 + 8 ) * 6 * socialSubscriber.persons_count)
				val vertex_buffer = new VertexBuffer ( vertices, GL_STATIC_DRAW, Attribute ( 0, 2, GL_FLOAT, false ), Attribute ( 1, 2, GL_FLOAT, false ) )
				vertex_buffer.bind ( )
				circles_program.bind ( )
				circles_program ( "viewproj" ) = viewproj
				circles_program ( "color" ) = 0xFFFFFFFF.toVec4
				circles_program ( "texture" ) = (atlas.attached_texture, 0)
				glViewport ( 0, 0, getWidth, getHeight )
				glDrawArrays ( GL_TRIANGLES, 0, relationGraph.getUsers.length * 6 )
				vertex_buffer.unbind ( )
				vertex_buffer.dispose ( )
				atlas.draw ( )*/
				val requests_count = uv_requests.getInt
				//Log.w ( "REQUESTS COUNT", requests_count.toString )
				for ( i <- 0 until requests_count ) {
					val person_id = uv_requests.getInt
					val person_view_id = uv_requests.getInt
					val lod = uv_requests.getFloat
					updateUVMapping ( person_id, person_view_id, lod )
				}
			}
		}
		override def onSurfaceChanged ( unused : GL10, width : Int, height : Int ) : Unit = {
			glViewport ( 0, 0, width, height )
		}
	}
	class SocialSubscriber extends Subscriber {
		var persons_count = 0
		var pairs_count = 0
		val persons_added = new ArrayBuffer[Person]()
		val pairs_added = new ArrayBuffer[(Person,Person)]()
		override def onPersonAdded ( person : Person ) : Unit = this synchronized {
			persons_added += person
			persons_count += 1
		}
		override def onRelationRemoved ( person : Person, person1 : Person ) : Unit = {}
		override def onPersonRemoved ( person : Person ) : Unit = {}
		override def onRelationAdded ( person : Person, person1 : Person ) : Unit = this synchronized {
			pairs_added += Tuple2( person , person1 )
			pairs_count += 1
		}
	}
	val socialSubscriber = new SocialSubscriber
	def linkModel ( relationGraph : RelationGraph ) = {
		this.relationGraph = relationGraph
		relationGraph.addSubscriber(socialSubscriber)

		//relationGraphView = new RelationGraphView ( relationGraph )
		//layouter = new Layouter ( relationGraphView )
	}
	var cam_point = vec2 ( 0.0f, 0.0f )
	var cam_z = 50.0f
	var last_mpos0 = vec2 ( 0.0f, 0.0f )
	var last_mpos1 = vec2 ( 0.0f, 0.0f )
	var last_avg = vec2 ( 0.0f, 0.0f )
	var last_dist = 0.0f
	var finger_id0 = -1
	var finger_id1 = -1
	override def onTouchEvent ( e : MotionEvent ) : Boolean = {
		def mpos ( id : Int ) = vec2 ( e.getX ( e.findPointerIndex ( id ) ) / getHeight, e.getY ( e.findPointerIndex ( id ) ) / getHeight ) * 2 - vec2 ( 1, 1 )
		/*Log.w ( "Input",
			"finger_id0:" + finger_id0.toString + " "
				+ "finger_id1:" + finger_id1.toString + " "
				+ e.getPointerId ( e.getActionIndex ).toString + " "
				+ MotionEvent.actionToString(e.getActionMasked)
		)*/
		e.getActionMasked match {
			case MotionEvent.ACTION_DOWN =>
				if ( finger_id0 < 0 ) {
					finger_id0 = e.getPointerId ( e.getActionIndex )
					last_mpos0 = mpos ( finger_id0 )
					last_avg = last_mpos0
					last_dist = 0.0f
				}
			case MotionEvent.ACTION_POINTER_UP | MotionEvent.ACTION_UP | MotionEvent.ACTION_CANCEL =>
				if ( finger_id1 == e.getPointerId ( e.getActionIndex ) ) {
					finger_id1 = -1
					last_avg = last_mpos0
					last_dist = 0.0f
				} else if ( finger_id0 == e.getPointerId ( e.getActionIndex ) ) {
					finger_id0 = finger_id1
					finger_id1 = -1
					last_avg = last_mpos1
					last_mpos0 = last_mpos1
					last_dist = 0.0f
				}
			case MotionEvent.ACTION_POINTER_DOWN =>
				if ( finger_id1 < 0 ) {
					finger_id1 = e.getPointerId ( e.getActionIndex )
					last_mpos1 = mpos ( finger_id1 )
					last_avg = ( last_mpos1 + last_mpos0 ) * 0.5f
					last_dist = ( last_mpos1 - last_mpos0 ).mod
				}
			case MotionEvent.ACTION_MOVE =>
				for ( i <- 0 until e.getPointerCount ) {
					if ( finger_id0 == e.getPointerId ( i ) ) {
						val mp = mpos ( finger_id0 )
						val mp1 = if ( finger_id1 >= 0 ) {
							last_mpos1
						} else {
							mp
						}
						val avg = ( mp1 + mp ) * 0.5f
						val dist = ( mp1 - mp ).mod
						val delta = avg - last_avg
						cam_z -= ( dist - last_dist ) * Math.exp ( cam_z * 0.01f ).toFloat * 10.0f
						cam_point += delta * cam_z
						last_mpos0 = mp
						last_avg = avg
						last_dist = dist
					} else if ( finger_id1 == e.getPointerId ( i ) ) {
						val mp = mpos ( finger_id1 )
						val mp1 = last_mpos0
						val avg = ( mp1 + mp ) * 0.5f
						val dist = ( mp1 - mp ).mod
						val delta = avg - last_avg
						cam_z -= ( dist - last_dist ) * Math.exp ( cam_z * 0.01f ).toFloat * 10.0f
						cam_point += delta * cam_z
						last_mpos1 = mp
						last_avg = avg
						last_dist = dist
					}
				}
				cam_z = Math.min ( 500.0f, Math.max ( 1.0f, cam_z ) )
			case _ =>
		}
		true
	}
}