package main.scala.vkapi
import android.app.Activity
import android.content.Context
import android.util.Log
import com.vk.sdk.VKSdk
import com.vk.sdk.api.VKRequest.VKRequestListener
import com.vk.sdk.api._
import main.scala.social.graph.{Person, RelationGraph}
import org.json.JSONObject

import scala.collection.mutable.ArrayBuffer
/**
  * Created by anton on 12/20/2016.
  */
object SocialMapper {
	def init ( context : Activity ) = {
		VKSdk.login ( context )
	}
	def image_fields = "photo_50,photo_100,photo_200"
	def mapUser ( relationGraph : RelationGraph, id : String = null ) : Unit = {
		val req = new VKRequest ( "users.get",
			if ( id != null ) VKParameters.from ( VKApiConst.FIELDS,image_fields , VKApiConst.USER_ID, id )
			else VKParameters.from ( VKApiConst.FIELDS, image_fields )
		)
		req.executeWithListener ( new VKRequestListener ( ) {
			override def onComplete ( response : VKResponse ) : Unit = {
				println(  response.responseString )
				val json = new JSONObject ( response.responseString )
				val resp = json.getJSONArray ( "response" )
				val item = resp.getJSONObject ( 0 )
				val person = parsePerson( relationGraph,item )
				mapFriends ( relationGraph, person )

			}
			override def onError ( error : VKError ) : Unit = {
				Log.e ( this.getClass.getSimpleName, error.errorMessage )
			}
			override def attemptFailed ( request : VKRequest, attemptNumber : Int, totalAttempts : Int ) : Unit = {
				Log.e ( this.getClass.getSimpleName, "attemptFailed" )
			}
		} )
	}
	def parsePerson( relationGraph : RelationGraph,item: JSONObject ) = {
		val item_id = item.getString ( "id" )
		val first_name = item.getString ( "first_name" )
		val last_name = item.getString ( "last_name" )
		val url_arr = ArrayBuffer.empty[ String ]
		for( img_size <- image_fields.split(",") ) {
			if( item.has( img_size ) ) {
				url_arr += item.getString( img_size )
			}
		}
		relationGraph.createPerson ( first_name, last_name, item_id, url_arr )
	}
	def mapFriends ( relationGraph : RelationGraph, person : Person, depth : Int = 1 ) : Unit = {
		val req = new VKRequest ( "friends.get",
			VKParameters.from ( VKApiConst.FIELDS, image_fields, VKApiConst.USER_ID, person.vk_id ) )
		req.executeWithListener ( new VKRequestListener ( ) {
			override def onComplete ( response : VKResponse ) : Unit = {
				//Log.i ( this.getClass.getSimpleName, response.responseString )
				val json = new JSONObject ( response.responseString )
				val resp = json.getJSONObject ( "response" )
				val count = resp.getInt ( "count" )
				val items = resp.getJSONArray ( "items" )
				for ( i <- 0 until count ) {
					val item = items.getJSONObject ( i )
					val new_person = parsePerson( relationGraph,item )
					new_person friendWith person
					//Log.w ( this.getClass.getSimpleName, new_person.toString )
					if ( depth > 0 ) {
						mapFriends ( relationGraph, new_person, depth - 1 )
					}
				}
			}
			override def onError ( error : VKError ) : Unit = {
				Log.e ( this.getClass.getSimpleName, error.errorMessage )
			}
			override def attemptFailed ( request : VKRequest, attemptNumber : Int, totalAttempts : Int ) : Unit = {
				Log.e ( this.getClass.getSimpleName, "attemptFailed" )
			}
		} )
	}
}
