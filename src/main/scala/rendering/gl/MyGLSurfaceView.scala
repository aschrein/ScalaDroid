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
import linalg.{vec2, vec3, vec4}
import main.scala.rendering.{Rect, Style}
import main.scala.social.graph.{Layouter, PersonView, RelationGraph, RelationGraphView}
import utilities.Camera

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer
/**
  * Created by anton on 12/19/2016.
  */
class MyGLSurfaceView ( implicit val context : Context ) extends GLSurfaceView ( context ) {
	implicit class IColor( c : Int ) {
		def toVec4 = {
			vec4(
				( c >> 24 ) & 0xff ,
				( c >> 16 ) & 0xff ,
				( c >> 8 ) & 0xff ,
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
			  |float alpha = length( uv - 0.5 ) < 0.5 ? 1.0 : 0.0;
			  |	gl_FragColor = color * texture2D( texture , uv ) * vec4( vec3(1) , alpha );
			  |}""".stripMargin,
			"""
			  |uniform vec2 offset;
			  |uniform vec2 scale;
			  |uniform mat4 viewproj;
			  |attribute vec2 position;
			  |varying vec2 uv;
			  |void main()
			  |{
			  |	uv = vec2( 0.5 , -0.5 ) * position + 0.5;
			  |	gl_Position = viewproj * vec4( position * scale + offset , 0.0 , 1.0 );
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
		var renderer : RendererGL = null

		override def onSurfaceCreated ( unused : GL10, config : EGLConfig ) : Unit = {
			renderer = new RendererGL

		}
		val textures = new mutable.WeakHashMap[Bitmap,Texture]
		override def onDrawFrame ( unused : GL10 ) : Unit = {
			if ( relationGraphView != null ) {
				val viewproj = Camera.perspLook (
					vec3( cam_point.x , cam_point.y , 2.0f ),
					vec3( cam_point.x , cam_point.y , 0.0f ),/// + vec3 ( 2.0f , 2.0f , 2.0f ),
					vec3 ( 0.0f, 1.0f, 0.0f ),
					1.0f, getHeight.toFloat/getWidth, 0.01f, 10000.0f
				).T
				glClearColor ( 1, 1, 1, 1 )
				glClearDepthf ( 1 )
				glClear ( GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT )
				//val sorted_draw_list = draw_list.sortWith ( _.style.z > _.style.z )
				val relations = relationGraphView.relation_graph.getRelations
				//Log.w( "RELATIONS COUNT" , relations.length.toString )
				/*val edges = ByteBuffer.allocate ( 16 * relations.length ).order ( ByteOrder.LITTLE_ENDIAN )
				//layouter.tick()
				relations foreach ( r => {
					val p0 = relationGraphView.getView( r._1 )
					val p1 = relationGraphView.getView( r._2 )
					edges putFloat p0.pos.x putFloat p0.pos.y
					edges putFloat p1.pos.x putFloat p1.pos.y
				} )
				edges.rewind ( )
				val edge_buffer = new VertexBuffer ( edges, GL_STATIC_DRAW, Attribute ( 0, 2, GL_FLOAT, false ) )
				line_program.bind()
				edge_buffer.bind()
				line_program ( "viewproj" ) = viewproj
				line_program ( "color" ) = 0x000000FF.toVec4
				glDrawArrays ( GL_LINES , 0, relations.length * 2 )
				edge_buffer.dispose()*/
				circles_program.bind ( )
				circles_program ( "viewproj" ) = viewproj
				val vertices = ByteBuffer.allocate ( 16 * relations.length ).order ( ByteOrder.LITTLE_ENDIAN )
				//layouter.tick()
				relations foreach ( r => {
					val p0 = relationGraphView.getView( r._1 )
					val p1 = relationGraphView.getView( r._2 )
					edges putFloat p0.pos.x putFloat p0.pos.y
					edges putFloat p1.pos.x putFloat p1.pos.y
				} )
				edges.rewind ( )
				val edge_buffer = new VertexBuffer ( edges, GL_STATIC_DRAW, Attribute ( 0, 2, GL_FLOAT, false ) )
				line_program.bind()
				edge_buffer.bind()
				line_program ( "viewproj" ) = viewproj
				line_program ( "color" ) = 0x000000FF.toVec4
				glDrawArrays ( GL_LINES , 0, relations.length * 2 )
				edge_buffer.dispose()
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
		//layouter = new Layouter( relationGraphView )
	}
	var cam_point = vec2( -2.0f , -2.0f )
	var last_mpos = vec2( 0.0f , 0.0f )
	override def onTouchEvent ( e : MotionEvent ) : Boolean = {
		val mpos = vec2 ( e.getX / getHeight, e.getY / getHeight ) * 2 - vec2 ( 1, 1 )
		e.getActionMasked match {
			case MotionEvent.ACTION_DOWN =>
				last_mpos = mpos
			case MotionEvent.ACTION_UP =>
			case MotionEvent.ACTION_MOVE =>
				val delta = mpos - last_mpos
				cam_point += delta * 2
				last_mpos = mpos
			case _ =>
		}
		true
	}
}