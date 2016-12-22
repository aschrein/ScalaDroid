package main.scala.social.graph
import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer
case class Person ( id : Int, first_name : String, last_name : String, vk_id : String, image_url : Seq[ String ], var view_id : Int = -1 )( implicit context : RelationGraph ) {
	def friendWith ( person : Person ) : Unit = {
		context addRelation (this, person)
	}
	def remove ( person : Person ) : Unit = {
		context removeRelation (this, person)
	}
	def getFriends = context.getFriends ( this )
}
trait Subscriber {
	def onPersonAdded ( person : Person ) : Unit
	def onPersonRemoved ( person : Person ) : Unit
	def onRelationAdded ( person : Person, person1 : Person ) : Unit
	def onRelationRemoved ( person : Person, person1 : Person ) : Unit
}
class RelationGraph {
	implicit val context = this
	private val persons = mutable.ArrayBuffer.empty [ Person ]
	private val id_map = new mutable.HashMap[ String, Person ]( )
	private val edges = new mutable.HashMap[ Person, mutable.Set[ Person ] ]( )
	private var pairs = new mutable.HashSet[ (Person, Person) ]( )
	private val subscribers = new mutable.HashSet[ Subscriber ]( )
	def addSubscriber ( subscriber : Subscriber ) = {
		persons.foreach ( p => subscriber.onPersonAdded ( p ) )
		pairs.foreach ( p => subscriber.onRelationAdded ( p._1, p._2 ) )
		subscribers.add ( subscriber )
	}
	def createPerson ( first_name : String, last_name : String, id : String, image_url : Seq[ String ] ) = {
		id_map.getOrElseUpdate ( id, {
			val new_person = Person ( persons.length, first_name, last_name, id, image_url )
			subscribers.foreach ( s => s.onPersonAdded ( new_person ) )
			persons += new_person
			new_person
		} )
	}
	def removePerson ( person : Person ) : Unit = {
		persons -= person
		edges.getOrElse ( person, mutable.Set.empty ).foreach ( p => {
			subscribers.foreach ( s => s.onPersonRemoved ( person ) )
			edges.getOrElse ( p, mutable.Set.empty ) -= person
		} )
		pairs = pairs.filter ( p => !( p._1 == person || p._2 == person ) )
		edges.remove ( person )
	}
	def addRelation ( person : Person, person1 : Person ) : Unit = {
		edges.getOrElseUpdate ( person, mutable.Set.empty ) += person1
		edges.getOrElseUpdate ( person1, mutable.Set.empty ) += person
		val sorted_pair = if ( person.id < person1.id ) (person, person1) else (person1, person)
		subscribers.foreach ( s => s.onRelationAdded ( sorted_pair._1, sorted_pair._2 ) )
		pairs.add ( sorted_pair )
	}
	def removeRelation ( person : Person, person1 : Person ) : Unit = {
		edges ( person ) -= person1
		edges ( person1 ) -= person
		val sorted_pair = if ( person.id < person1.id ) (person, person1) else (person1, person)
		subscribers.foreach ( s => s.onRelationRemoved ( sorted_pair._1, sorted_pair._2 ) )
		pairs = pairs.filter ( p => !( p._1 == person && p._2 == person1 || p._1 == person1 && p._2 == person ) )
	}
	def getUsers : Seq[ Person ] = persons
	//def getRelationMap = edges
	def getRelations : Seq[ (Person, Person) ] = pairs.toList
	def getFriends ( person : Person ) : Seq[ Person ] = edges.getOrElse ( person, mutable.Set.empty ).toList
}