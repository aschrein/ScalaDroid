//
// Created by anton on 12/20/2016.
//Java_main_java_JNITest_getNum
#include <jni.h>
#include <malloc.h>
#include <string.h>
#include <cmath>
/*struct QuadNode
{
	int index;
	float x , y , size;
};
struct QuadTree
{
	QuadTree *children[ 4 ] = { nullptr };
	QuadNode *items = nullptr;
	size_t items_count = 0 , items_capacity = 0;
	float x , y , size;
	int depth;
	QuadTree( float x , float y , float size , int depth ) :
		x( x ) ,
		y( y ) ,
		size( size ) ,
		depth( depth )
	{}
	void dispose()
	{
		if( children[ 0 ] )
		{
			for( int i = 0; i < 4; i++ )
			{
				children[ i ]->dispose();
			}
		}
		if( items )
		{
			free( items );
		}
		items_count = 0;
	}
	bool collide( float x , float y , float size )
	{
		return fabsf( this->x - x ) < size && fabsf( this->y - y ) < size;
	}
	void addItem( int index , float x , float y , float size , int MAX_ITEMS = 0x10 , int MAX_DEPTH = 8 )
	{
		if( children[ 0 ] )
		{

		} else
		{
			if( !items )
			{
				items = ( QuadNode* )malloc( sizeof( QuadNode ) * MAX_ITEMS );

			}
		}
	}
};*/
#define MAX_ITEMS 4
struct Node
{
	int ids[ MAX_ITEMS ];
};
struct NodeMatrix
{
	Node *matrix;
	float width , height , size , pos_x , pos_y;
	int row_size;
	int col_size;
	~NodeMatrix()
	{
		delete[] matrix;
	}
	NodeMatrix( float x , float y , float width , float height , float size ) :
		pos_x( x ) ,
		pos_y( y ) ,
		width( width ) ,
		height( height ) ,
		size( size )
	{
		row_size = int( ( width + size / 2 ) / size );
		col_size = int( ( height + size / 2 ) / size );
		matrix = new Node[ row_size * col_size ];
		memset( matrix , 0 , row_size * col_size * sizeof( Node ) );
	}
	void put( int index , float x , float y )
	{
		int row = int( ( y - pos_y ) / size );
		int col = int( ( x - pos_x ) / size );
		Node &node = matrix[ row * row_size + col ];
		int i;
		for( i = 0; i < MAX_ITEMS; i++ )
		{
			if( node.ids[ i ] == 0 )
			{
				break;
			}
		}
		if( i < MAX_ITEMS )
		{
			node.ids[ i ] = index;
		}
	}
	int getItems( int *out , float x , float y , float radius )
	{
		int cells = int( ( radius + size / 2 ) / size );
		int row = int( ( y - pos_y ) / size );
		int col = int( ( x - pos_x ) / size );
		int item_counter = 0;
		for( int i = row - cells; i <= row + cells; i++ )
		{
			if( i > 0 && i < col_size )
			{
				for( int j = col - cells; j <= col + cells; j++ )
				{
					if( j > 0 && j < row_size )
					{
						Node &node = matrix[ i * row_size + j ];
						for( int k = 0; k < MAX_ITEMS; k++ )
						{
							if( node.ids[ i ] == 0 )
							{
								break;
							} else
							{
								out[ item_counter++ ] = node.ids[ i ];
							}
						}
					}
				}
			}
		}
		return item_counter;
	}
};
float pushForce( float x )
{
	return -1.0f / ( 1.0f + x );
}
float pullForce( float x )
{
	return fmin( 1.0f , x );
}
void tickForce( float size , int point_count , int pair_count , float *points , int *pairs )
{
	float min_x = 0.0f , min_y = 0.0f , max_x = 0.0f , max_y = 0.0f;
	/*for( int i = 0; i < point_count; i++ )
	{
		float x = points[ i * 2 ];
		float y = points[ i * 2 + 1 ];
		min_x = fmin( min_x , x );
		min_y = fmin( min_y , y );
		max_x = fmax( max_x , x );
		max_y = fmax( max_y , y );
	}
	NodeMatrix matrix( min_x , min_y , max_x - min_x , max_y - min_y , size );*/
	for( int i = 0; i < pair_count; i++ )
	{
		int id0 = pairs[ i * 2 ];
		int id1 = pairs[ i * 2 + 1 ];
		float x0 = points[ id0 * 2 ];
		float y0 = points[ id0 * 2 + 1 ];
		float x1 = points[ id1 * 2 ];
		float y1 = points[ id1 * 2 + 1 ];
		float dx = x1 - x0;
		float dy = y1 - y0;
		float dist =( dx * dx + dy * dy );
		if( __isfinitef( dist ) )
		{
			dist = sqrtf( dist );
			dx /= dist;
			dy /= dist;
			float force = pushForce( dist ) * 0.01f;
			points[ id0 * 2 ] = x0 + dx * force;
			points[ id0 * 2 + 1 ] = y0 - dy * force;
			points[ id1 * 2 ] = x1 - dx * force;
			points[ id1 * 2 + 1 ] = y1 - dy * force;
		}
	}
}
extern "C" {
	JNIEXPORT void JNICALL Java_main_java_JNITest_getNum(
		JNIEnv* env , jclass clazz ,
		jfloat size , jint point_count , jint pair_count ,
		jobject points_buf , jobject pairs_buf )
	{
		float *points = ( float* )env->GetDirectBufferAddress( points_buf );
		int *pairs = ( int * )env->GetDirectBufferAddress( pairs_buf );
		tickForce( size , point_count , pair_count , points , pairs );
	}
}