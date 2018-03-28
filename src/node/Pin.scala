package spade.node

import spade._

import prism._
import prism.node._
import prism.util._

import scala.language.reflectiveCalls

import scala.collection.mutable._

trait Edge extends prism.node.Edge[SpadeNode]() {
  type A = Pin[_]
}
abstract class  DirectedEdge[B<:PinType:ClassTag, E<:Edge:ClassTag] extends prism.node.DirectedEdge[SpadeNode, E] with Edge {
  val bct = implicitly[ClassTag[B]]
}

class InputEdge[B<:PinType:ClassTag](val src:Pin[B])(implicit design:Design) extends DirectedEdge[B,OutputEdge[B]] with prism.node.Input[SpadeNode]  {
  val id = design.nextId
  type E <: OutputEdge[B]
  def connect(p:Pin[B]):Unit = connect(p.out) 
  def <== (p:Pin[B]):Unit = connect(p) 
  def <== (ps:List[Pin[B]]):Unit = ps.foreach { p => connect(p) }
}
class OutputEdge[B<:PinType:ClassTag](val src:Pin[B])(implicit design:Design) extends DirectedEdge[B,InputEdge[B]] with prism.node.Output[SpadeNode]  {
  val id = design.nextId
  type E <: InputEdge[B]
}

abstract class Pin[B<:PinType:ClassTag](implicit val src:Module, design:Design) extends SpadeNode with Atom[SpadeNode] {
  setParent(src)
  val bct = implicitly[ClassTag[B]]
  val in:InputEdge[B] = new InputEdge(this)
  val out:OutputEdge[B] = new OutputEdge(this) 
}
case class Wire[B<:PinType:ClassTag]()(implicit src:Module, design:Design) extends Pin[B]
object Wire {
  def apply[B<:PinType:ClassTag](name:String)(implicit src:Module, design:Design):Wire[B] = naming(Wire(), name)
}

abstract class Port[B<:PinType:ClassTag](implicit src:Module, design:Design) extends Pin[B] {
  val external:DirectedEdge[B,_<:Edge]
  val internal:DirectedEdge[B,_<:Edge]
  def ic = internal

  type PT <: Port[B]
  def connected:List[PT] = external.connected.map(_.src.asInstanceOf[PT])
  def isConnected:Boolean = external.isConnected
  def isConnectedTo(p:PT) = external.connected.contains(p.external)
  def connect(p:PT):Unit = external.connect(p.external)
  def disconnectFrom(e:PT):Unit = external.disconnectFrom(e.external)
}
case class Input[B<:PinType:ClassTag]()(implicit src:Module, design:Design) extends Port[B] {
  type PT = Output[B]
  override val external:InputEdge[B] = in
  override val internal:OutputEdge[B] = out
  override def ic:OutputEdge[B] = internal
  def <== (p:OutputEdge[B]):Unit = external.connect(p)
  def <== (p:PT):Unit = connect(p)
  def <== (p:Wire[B]):Unit = ic.connect(p.in)
  def <== (ps:List[Any]):Unit = ps.foreach { p => 
    p match {
      case p:PT => <==(p)
      case p:Wire[B] => <==(p)
      case p:OutputEdge[B] => <==(p)
      case p => err(s"$this cannot connect to $p")
    }
  }
  def slice[T<:PinType:ClassTag](idx:Int)(implicit module:Module, design:Design):Pin[T] = {
    val sl = Module(Slice[T,B](idx), s"$this.slice($idx)")(module, design)
    this <== sl.out
    sl.in
  }
}
object Input {
  def apply[B<:PinType:ClassTag](name:String)(implicit src:Module, design:Design):Input[B] = naming(Input(), name)
}
object Inputs {
  def apply[B<:PinType:ClassTag](name:String, num:Int)(implicit src:Module, design:Design) = {
    indexing(List.fill(num)(Input[B](name)))
  }
}
case class Output[B<:PinType:ClassTag]()(implicit src:Module, design:Design) extends Port[B] {
  type PT = Input[B]
  override val external:OutputEdge[B] = out 
  override val internal:InputEdge[B] = in
  override def ic:InputEdge[B] = internal
  def broadCast[T<:PinType:ClassTag](implicit module:Module, design:Design):Pin[T] = {
    val bc = Module(BroadCast[B,T](), s"$this.broadcast")(module, design)
    bc.in <== this
    bc.out
  }
  def slice[T<:PinType:ClassTag](idx:Int)(implicit module:Module, design:Design):Pin[T] = {
    val sl = Module(Slice[B,T](idx), s"$this.slice($idx)")(module, design)
    sl.in <== this
    sl.out
  }
}
object Output {
  def apply[B<:PinType:ClassTag](name:String)(implicit src:Module, design:Design):Output[B] = naming(Output(), name)
}
case class BroadCast[I<:PinType:ClassTag, O<:PinType:ClassTag]()(implicit design:Design) extends Module {
  val in = Input[I](s"in")
  val out = Output[O](s"out")
}

case class Slice[I<:PinType:ClassTag, O<:PinType:ClassTag](idx:Int)(implicit design:Design) extends Module {
  val in = Input[I](s"in")
  val out = Output[O](s"out")
}

