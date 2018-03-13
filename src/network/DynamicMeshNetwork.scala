package spade.network

import spade._
import spade.node._
import spade.params._
import spade.util._

import prism._
import prism.collection.mutable.Table

import scala.language.reflectiveCalls

import scala.collection.mutable

class DynamicMeshNetwork[B<:BundleType](param:DynamicMeshNetworkParam[B], top:DynamicMeshTop)(implicit design:Design) {
  implicit val bct = param.bct
  import param._
  import top._
  import top.param._
  import fringePattern.argFringeParam._

  val bundleOf = mutable.Map[BundleGroup, GridBundle[B]]()

  bundles.foreach { node => 
    val bundle = GridBundle[B]()
    node.nios += bundle
    bundleOf(node) = bundle
  }

  def tpOf(node:BundleGroup) = node.param match {
    case param:PCUParam => "pcu"
    case param:PMUParam => "pmu"
    case param:SCUParam => "scu"
    case param:SwitchParam => "sb"
    case param:ArgFringeParam => "arg"
    case param:MCParam => "mc"
  }

  def connect(out:BundleGroup, in:BundleGroup)(implicit design:Design):Unit = {
    val cw = channelWidth("src"->tpOf(out), "dst"->tpOf(in))
    val key = Seq("src"->tpOf(out), "dst"->tpOf(in))
    (tpOf(out), tpOf(in)) match {
      case ("arg", _) =>
        val outs = bundleOf(out).outputs
        outs.foreach { argIn =>
          val ins = bundleOf(in).addIns(cw)
          ins.foreach { _ <== argIn }
        }
      case (_, "arg") =>
        val outs = bundleOf(out).addOuts(cw)
        val ins = bundleOf(in).inputs
        ins.foreach { _ <== outs }
      case (_, _) =>
        val outs = bundleOf(out).addOuts(cw)
        val ins = bundleOf(in).addIns(cw)
        outs.zip(ins).foreach { case (o, i) => i <== o }
    }
  }


  if (is[Word](this)) {
    bundleOf(argFringe).addInAt("S", numArgOuts)
    bundleOf(argFringe).addOutAt("S", numArgIns)
  } else if (is[Bit](this)) {
    bundleOf(argFringe).addInAt("S", 1) //status
    bundleOf(argFringe).addOutAt("S", 1) //command
  }

  /** ----- Central Array Connection ----- **/
  for (y <- 0 until numRows) {
    for (x <- 0 until numCols) {
      /* ----- CU to SB Connection ----- */
      connect(cuArray(x)(y), sbArray(x)(y))

      /* ----- SB to CU Connection ----- */
      connect(sbArray(x)(y), cuArray(x)(y))
    }
  }

  for (y <- 0 until numRows) {
    for (x <- 0 until numCols) {
      // Top to SB
      // Top Switches
      if (y==numRows) {
        // S -> N
        connect(sbArray(x)(y), argFringe) // bottom up 
        // N -> S
        connect(argFringe, sbArray(x)(y)) // top down
      }
      // Bottom Switches
      if (y==0) {
        // N -> S
        connect(sbArray(x)(y), argFringe) // top down 
        // S -> N
        connect(argFringe, sbArray(x)(y)) // bottom up
      }
    }
  }

  /** ----- Fringe Connection ----- **/
  for (y <- 0 until mcArray.headOption.map(_.size).getOrElse(0)) { //cols
    for (x <- 0 until mcArray.size) { //rows

      ///* ---- DramAddrGen and SwitchBox connection ---- */
      //if (x==0) {
        //// DAG to SB (W -> E) (left side)
        //connect(dramAGs(x)(y), "E", sbArray(0)(y), "W", "left")
        //// SB to DAG (E -> W) (left side)
        //connect(sbArray(0)(y), "W", dramAGs(x)(y), "E", "left")
      //} else {
        //// DAG to SB (E -> W) (right side)
        //connect(dramAGs(x)(y), "W", sbArray(numCols)(y), "E", "right")
        //// SB to DAG (W -> E) (right side)
        //connect(sbArray(numCols)(y), "E", dramAGs(x)(y), "W", "right")
      //}

      ///* ---- SramAddrGen and SwitchBox connection ---- */
      //if (x==0) {
        //// SAG to SB (W -> E) (left side)
        //connect(sramAGs(x)(y), "E", sbArray(0)(y), "W", "left")
        //// SB to SAG (E -> W) (left side)
        //connect(sbArray(0)(y), "W", sramAGs(x)(y), "E", "left")
      //} else {
        //// SAG to SB (E -> W) (right side)
        //connect(sramAGs(x)(y), "W", sbArray(numCols)(y), "E", "right")
        //// SB to SAG (W -> E) (right side)
        //connect(sbArray(numCols)(y), "E", sramAGs(x)(y), "W", "right")
      //}

      ///* ---- MC and SwitchBox connection ---- */
      if (x==0) {
        // MC to SB (W -> E) (left side)
        connect(mcArray(x)(y), sbArray(0)(y))
        // SB to MC (E -> W) (left side)
        connect(sbArray(0)(y), mcArray(x)(y))
      } else {
        // MC to SB (E -> W) (right side)
        connect(mcArray(x)(y), sbArray(numCols-1)(y))
        // SB to MC (W -> E) (right side)
        connect(sbArray(numCols-1)(y), mcArray(x)(y))
      }

      ///* ---- MC and DramAddrGen connection ---- */
      //val pos = if (x==0) "left" else "right"
      //// MC to DAG (S -> N)
      //connect(mcArray(x)(y), "N", dramAGs(x)(y), "S", pos)
      //// DAG to MC (N -> S)
      //connect(dramAGs(x)(y), "S", mcArray(x)(y), "N", pos)
    }
  }

  bundleOf.values.foreach { bundle =>
    indexing(bundle.inputs)
    indexing(bundle.outputs)
  }
}
