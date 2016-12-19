package main.java;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;
import android.widget.ImageView;

import java.io.InputStream;

/**
 * Created by anton on 12/19/2016.
 */
public class DownloadImageTask extends AsyncTask< String, Void, Bitmap >
{
	BitmapCallback callback;
	public DownloadImageTask( BitmapCallback callback )
	{
		this.callback = callback;
	}

	protected Bitmap doInBackground( String... urls )
	{
		String urldisplay = urls[ 0 ];
		Bitmap bitmap = null;
		try
		{
			InputStream in = new java.net.URL( urldisplay ).openStream( );
			bitmap = BitmapFactory.decodeStream( in );
		} catch( Exception e )
		{
			Log.e( "Error", e.getMessage( ) );
			e.printStackTrace( );
		}
		return bitmap;
	}

	protected void onPostExecute( Bitmap result )
	{
		callback.consume( result );
	}
}