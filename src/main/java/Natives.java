package main.java;


import java.nio.ByteBuffer;

/**
 * Created by anton on 12/20/2016.
 */

public class Natives
{
	static
	{
		System.loadLibrary("natives");
	}
	static public native void render( float x , float y , float z , int width , int height , ByteBuffer requests_buf , ByteBuffer responses_buf );
	static public native void init();
	static public native int createView( int person_id );
	static public native int createRelationView( int person_id0 , int person_id1 );
}
