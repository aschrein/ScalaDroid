package main.scala.vkapi
import android.app.Activity
import android.content.Context
import android.util.Log
import com.vk.sdk.VKSdk
import com.vk.sdk.api.VKRequest.VKRequestListener
import com.vk.sdk.api._
import main.scala.social.graph.{Person, RelationGraph}
import org.json.JSONObject
/**
  * Created by anton on 12/20/2016.
  */
object SocialMapper {
	def init( context : Activity) = {
		VKSdk.login ( context )
	}
	def mapUser( relationGraph: RelationGraph , id : String = null ) : Unit = {
		val req = new VKRequest("users.get",
			if( id != null ) VKParameters.from ( VKApiConst.FIELDS, "photo",VKApiConst.USER_ID, id )
			else VKParameters.from ( VKApiConst.FIELDS, "photo" ) )
		req.executeWithListener ( new VKRequestListener ( ) {
			override def onComplete ( response : VKResponse ) : Unit = {
				val json = new JSONObject ( response.responseString )
				val resp = json.getJSONArray( "response" )
				val item = resp.getJSONObject ( 0 )
				val item_id = item.getString( "id" )
				val first_name = item.getString ( "first_name" )
				val last_name = item.getString ( "last_name" )
				val avatar_url = item.getString ( "photo" )
				val person = relationGraph.createPerson( first_name , last_name , item_id , avatar_url )
				mapFriends(relationGraph , person)
				Log.i( this.getClass.getSimpleName , response.responseString )
			}
			override def onError ( error : VKError ): Unit = {
				Log.e( this.getClass.getSimpleName , error.errorMessage )
			}
			override def attemptFailed ( request : VKRequest, attemptNumber : Int, totalAttempts : Int ): Unit = {
				Log.e( this.getClass.getSimpleName , "attemptFailed" )
			}
		} )
	}
	def mapFriends( relationGraph: RelationGraph , person: Person ) : Unit = {
		val req = new VKRequest("friends.get",
			VKParameters.from ( VKApiConst.FIELDS, "photo",VKApiConst.USER_ID, person.id ) )
			//else VKParameters.from ( VKApiConst.FIELDS, "photo" ) )
		req.executeWithListener ( new VKRequestListener ( ) {
			override def onComplete ( response : VKResponse ) : Unit = {
				val json = new JSONObject ( response.responseString )
				val resp = json.getJSONObject ( "response" )
				val count = resp.getInt ( "count" )
				val items = resp.getJSONArray ( "items" )
				for ( i <- 0 until count ) {
					val item = items.getJSONObject ( i )
					val item_id = item.getString( "id" )
					val first_name = item.getString ( "first_name" )
					val last_name = item.getString ( "last_name" )
					val avatar_url = item.getString ( "photo" )
					relationGraph.createPerson( first_name , last_name , item_id , avatar_url ) friendWith person
				}
				Log.i( this.getClass.getSimpleName , response.responseString )
			}
			override def onError ( error : VKError ): Unit = {
				Log.e( this.getClass.getSimpleName , error.errorMessage )
			}
			override def attemptFailed ( request : VKRequest, attemptNumber : Int, totalAttempts : Int ): Unit = {
				Log.e( this.getClass.getSimpleName , "attemptFailed" )
			}
		} )
	}

}
