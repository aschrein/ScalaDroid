package main.scala.game
import linalg.vec2
import main.scala.rendering.Command


trait InputEvent
case class MouseDown( p : vec2 ) extends InputEvent
case class MouseUp( p : vec2 ) extends InputEvent
case class MouseDrag( dp : vec2 ) extends InputEvent
trait Message
trait Component
trait RenderingComponent extends Component {
	def getCommands : Seq[ Command ]
}
trait InputComponent extends Component {
	def collide( p : vec2 ) : Boolean
	def consume( e : InputEvent ) : Unit
}
class Entity( val name : String ) {
	private val components = scala.collection.mutable.ArrayBuffer.empty[Component]
	def addComponent( component : Component ) = {
		components += component
	}
	def removeComponent( component: Component ) = {
		components -= component
	}
	def getComponents : Seq[ Component ] = components
}
trait Vertex
trait Edge {
	def accident ( vertex: Vertex ) : Boolean
	def getAccident : ( Vertex , Vertex )
	def getNext( v : Vertex ) = if( v == getAccident._1 ) getAccident._2 else if( v == getAccident._2 ) getAccident._1 else NilVertex
}
trait Node {
	private val edges = scala.collection.mutable.ArrayBuffer.empty[Edge]
	def addEdge( edge: Edge ) = {
		edges += edge
	}
	def removeEdge( edge: Edge ) = {
		edges -= edge
	}
	def getEdges : Seq[ Edge ] = edges
}
object NilVertex extends Vertex
case class Branch( origin : Vertex , end : Vertex = NilVertex ) extends Edge {
	def accident ( vertex: Vertex ) = vertex == origin || vertex == end
	override def getAccident : (Vertex, Vertex) = ( origin , end )
}