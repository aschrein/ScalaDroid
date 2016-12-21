package main.scala.social.graph
import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer
case class Person ( first_name : String, last_name : String, id : String, image_url : Seq[String] )( implicit context : RelationGraph ) {
	def friendWith ( person : Person ) : Unit = {
		context addRelation (this, person)
	}
	def remove ( person : Person ) : Unit = {
		context removeRelation (this, person)
	}
	def getFriends = context.getFriends ( this )
}
class RelationGraph {
	implicit val context = this
	private val persons = mutable.ArrayBuffer.empty[ Person ]
	private val edges = new mutable.HashMap[ Person, ArrayBuffer[ Person ] ]( )
	def createPerson( first_name : String, last_name : String, id : String, image_url : Seq[String] ) = {
		val new_person = Person(first_name,last_name,id,image_url)
		persons += new_person
		new_person
	}
	def removePerson( person: Person ) : Unit = {
		persons -= person
		edges.getOrElse( person , ArrayBuffer.empty ).foreach( p => edges.getOrElse( p , ArrayBuffer.empty ) -= person )
		edges.remove( person )
	}
	def addRelation ( person : Person, person1 : Person ) : Unit = {
		edges.getOrElseUpdate ( person, ArrayBuffer.empty ) += person1
		edges.getOrElseUpdate ( person1, ArrayBuffer.empty ) += person
	}
	def removeRelation ( person : Person, person1 : Person ) : Unit = {
		edges ( person ) -= person1
		edges ( person1 ) -= person
	}
	def getUsers : Seq[ Person ] = persons.clone()
	def getRelations : Seq[ ( Person , Person ) ] =
		edges
			.toList
			.flatMap ( e => e._2
				.map ( p => if ( e._1.id < p.id ) (e._1, p) else (p, e._1) ) ).distinct
	def getFriends ( person : Person ) : Seq[ Person ] = edges.getOrElse ( person, ArrayBuffer.empty )
}