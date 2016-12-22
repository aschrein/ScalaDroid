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
	static public native void renderVertexBuffer(
			ByteBuffer uv_mappings_buf , ByteBuffer out_rects_buf , ByteBuffer out_edges_buf
			, ByteBuffer uv_requests_buf , ByteBuffer uv_responses_buf );
	static public native int createView( int person_id );
	static public native int createRelationView( int person_id0 , int person_id1 );
}
