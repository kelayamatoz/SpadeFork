package spade.simulation

import spade._
import spade.node._
import spade.codegen._
import spade.util.SpadeMetadata

import pirc._
import pirc.util._

import scala.collection.mutable.ListBuffer

trait SimUtil extends Logger {
  def quote(n:Any):String
  var mapping:SpadeMapLike = _
  implicit lazy val mp:SpadeMapLike = mapping
  def fimap = mapping.fimap
  def cfmap = mapping.cfmap
  def rst:Boolean
  def cycle:Int
}

class Simulator(implicit arch:Spade, compiler:Compiler) extends SimUtil with Logger {

  val spademeta:SpadeMetadata = arch 
  implicit val sim:Simulator = this
  lazy val vcds = ListBuffer[VcdPrinter]()

  override def debug = SpadeConfig.verbose

  lazy val util:SimUtil = this

  var _inSimulation = false 
  def inSimulation = _inSimulation
  var _inRegistration = false 
  def inRegistration = _inRegistration

  override lazy val stream = newStream("sim.log")(compiler) //TODO: change this to app specific

  val period = 1; //ns per cycle
  var cycle = 0
  var rst = false

  var timeOut = false
  var done = false

  def finishSimulation:Boolean = {
    if (arch.top.ctrlBox.status.vAt(3).isHigh.getOrElse(false)) { done = true; true }
    else if (cycle >= SpadeConfig.simulationTimeOut) { 
      timeOut = true
      warn(s"Simulation time out at #${cycle}!")
      true 
    }
    else false
  } 

  def reset = {
    rst = false
    timeOut = false
    done = false
    _inSimulation = false
    _inRegistration = false
    cycle = 0
    arch.simulatable.foreach { m => m.reset }
  }

  def initPass = {
    vcds.foreach { vcds => vcds.emitHeader }
  }

  def register = {
    dprintln(s"\n\nRegistering update functions ...")
    _inRegistration = true
    arch.simulatable.foreach { s => s.registerAll; s.zeroModule }
    arch.simulatable.foreach { s => s.updateModule; }
    arch.simulatable.foreach { s => s.clearModule; s.zeroModule }
    _inRegistration = false
    arch.simulatable.foreach { s => s.check }
    info(s"# ios simulated: ${arch.simulatable.map(_.ios.size).sum}")
  }

  def simulate = {
    dprintln(s"\n\nStarting simulation ...")
    _inSimulation = true
    while (!finishSimulation) {
      rst = if (cycle == 1) true else false
      arch.simulatable.foreach(_.updateModule)
      vcds.foreach(_.emitSignals)
      arch.simulatable.foreach(_.clearModule)
      cycle += 1
    }
    _inSimulation = false
  }

  def run = {
    tic
    register
    dprintln(s"\n\nDefault values ...")
    vcds.foreach { _.emitSignals }
    cycle += 1
    simulate
    toc("Simulation","s")
  }

  def finPass = {
    close
    vcds.foreach { _.close }
  }

  override def quote(n:Any):String = {
    import spademeta._
    n match {
      case n:Stage => s"st[${n.index}]"
      case n:FuncUnit => s"${quote(n.stage)}.$n"
      case PipeReg(stage, reg) => s"${quote(stage)}.${quote(reg)}"
      case n:ArchReg => s"reg[${n.index}]"
      case n:Primitive if indexOf.get(n).nonEmpty => 
        s"${n.typeStr}[${n.index}]".replace(s"${spade.util.quote(n.prt)}", quote(n.prt))
      case n:Routable => coordOf.get(n).fold(s"$n") { case (x,y) => s"${n.typeStr}[$x,$y]" }
      case n:IO[_,_] =>  
        var q = spade.util.quote(n).replace(spade.util.quote(n.src), quote(n.src))
        n.src match {
          case n:Primitive => q = q.replace(spade.util.quote(n.prt), quote(n.prt))
          case _ =>
        }
        q
      case n:Value if n.parent.nonEmpty => s"${quote(n.parent.get)}.$n"
      case n:PortType => s"${quote(n.io)}.$n"
      case n:Node => spade.util.quote(n)
      case n => s"$n"
    }
  }

}

