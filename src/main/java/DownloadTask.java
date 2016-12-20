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
abstract public class DownloadTask< T > extends AsyncTask< String, Void, T >
{
	protected T doInBackground( String... urls )
	{
		String urldisplay = urls[ 0 ];
		T out = null;
		try
		{
			InputStream in = new java.net.URL( urldisplay ).openStream( );
			out = process( in );
		} catch( Exception e )
		{
			Log.e( "Error", e.getMessage( ) );
			e.printStackTrace( );
		}
		return out;
	}
	public abstract T process( InputStream istream );
}