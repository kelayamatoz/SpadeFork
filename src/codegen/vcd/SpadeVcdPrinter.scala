package spade.codegen

import spade._
import spade.node._
import spade.traversal._
import spade.util.{quote => _, _}
import spade.simulation._
import pirc.util._

import scala.collection.mutable.Map
import scala.collection.mutable.ListBuffer

trait SpadeVcdDeclarator { self:VcdPrinter =>
  val spadeDeclarator:SpadeVcdDeclarator = this
  private val _tracking = ListBuffer[Simulatable]()
  def tracking(self:SpadeVcdDeclarator):Iterable[Simulatable] = _tracking
  def track(s:Any):Unit = s match {
    case s:Simulatable if (!tracked(s)) => _tracking += s
    case s:Iterable[_] => s.foreach(track)
    case s =>
  } 
  def tracked(s:Simulatable):Boolean = _tracking.contains(s)
  def declarator(self:SpadeVcdDeclarator):Traversal = _declarator
  private val _declarator = new Traversal {
    override def visitNode (node:Node): Unit = {
      if (visited.contains(node)) return
      node match {
        case node:ComputeUnit if tracked(node) => declare(node) {
          declare("srams") { node.srams.foreach(visitNode) }
          declare("ctrs") { node.ctrs.foreach(visitNode) }
          declare("sfifos") { node.sfifos.foreach(visitNode) }
          declare("vfifos") { node.vfifos.foreach(visitNode) }
          declare("stages") { node.stages.foreach(visitNode) }
          super.visitNode(node)
        }
        case node:CtrlBox if tracked(node) => declare(node) { super.visitNode(node) }
        case node:Simulatable if tracked(node) => declare(node) { super.visitNode(node) }
        case _ => super.visitNode(node)
      }
    }
    override def traverse(implicit spade: Spade):Unit = {
      import spade._
      visitNode(top)
      declare("pcus") { pcus.foreach(visitNode) }
      declare("mcus") { mcus.foreach(visitNode) }
      declare("scus") { scus.foreach(visitNode) }
      declare("ocus") { ocus.foreach(visitNode) }
      declare("mcs") { mcs.foreach(visitNode) }
      declare("sbs") { sbs.foreach(visitNode) }
    } 
  } 

  def declare(m:Module)(finPass: => Unit):Unit = {
    emitkv(s"scope module", s"${quote(m)}")
    m match {
      case m:Routable =>
        emitkv(s"scope module", "sio")
        m.scalarIO.ios.foreach { io => declare(io) }
        emitln(s"$$upscope $$end")
        emitkv(s"scope module", "vio")
        m.vectorIO.ios.foreach { io => declare(io) }
        emitln(s"$$upscope $$end")
        emitkv(s"scope module", "cio")
        m.ctrlIO.ios.foreach { io => declare(io) }
        emitln(s"$$upscope $$end")
      case m:Simulatable => m.ios.foreach { io => declare(io) }
      case _ =>
    }
    finPass
    emitln(s"$$upscope $$end")
  }

}

class SpadeVcdPrinter(implicit sim:Simulator, spade: Spade) extends VcdPrinter {
  override lazy val stream = newStream("sim.vcd") 
  import sim.util._

  def declareAll = {
    addAll
    declarator(spadeDeclarator).traverse
  }

  def emitSignals = {
    emitTime
    tracking(spadeDeclarator).foreach { m =>
      m.ios.foreach { io => emitValue(io) }
    }
  }

  val adder = new Traversal {
    override def visitNode (node:Node): Unit = {
      node match {
        case node:GlobalIO[_,_] =>
        case node:Simulatable if isMapped(node) => 
          sim.dprintln(s"tracking ${sim.quote(node)}")
          spadeDeclarator.track(node)
        case _ =>
      }
      super.visitNode(node)
    }
  } 

  def addModule(m:Simulatable) = {
    adder.visitNode(m)
  }

  def addAll = {
    adder.traverse
    val sb = spade.asInstanceOf[SwitchNetwork].sbs.head
    val out = sb.ctrlIO.outs.head
  }

}
