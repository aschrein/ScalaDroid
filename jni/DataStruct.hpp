#pragma once
#include <malloc.h>
#include <string.h>
#include <stdlib.h>
#include <stdint.h>
struct QuadTree
{
	QuadTree *children[ 4 ] = { nullptr };
	int32_t *items = nullptr;
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
				items = ( int32_t* )malloc( sizeof( int32_t ) * MAX_ITEMS );

			}
		}
	}
};
template< typename T >
struct Array
{
	static constexpr int STEP = 0x20;
	T *data = nullptr;
	int32_t position = 0 , limit = 0;
	void add( T v )
	{
		if( position >= limit )
		{
			auto *new_data = ( T* )malloc( sizeof( T ) * ( limit + STEP ) );
			if( data != nullptr )
			{
				memcpy( new_data , data , sizeof( T ) * limit );
				free( data );
			}
			data = new_data;
			limit += STEP;
		}
		data[ position++ ] = v;
	}
	void dispose()
	{
		if( data )
		{
			position = 0;
			limit = 0;
			free( data );
			data = nullptr;
		}
	}
};