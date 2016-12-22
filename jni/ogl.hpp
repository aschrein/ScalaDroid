#pragma once
#include <stdint.h>
#include <GLES2/gl2.h>
#include <android/log.h>

struct Program
{
	uint32_t program , frag_shader , vert_shader;
	static Program create( char const *frag_text , char const *vert_text )
	{
		auto compile = []( uint32_t type , char const *raw ) -> int32_t
		{
			uint32_t out = glCreateShader( type );
			glShaderSource( out , 1 , &raw , NULL );
			glCompileShader( out );
			GLint compiled;
			glGetShaderiv( out , GL_COMPILE_STATUS , &compiled );
			if( !compiled )
			{
				GLint length;
				glGetShaderiv( out , GL_INFO_LOG_LENGTH , &length );
				char *log = new char[ length ];
				glGetShaderInfoLog( out , length , &length , log );
				
				__android_log_print( ANDROID_LOG_VERBOSE , "NATIVE" , "shader creation error %s\n" , log );
				__android_log_print( ANDROID_LOG_VERBOSE , "NATIVE" , "shader creation error %s\n" , raw );
				glDeleteShader( out );
				delete[] log;
				return 0;
			}
			return out;
		};
		auto frag_shader = compile( GL_FRAGMENT_SHADER , frag_text );
		if( !frag_shader )
		{
			return{ 0 };
		}
		auto vert_shader = compile( GL_VERTEX_SHADER , vert_text );
		if( !vert_shader )
		{
			glDeleteShader( frag_shader );
			return{ 0 };
		}
		uint32_t prog = glCreateProgram();
		glAttachShader( prog , frag_shader );
		glAttachShader( prog , vert_shader );
		glLinkProgram( prog );
		GLint compiled;
		glGetProgramiv( prog , GL_LINK_STATUS , &compiled );
		if( !compiled )
		{
			GLint length;
			glGetProgramiv( prog , GL_INFO_LOG_LENGTH , &length );
			char *log = new char[ length ];
			glGetProgramInfoLog( prog , length , &length , &log[ 0 ] );
			__android_log_print( ANDROID_LOG_VERBOSE , "NATIVE" , "program creation error %s\n" , log );

			glDeleteShader( frag_shader );
			glDeleteShader( vert_shader );
			glDeleteProgram( prog );
			delete[] log;
			return{ 0 };
		}
		return{ prog , frag_shader ,vert_shader };
	}
	void bind()
	{
		glUseProgram( program );
	}
	uint32_t getUniform( const char *name )
	{
		return glGetUniformLocation( program , name );
	}
	void dispose()
	{
		if( program >= 0 )
		{
			glDeleteShader( vert_shader );
			glDeleteShader( frag_shader );
			glDeleteProgram( program );
			vert_shader = 0;
			frag_shader = 0;
			program = 0;
		}
	}
};
struct VertexAttributeGL
{
	uint32_t location;
	uint32_t elem_count;
	uint32_t src_type;
	uint32_t normalized;
	uint32_t stride;
	uint32_t offset;
};
struct AttributeArray
{
	uint32_t count;
	VertexAttributeGL attributes[ 10 ];
	void bind()
	{
		for( int i = 0; i < count; i++ )
		{
			glEnableVertexAttribArray( attributes[ i ].location );
			glVertexAttribPointer(
				attributes[ i ].location ,
				attributes[ i ].elem_count ,
				attributes[ i ].src_type ,
				attributes[ i ].normalized ,
				attributes[ i ].stride ,
				( void * )attributes[ i ].offset );
		}
	}
	void unbind()
	{
		for( int i = 0; i < count; i++ )
		{
			glDisableVertexAttribArray( attributes[ i ].location );
		}
	}
};
struct Buffer
{
	uint32_t bo;
	uint32_t target;
	uint32_t usage;
	AttributeArray attrib_array;
	uint32_t index_type;
	static Buffer createVBO( int usage , AttributeArray attributes )
	{
		Buffer out;
		out.attrib_array = attributes;
		out.target = GL_ARRAY_BUFFER;
		glGenBuffers( 1 , &out.bo );
		out.usage = usage;
		return out;
	}
	static Buffer createIBO( int usage , int index_type )
	{
		Buffer out;
		out.index_type = index_type;
		out.target = GL_ELEMENT_ARRAY_BUFFER;
		glGenBuffers( 1 , &out.bo );
		out.usage = usage;
		return out;
	}
	void setData( void *data , int size )
	{
		glBufferData( target , size , data , usage );
	}
	void bind()
	{
		glBindBuffer( target , bo );
		attrib_array.bind();
	}
	void unbind()
	{
		glBindBuffer( target , 0 );
		attrib_array.unbind();
	}
	void dispose()
	{
		if( bo )
		{
			glDeleteBuffers( 1 , &bo );
			bo = 0;
		}
	}
};