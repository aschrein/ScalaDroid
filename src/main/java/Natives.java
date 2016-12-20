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
	static public native int tickForces(
			float size , int point_count , int pair_count ,
			ByteBuffer points , ByteBuffer pairs );

}
