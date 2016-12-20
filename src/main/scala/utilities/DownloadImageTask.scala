package main.scala.utilities
import java.io.InputStream

import android.graphics.{Bitmap, BitmapFactory}
import main.java.DownloadTask

/**
  * Created by anton on 12/20/2016.
  */
class DownloadImageTask[ T ]( callback : Bitmap => T ) extends DownloadTask[ Bitmap ] {
	protected override def onPostExecute ( result : Bitmap ) {
		callback ( result )
	}
	override def process( inputStream: InputStream ) : Bitmap = BitmapFactory.decodeStream ( inputStream )
}
