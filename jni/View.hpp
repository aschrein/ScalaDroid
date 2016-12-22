#pragma once
#include <malloc.h>
#include <string.h>
#include <stdlib.h>
#include <stdint.h>
#include "DataStruct.hpp"
#include <cmath>
#include "Atlas.hpp"
struct SpatialState
{
	float x , y;
};
struct PersonView
{
	int32_t person_id;
	int32_t uv_mapping_id;
};
struct RelationView
{
	int32_t person_view0 , person_view1;
};
struct RectVertex
{
	float x , y;
	uint8_t u , v , ru , tv;
};
struct PersonViewRect
{
	RectVertex vertex[ 6 ];
};
float randf()
{
	return float( rand() ) / RAND_MAX;
}
struct View
{
	Array< PersonView > views;
	Array< SpatialState > spatial_states;
	Array< RelationView > relations;
	Atlas *atlas;
	struct
	{
		View *view;
		Program program;
		Buffer dev_buffer;
		Array< PersonViewRect > host_buffer;
		int32_t uviewproj , utexture , ucolor;
		void init( View *view )
		{
			this->view = view;
			dev_buffer = Buffer::createVBO( GL_STREAM_DRAW ,
			{ 2 ,
			{
				{ 0 , 2 , GL_FLOAT , GL_FALSE , 12,0 },
				{ 1 , 4 , GL_UNSIGNED_BYTE , GL_TRUE , 12,8 }
			}
			} );
			program = Program::create(
				"precision highp float;\n\
				uniform vec4 color;\n\
				uniform sampler2D texture;\n\
				varying vec4 uv;\n\
				void main()\n\
				{\n\
					float alpha = length( uv.zw - 0.5 ) < 0.5 ? 1.0 : 0.0;\n\
					gl_FragColor = color * texture2D( texture , uv.xy ) * vec4( vec3(1) , alpha );\n\
				}"
				,
				"//uniform vec2 offset;\n\
				//uniform vec2 scale;\n\
				uniform mat4 viewproj;\n\
				attribute vec2 position;\n\
				attribute vec4 vertex_uv;\n\
				varying vec4 uv;\n\
				void main()\n\
				{\n\
					uv = vertex_uv;\
					gl_Position = viewproj * vec4( position /* scale + offset*/ , 0.0 , 1.0 );\n\
				}"
			);
			uviewproj = program.getUniform( "viewproj" );
			ucolor = program.getUniform( "color" );
			utexture = program.getUniform( "texture" );
		}
		void update()
		{
			host_buffer.position = 0;
			float view_size = 1.1f;
			for( int32_t i = 0; i < view->views.position; i++ )
			{
				auto person_view = view->views.data[ i ];
				UVMapping uv_mapping{ 0xff , 0xff , 0 , 0 };
				if( person_view.uv_mapping_id >= 0 )
				{
					uv_mapping = view->atlas->mappings.data[ person_view.uv_mapping_id ];
				}
				auto sstate = view->spatial_states.data[ i ];
				PersonViewRect rect;
				rect.vertex[ 0 ] =
				{
					sstate.x - view_size , sstate.y - view_size , uv_mapping.u , uv_mapping.v , 0 , 0
				};
				rect.vertex[ 1 ] =
				{
					sstate.x - view_size , sstate.y + view_size
					, uv_mapping.u , uv_mapping.v + uv_mapping.v_size , 0 , 0xff
				};
				rect.vertex[ 2 ] =
				{
					sstate.x + view_size , sstate.y + view_size
					, uv_mapping.u + uv_mapping.u_size , uv_mapping.v + uv_mapping.v_size , 0xff, 0xff
				};
				rect.vertex[ 3 ] = rect.vertex[ 0 ];
				rect.vertex[ 4 ] = rect.vertex[ 2 ];
				rect.vertex[ 5 ] =
				{
					sstate.x + view_size , sstate.y - view_size
					, uv_mapping.u + uv_mapping.u_size , uv_mapping.v , 0xff , 0
				};
				//__android_log_print( ANDROID_LOG_VERBOSE , "NATIVE" , "UV %f\n" , rect.vertex[ 0 ].u );
				host_buffer.add( rect );
			}
			dev_buffer.bind();
			dev_buffer.setData( host_buffer.data , host_buffer.position * sizeof( PersonViewRect ) );
			dev_buffer.unbind();
		}
		void draw( float *viewproj )
		{

			program.bind();
			dev_buffer.bind();
			glUniform4f( ucolor , 1.0f , 1.0f , 1.0f , 1.0f );
			glActiveTexture( GL_TEXTURE0 );
			glBindTexture( GL_TEXTURE_2D , view->atlas->atlas_texture );
			glUniform1i( utexture , 0 );
			glUniformMatrix4fv( uviewproj , 1 , false , viewproj );
			glDrawArrays( GL_TRIANGLES , 0 , view->views.position * 6 );
			dev_buffer.unbind();
		}
		void dispose()
		{
			program.dispose();
			dev_buffer.dispose();
			host_buffer.dispose();
		}
	} rects;
	struct
	{
		View *view;
		Program program;
		Buffer dev_buffer;
		Array< float > host_buffer;
		int32_t uviewproj , ucolor;
		void init( View *view )
		{
			this->view = view;
			dev_buffer = Buffer::createVBO( GL_STREAM_DRAW , { 1 ,{ { 0 , 2 , GL_FLOAT , false , 8,0 } } } );
			program = Program::create(
				"precision highp float;\n\
				uniform vec4 color;\n\
				void main()\n\
				{\n\
					gl_FragColor = color;\n\
				}"
				,
				"uniform mat4 viewproj;\n\
				attribute vec2 position;\n\
				varying vec2 uv;\n\
				void main()\n\
				{\n\
					gl_Position = viewproj * vec4( position , 0.0 , 1.0 );\n\
				}"
			);
			uviewproj = program.getUniform( "viewproj" );
			ucolor = program.getUniform( "color" );
		}
		void update()
		{
			host_buffer.position = 0;
			//__android_log_print( ANDROID_LOG_VERBOSE , "NATIVE" , "edges buf pointer %i\n" , out_edges_buf );
			for( int i = 0; i < view->relations.position; i++ )
			{
				auto relation = view->relations.data[ i ];
				auto v0 = view->spatial_states.data[ relation.person_view0 ];
				auto v1 = view->spatial_states.data[ relation.person_view1 ];
				host_buffer.add( v0.x );
				host_buffer.add( v0.y );
				host_buffer.add( v1.x );
				host_buffer.add( v1.y );
			}
			dev_buffer.bind();
			dev_buffer.setData( host_buffer.data , host_buffer.position * sizeof( float ) * 2 );
			dev_buffer.unbind();
			
		}
		void draw( float *viewproj )
		{
			program.bind();
			dev_buffer.bind();
			glUniform4f( ucolor , 0.0f , 0.0f , 0.0f , 1.0f );
			glUniformMatrix4fv( uviewproj , 1 , false , viewproj );
			glDrawArrays( GL_LINES , 0 , view->relations.position * 2 );
			dev_buffer.unbind();
		}
		void dispose()
		{
			program.dispose();
			dev_buffer.dispose();
			host_buffer.dispose();
		}
	} edges;
	static View *create()
	{
		View *out = ( View* )malloc( sizeof( View ) );
		memset( out , 0 , sizeof( View ) );
		out->atlas = Atlas::create();
		out->rects.init(out);
		out->edges.init( out );
		return out;
	}
	static void dispose( View *view )
	{
		view->views.dispose();
		view->relations.dispose();
		view->edges.dispose();
		view->rects.dispose();
		view->spatial_states.dispose();
		Atlas::dispose( view->atlas );
		free( view );
	}
	int32_t createPersonView( int32_t person_id )
	{
		float r = sqrtf( randf() ) * 100.0f;
		float phi = randf() * M_PI * 2;
		SpatialState sstate{ cosf( phi ) * r , sinf( phi ) * r };
		PersonView view{ person_id , -1 };
		spatial_states.add( sstate );
		views.add( view );
		return views.position - 1;
	}
	int32_t addRelation( int32_t person_view0 , int32_t person_view1 )
	{
		relations.add( { person_view0 , person_view1 } );
		return relations.position - 1;
	}
	View() = default;
	void render( float x , float y , float z , int width , int height , void *requests , void *responses )
	{
		
		int32_t uv_responses_count = getResponsesCount( responses );
		for( int32_t i = 0; i < uv_responses_count; i++ )
		{
			auto response = getResponse( responses , i );
			auto new_id = atlas->allocate( response.texture_id , response.width , response.heght );
			views.data[ response.person_view_id ].uv_mapping_id = new_id;
			//__android_log_print( ANDROID_LOG_VERBOSE , "NATIVE" , "response %i->%i\n" , response.person_view_id , response.texture_id );
			//views.data[ uv_response.person_view_id ].uv_mapping_id = uv_response.new_uv_mapping_id;
		}
		for( int32_t i = 0; i < views.position; i++ )
		{
			auto person_view = views.data[ i ];
			if( person_view.uv_mapping_id == -1 )
			{
				addRequest( requests , { person_view.person_id , i , 1.0f } );
				views.data[ i ].uv_mapping_id = -2;
			}
		}
		float viewproj[] =
		{
			-1.0f , 0.0f , 0.0f , 0.0f ,
			0.0f , float( width ) / height , 0.0f , 0.0f ,
			0.0f , 0.0f , 1.0f , 0.0f ,
			x , -y , 0.0f , z
		};
		pack();
		rects.update();
		edges.update();
		glBindFramebuffer( GL_FRAMEBUFFER , 0 );
		glClearColor( 1 , 1 , 1 , 1 );
		glClearDepthf( 1 );
		glClear( GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT );
		glViewport( 0 , 0 , width , height );
		glDisable( GL_DEPTH_TEST );
		glDisable( GL_CULL_FACE );
		glEnable( GL_BLEND );
		glBlendFunc( GL_SRC_ALPHA , GL_ONE_MINUS_SRC_ALPHA );
		glBlendEquation( GL_FUNC_ADD );
		edges.draw( viewproj );
		rects.draw( viewproj );
		
		
		atlas->draw();
	}
	float pushForce( float x )
	{
		return -1.0f / ( 1.0f + x );
	}
	float pullForce( float x )
	{
		return fmin( 1.0f , x );
	}
	void pack()
	{
		for( int32_t i = 0; i < spatial_states.position; i++ )
		{
			auto person_view = spatial_states.data[ i ];
			for( int32_t j = i + 1; j < spatial_states.position; j++ )
			{
				auto &person_view1 = spatial_states.data[ j ];
				float dx = person_view1.x - person_view.x;
				float dy = person_view1.y - person_view.y;
				float dist = ( dx * dx + dy * dy );
				if( __isfinitef( dist ) && fabsf( dist ) > __FLT_EPSILON__ && fabsf( dist ) < 100.0f )
				{
					dist = sqrtf( dist );
					dx /= dist;
					dy /= dist;
					float force = pushForce( dist );
					spatial_states.data[ i ].x += dx * force;
					spatial_states.data[ i ].y += dy * force;
					person_view1.x -= dx * force;
					person_view1.y -= dy * force;
				}
			}
		}
		for( int i = 0; i < relations.position; i++ )
		{
			auto relation = relations.data[ i ];
			auto &v0 = spatial_states.data[ relation.person_view0 ];
			auto &v1 = spatial_states.data[ relation.person_view1 ];
			float dx = v1.x - v0.x;
			float dy = v1.y - v0.y;
			float dist = ( dx * dx + dy * dy );
			if( __isfinitef( dist ) && fabsf( dist ) > __FLT_EPSILON__ )
			{
				dist = sqrtf( dist );
				dx /= dist;
				dy /= dist;
				float force = ( pushForce( dist * 0.1f ) * 2.0f + pullForce( dist ) ) * 1.6f;
				v0.x += dx * force;
				v0.y += dy * force;
				v1.x -= dx * force;
				v1.y -= dy * force;
			}
		}
	}

};