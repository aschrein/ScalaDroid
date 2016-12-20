package main.scala.events
import linalg.vec2
trait InputEvent
case class MouseDown( p : vec2 ) extends InputEvent
case class MouseUp( p : vec2 ) extends InputEvent
case class MouseDrag( dp : vec2 ) extends InputEvent