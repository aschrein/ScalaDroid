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
import main.scala.rendering.{Rect, Style}
import main.scala.social.graph.{Layouter, PersonView, RelationGraph, RelationGraphView}
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
	var relationGraphView : RelationGraphView = null
	var layouter : Layouter = null
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
		//var renderer : RendererGL = null
		override def onSurfaceCreated ( unused : GL10, config : EGLConfig ) : Unit = {
			//renderer = new RendererGL
		}
		val textures = new mutable.WeakHashMap[ Bitmap, Texture ]
		lazy val atlas = new TextureAtlas ( 2048, 2048, 32 )
		override def onDrawFrame ( unused : GL10 ) : Unit = {
			if ( relationGraphView != null ) {
				val viewproj = Camera.perspLook (
					vec3 ( cam_point.x, cam_point.y, cam_z ),
					vec3 ( cam_point.x, cam_point.y, 0.0f ), /// + vec3 ( 2.0f , 2.0f , 2.0f ),
					vec3 ( 0.0f, 1.0f, 0.0f ),
					1.0f, getHeight.toFloat / getWidth, 0.01f, 10000.0f
				).T
				glClearColor ( 1, 1, 1, 1 )
				glClearDepthf ( 1 )
				glClear ( GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT )
				glViewport ( 0, 0, getWidth, getHeight )
				//val sorted_draw_list = draw_list.sortWith ( _.style.z > _.style.z )
				val relations = relationGraphView.relation_graph.getRelations
				//Log.w( "RELATIONS COUNT" , relations.length.toString )
				val edges = ByteBuffer.allocate ( 16 * relations.length ).order ( ByteOrder.LITTLE_ENDIAN )

				layouter.tick ( )

				relations foreach ( r => {
					val p0 = relationGraphView.getView ( r._1 )
					val p1 = relationGraphView.getView ( r._2 )
					edges putFloat p0.pos.x putFloat p0.pos.y
					edges putFloat p1.pos.x putFloat p1.pos.y
				} )
				edges.rewind ( )
				val edge_buffer = new VertexBuffer ( edges, GL_STATIC_DRAW, Attribute ( 0, 2, GL_FLOAT, false ) )
				line_program.bind ( )
				edge_buffer.bind ( )
				line_program ( "viewproj" ) = viewproj
				line_program ( "color" ) = 0x000000FF.toVec4
				glViewport ( 0, 0, getWidth, getHeight )
				glDrawArrays ( GL_LINES, 0, relations.length * 2 )
				edge_buffer.unbind ( )
				edge_buffer.dispose ( )


				val views = relationGraphView.relation_graph.getUsers.map ( u => relationGraphView.getView ( u ) )
				views.foreach ( person_view =>
					person_view.bitmap match {
						case Some ( b ) =>
							(textures.getOrElseUpdate ( b, {
								val texture = Texture ( b )
								val alloc = atlas.allocate ( texture, ivec2 ( texture.width, texture.height ) )
								//Log.i( "ALLOC" , alloc.toString() )
								texture
							} ), 0)
						case None =>
					}
				)
				val vertices = ByteBuffer.allocate ( ( 8 + 8 ) * 6 * views.length ).order ( ByteOrder.LITTLE_ENDIAN )
				layouter.tick ( )
				views foreach ( v => {
					val pos = v.pos
					val size = 0.1f
					val mapping = v.bitmap match {
						case Some( b ) => textures.get(b) match {
							case Some( t ) => atlas.getMapping(t)
							case None => ( 1.0f , 1.0f , 0.0f , 0.0f )
						}
						case None => ( 1.0f , 1.0f , 0.0f , 0.0f )
					}
					vertices putFloat pos.x - size putFloat pos.y - size
					vertices putFloat mapping._1 putFloat mapping._2
					vertices putFloat pos.x - size putFloat pos.y + size
					vertices putFloat mapping._1 putFloat mapping._2 + mapping._4
					vertices putFloat pos.x + size putFloat pos.y + size
					vertices putFloat mapping._1 + mapping._3 putFloat mapping._2 + mapping._4
					vertices putFloat pos.x - size putFloat pos.y - size
					vertices putFloat mapping._1 putFloat mapping._2
					vertices putFloat pos.x + size putFloat pos.y + size
					vertices putFloat mapping._1 + mapping._3 putFloat mapping._2 + mapping._4
					vertices putFloat pos.x + size putFloat pos.y - size
					vertices putFloat mapping._1 + mapping._3 putFloat mapping._2
				} )
				vertices.rewind ( )
				val vertex_buffer = new VertexBuffer ( vertices, GL_STATIC_DRAW, Attribute ( 0, 2, GL_FLOAT, false ), Attribute ( 1, 2, GL_FLOAT, false ) )
				vertex_buffer.bind ( )
				circles_program.bind ( )
				circles_program ( "viewproj" ) = viewproj
				circles_program ( "color" ) = 0xFFFFFFFF.toVec4
				circles_program ( "texture" ) = (atlas.attached_texture, 0)
				glViewport ( 0, 0, getWidth, getHeight )
				glDrawArrays ( GL_TRIANGLES, 0, views.length * 6 )
				vertex_buffer.unbind ( )
				vertex_buffer.dispose ( )
				atlas.draw()
				/*rect_buffer.bind ( )
				relationGraphView.relation_graph.getUsers.map ( u => relationGraphView.getView ( u ) ) foreach( person_view => {
					circles_program ( "offset" ) = person_view.pos
					circles_program ( "scale" ) = vec2( 0.2f , 0.2f )
					circles_program ( "color" ) = 0xFFFFFFFF.toVec4
					person_view.bitmap match {
						case Some( b ) =>
							circles_program ( "texture" ) = ( textures.getOrElseUpdate( b , Texture( b ) ) , 0)
						case None =>
					}
					glDrawArrays ( GL_TRIANGLE_FAN, 0, 4 )
				} )*/
			}
		}
		override def onSurfaceChanged ( unused : GL10, width : Int, height : Int ) : Unit = {
			glViewport ( 0, 0, width, height )
		}
	}
	def linkModel ( relationGraph : RelationGraph ) = {
		relationGraphView = new RelationGraphView ( relationGraph )
		layouter = new Layouter ( relationGraphView )
	}
	var cam_point = vec2 ( 0.0f, 0.0f )
	var cam_z = 2.0f
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
						cam_z -= ( dist - last_dist ) * Math.exp ( cam_z * 0.1f ).toFloat
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
						cam_z -= ( dist - last_dist ) * Math.exp ( cam_z * 0.1f ).toFloat
						cam_point += delta * cam_z
						last_mpos1 = mp
						last_avg = avg
						last_dist = dist
					}
				}
				cam_z = Math.min ( 20.0f, Math.max ( 1.0f, cam_z ) )
			case _ =>
		}
		true
	}
}