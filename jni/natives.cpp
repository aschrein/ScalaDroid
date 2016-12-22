//
// Created by anton on 12/20/2016.
//Java_main_java_JNITest_getNum
#include <jni.h>
#include <malloc.h>
#include <string.h>
#include <cmath>
#include <stdio.h>
#include <stdlib.h>
#include <stdint.h>
struct QuadNode
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
			auto *new_data = ( T* )malloc( sizeof( T ) * (limit + STEP ));
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
struct PersonView
{
	int32_t person_id;
	int32_t uv_mapping_id;
	float x , y;
};
struct RelationView
{
	int32_t person_view0 , person_view1;
};

float randf()
{
	return float( rand() ) / RAND_MAX;
}
struct View
{
	Array< PersonView > views;
	Array< RelationView > relations;
	int32_t createPersonView( int32_t person_id )
	{
		float r = sqrtf( randf() ) * 100.0f;
		float phi = randf() * M_PI * 2;
		PersonView view{ person_id , -1 , cos( phi ) * r , sin( phi ) * r };
		views.add( view );
		return views.position - 1;
	}
	int32_t addRelation( int32_t person_view0 , int32_t person_view1 )
	{
		relations.add( { person_view0 , person_view1 } );
		return relations.position - 1;
	}
	~View()
	{
		views.dispose();
		relations.dispose();
	}
};
struct UVMapping
{
	float u , v , size_u , size_v;
};
struct RectVertex
{
	float x , y , u , v;
};
struct PersonViewRect
{
	RectVertex vertex[ 6 ];
};
struct UVMappingRequest
{
	int32_t person_view_id;
	int32_t person_id;
	int32_t old_uv_mapping_id;
	float lod;
};
struct UVMappingResponse
{
	int32_t person_view_id;
	int32_t new_uv_mapping_id;
};
void addUVRequest( void *uv_requests , UVMappingRequest request )
{
	( ( UVMappingRequest* )( ( int32_t* )uv_requests + 1 ) )[ ( ( int32_t* )uv_requests )[ 0 ]++ ] = request;
}
int32_t getUVResponsesCount( void *uv_responses )
{
	return ( ( int32_t* )uv_responses )[ 0 ];
}
UVMappingResponse getUVResponse( void *uv_responses , int32_t i )
{
	return ( ( UVMappingResponse* )( ( int32_t*)uv_responses + 1 ) )[ i ];
}
#include <android/log.h>
float pushForce( float x )
{
	return -1.0f / ( 1.0f + x );
}
float pullForce( float x )
{
	return fmin( 1.0f , x );
}
int renderVertexBuffer( View *view
	, UVMapping *uv_mappings , PersonViewRect *out_rects , void *out_edges
	, void *uv_requests , void *uv_responses
)
{
	int32_t uv_responses_count = getUVResponsesCount( uv_responses );
	//__android_log_print( ANDROID_LOG_VERBOSE , "NATIVE" , "responses count %i\n" , uv_responses_count );
	for( int32_t i = 0; i < uv_responses_count; i++ )
	{
		auto uv_response = getUVResponse( uv_responses , i );
		//__android_log_print( ANDROID_LOG_VERBOSE , "NATIVE" , "response %i->%i\n" , uv_response.person_view_id , uv_response.new_uv_mapping_id );
		view->views.data[ uv_response.person_view_id ].uv_mapping_id = uv_response.new_uv_mapping_id;
	}
	float view_size = 0.1f;
	for( int32_t i = 0; i < view->views.position; i++ )
	{
		auto person_view = view->views.data[ i ];
		for( int32_t j = i + 1; j < view->views.position; j++ )
		{
			auto &person_view1 = view->views.data[ j ];
			float dx = person_view1.x - person_view.x;
			float dy = person_view1.y - person_view.y;
			float dist = ( dx * dx + dy * dy );
			if( __isfinitef( dist ) && fabsf( dist ) > __FLT_EPSILON__ && fabsf( dist ) < 1.0f )
			{
				dist = sqrtf( dist );
				dx /= dist;
				dy /= dist;
				float force = pushForce( dist * 50.0f ) * 0.1f;
				view->views.data[ i ].x += dx * force;
				view->views.data[ i ].y += dy * force;
				person_view1.x -= dx * force;
				person_view1.y -= dy * force;
			}
		}
		UVMapping uv_mapping{ 1.0f , 1.0f , 0.0f , 0.0f };
		if( person_view.uv_mapping_id >= 0 )
		{
			uv_mapping = uv_mappings[ person_view.uv_mapping_id ];
		} else if( person_view.uv_mapping_id == -1 )
		{
			addUVRequest( uv_requests , { i , person_view.person_id , person_view.uv_mapping_id , 1.0f } );
			view->views.data[ i ].uv_mapping_id = -2;
		}
		PersonViewRect rect;
		rect.vertex[ 0 ] =
		{
			person_view.x - view_size , person_view.y - view_size , uv_mapping.u , uv_mapping.v
		};
		rect.vertex[ 1 ] =
		{
			person_view.x - view_size , person_view.y + view_size
			, uv_mapping.u , uv_mapping.v + uv_mapping.size_v
		};
		rect.vertex[ 2 ] =
		{
			person_view.x + view_size , person_view.y + view_size
			, uv_mapping.u + uv_mapping.size_u , uv_mapping.v + uv_mapping.size_v
		};
		rect.vertex[ 3 ] = rect.vertex[ 0 ];
		rect.vertex[ 4 ] = rect.vertex[ 2 ];
		rect.vertex[ 5 ] =
		{
			person_view.x + view_size , person_view.y - view_size
			, uv_mapping.u + uv_mapping.size_u , uv_mapping.v
		};
		out_rects[ i ] = rect;
	}
	float *out_edges_buf = ( float* )out_edges;
	//__android_log_print( ANDROID_LOG_VERBOSE , "NATIVE" , "edges buf pointer %i\n" , out_edges_buf );
	//__android_log_print( ANDROID_LOG_VERBOSE , "NATIVE" , "edges count %i\n" , view->relations.position );
	for( int i = 0; i < view->relations.position; i++ )
	{
		auto relation = view->relations.data[ i ];
		auto &v0 = view->views.data[ relation.person_view0 ];
		auto &v1 = view->views.data[ relation.person_view1 ];
		float dx = v1.x - v0.x;
		float dy = v1.y - v0.y;
		float dist = ( dx * dx + dy * dy );
		if( __isfinitef( dist ) && fabsf( dist ) > __FLT_EPSILON__ )
		{
			dist = sqrtf( dist );
			dx /= dist;
			dy /= dist;
			float force = ( pushForce( dist * 0.5f ) * 2.0f + pullForce( dist ) ) * 0.1f;
			v0.x += dx * force;
			v0.y += dy * force;
			v1.x -= dx * force;
			v1.y -= dy * force;
		}
	}
	for( int i = 0; i < view->relations.position; i++ )
	{
		auto relation = view->relations.data[ i ];
		auto v0 = view->views.data[ relation.person_view0 ];
		auto v1 = view->views.data[ relation.person_view1 ];
		out_edges_buf[ 4 * i ] = v0.x;
		out_edges_buf[ 4 * i + 1 ] = v0.y;
		out_edges_buf[ 4 * i + 2 ] = v1.x;
		out_edges_buf[ 4 * i + 3 ] = v1.y;
	}
	return view->views.position;
}
/*#define MAX_ITEMS 4
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
*/

View *view = new View();
extern "C" {
	JNIEXPORT void JNICALL Java_main_java_Natives_renderVertexBuffer(
		JNIEnv* env , jclass clazz
		, jobject uv_mappings_buf , jobject out_rects_buf , jobject out_edge_buf
		, jobject uv_requests_buf , jobject uv_responses_buf
	)
	{
		renderVertexBuffer( view
			, ( UVMapping* )env->GetDirectBufferAddress( uv_mappings_buf )
			, ( PersonViewRect* )env->GetDirectBufferAddress( out_rects_buf ) , env->GetDirectBufferAddress( out_edge_buf )
			, env->GetDirectBufferAddress( uv_requests_buf ) , env->GetDirectBufferAddress( uv_responses_buf ) );
		//tickForce( size , point_count , pair_count , points , pairs );
	}
	JNIEXPORT jint JNICALL Java_main_java_Natives_createView(
		JNIEnv* env , jclass clazz , jint person_id
	)

	{
		return view->createPersonView( person_id );
	}
	JNIEXPORT jint JNICALL Java_main_java_Natives_createRelationView(
		JNIEnv* env , jclass clazz , jint person_id0 , jint person_id1
	)

	{
		return view->addRelation( person_id0 , person_id1 );
	}
}