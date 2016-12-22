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
#include <pthread.h>
#include "View.hpp"
typedef  void ( *Work )( void* );
struct WorkerThread
{
	pthread_cond_t cond;
	pthread_mutex_t mutex;
	pthread_t pthread;
	volatile int32_t working;
	volatile int32_t work_is_done;
	Work work;
	void *data;
	static void *loop( WorkerThread *th )
	{
		while( th->working )
		{
			pthread_mutex_lock( &th->mutex );
			pthread_cond_wait( &th->cond , &th->mutex );
			if( th->work )
			{
				th->work( th->data );
				th->work = nullptr;
				th->data = nullptr;
				th->work_is_done = 1;
				pthread_cond_signal( &th->cond );
			}
			pthread_mutex_unlock( &th->mutex );
		}
		return 0;
	}
	static WorkerThread *create()
	{
		WorkerThread *out = ( WorkerThread* )malloc( sizeof( WorkerThread ) );
		pthread_mutex_init( &out->mutex , NULL );
		pthread_cond_init( &out->cond , NULL );
		out->working = 1;
		out->work_is_done = 0;
		out->work = nullptr;
		out->data = nullptr;
		pthread_create( &out->pthread , NULL , ( void *(*)(void*) )loop , out );
		return out;
	}
	void submitWork( Work work , void *data )
	{
		pthread_mutex_lock( &mutex );
		this->work = work;
		this->data = data;
		work_is_done = 0;
		pthread_cond_signal( &cond );
		pthread_mutex_unlock( &mutex );
	}
	void waitWorkDone()
	{
		if( !work_is_done )
		{
			pthread_mutex_lock( &mutex );
			while( !work_is_done )
			{
				pthread_cond_wait( &cond , &mutex );
			}
			pthread_mutex_unlock( &mutex );
		}
	}
	void join()
	{
		working = 0;
		pthread_cond_signal( &cond );
		pthread_join( pthread , NULL );
		pthread_mutex_destroy( &mutex );
		pthread_cond_destroy( &cond );
		free( this );
	}
};
//WorkerThread *worker = WorkerThread::create();

void testWork(void*)
{
	__android_log_print( ANDROID_LOG_VERBOSE , "NATIVE WORKER" , "hello from native worker!!\n" );
}


View *view;
extern "C" {
	JNIEXPORT void JNICALL Java_main_java_Natives_init(
		JNIEnv* env , jclass clazz
	)
	{
		view = View::create();
	}
	JNIEXPORT void JNICALL Java_main_java_Natives_render(
		JNIEnv* env , jclass clazz , float x , float y , float z , int width , int height , jobject requests_buf , jobject responses_buf
	)
	{
		//worker->submitWork( testWork , nullptr );
		view->render( x , y , z , width , height , env->GetDirectBufferAddress( requests_buf ) , env->GetDirectBufferAddress( responses_buf ) );
		/*renderVertexBuffer( view
			, ( UVMapping* )env->GetDirectBufferAddress( uv_mappings_buf )
			, ( PersonViewRect* )env->GetDirectBufferAddress( out_rects_buf ) , env->GetDirectBufferAddress( out_edge_buf )
			, env->GetDirectBufferAddress( uv_requests_buf ) , env->GetDirectBufferAddress( uv_responses_buf ) );*/
		//tickForce( size , point_count , pair_count , points , pairs );
		//worker->waitWorkDone();
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