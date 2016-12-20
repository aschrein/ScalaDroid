package main.scala.social.graph
import android.graphics.Bitmap
import linalg.{Mat, vec2}
import main.java.{BitmapCallback, DownloadTask}
import main.scala.events.InputEvent
import main.scala.utilities.DownloadImageTask

import scala.collection.mutable
import scala.ref.WeakReference
/**
  * Created by anton on 12/20/2016.
  */
case class PersonView( person: WeakReference[Person], var pos : vec2 = vec2( 0.0f , 0.0f ), var bitmap: Option[ Bitmap ] = None )
class RelationGraphView( val relation_graph: RelationGraph ) {
	private val views = mutable.WeakHashMap.empty[ Person , PersonView ]
	private var viewproj : Mat = Mat.Mat4Identity
	def getView( person: Person ) : PersonView = {
		val view = views.get(person)
		view match {
			case None =>
				val r = Math.sqrt( Math.random() * 10 ).toFloat
				val phi = ( Math.random() * Math.PI ).toFloat * 2
				val new_view = new PersonView( new WeakReference(person) , vec2 ( Math.cos(phi).toFloat , Math.sin(phi).toFloat ) * r )
				val image_ld = new DownloadImageTask( bitmap => new_view.bitmap = Some( bitmap ) )
				views.put( person , new_view )
				image_ld.execute( person.image_url )
				new_view
			case Some( _ ) => view.get
		}
	}
	def setViewProj( viewproj : Mat ) = this.viewproj = viewproj
	def viewProj = viewproj.copy()
}