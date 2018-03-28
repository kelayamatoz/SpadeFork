package spade.params
                          
import spade._
import spade.node._
import spade.network._
import prism._
import prism.node._
import prism.enums._

import scala.language.reflectiveCalls

import scala.collection.mutable.ListBuffer

sealed trait Pattern extends Parameter

trait GridPattern extends Pattern {
  val step = 2 
}

trait GridCentrolPattern extends GridPattern {
  val switchParam:SwitchParam
  def switchAt(i:Int, j:Int)(implicit top:MeshTop):BundleSet = {
    val coord = if (isDynamic(top)) (i,j) else (i*step, j*step)
    BundleSet(param=switchParam, coord=Some(coord))
  }
  def cuAt(i:Int, j:Int)(implicit top:MeshTop):BundleSet
  def cuCoord(i:Int, j:Int)(implicit top:MeshTop) = {
    top match {
      case top if isDynamic(top) => (i, j)
      case top => (step/2 + i*step, step/2 + j*step)
    }
  }
}

trait GridFringePattern extends GridPattern {
  val mcParam:MCParam
  val argFringeParam:ArgFringeParam
  def argBundle(implicit top:MeshTop):BundleSet = {
    BundleSet(argFringeParam)
  }
  def mcAt(i:Int, j:Int)(implicit top:MeshTop):BundleSet = {
    import top.param._
    val coord = top match {
      case top:StaticMeshTop => if (i==0) (-step/2, j*step) else (numCols*step+step/2, j*step)
      case top:DynamicMeshTop => if (i==0) (-1, j) else (numCols, j)
    }
    BundleSet(mcParam, coord=Some(coord))
  }
}

case class MCOnly(
  argFringeParam:ArgFringeParam=ArgFringeParam(),
  mcParam:MCParam=MCParam()
) extends GridFringePattern

/*
 *
 *  +-----+-----+
 *  | PCU | PMU |
 *  +-----+-----+
 *  | PMU | PCU |
 *  +-----+-----+
 *
 * */
case class Checkerboard (
  switchParam:SwitchParam=SwitchParam(),
  pcuParam:PCUParam=PCUParam(),
  pmuParam:PMUParam=PMUParam()
) extends GridCentrolPattern {
  def cuAt(i:Int, j:Int)(implicit top:MeshTop):BundleSet = {
    val param = if ((i+j) % 2 == 0) pcuParam else pmuParam 
    BundleSet(param=param, coord=Some(cuCoord(i,j)))
  }
}
/*
 *
 *  +-----+-----+
 *  | PCU | PMU |
 *  +-----+-----+
 *  | PCU | PMU |
 *  +-----+-----+
 *
 * */
case class ColumnStrip (
  switchParam:SwitchParam=SwitchParam(),
  pcuParam:PCUParam=PCUParam(),
  pmuParam:PMUParam=PMUParam()
) extends GridCentrolPattern {
  def cuAt(i:Int, j:Int)(implicit top:MeshTop):BundleSet = {
    val param = if (j % 2 == 0) pcuParam else pmuParam 
    BundleSet(param=param, coord=Some(cuCoord(i,j)))
  }
}
/*
 *
 *  +-----+-----+
 *  | PCU | PCU |
 *  +-----+-----+
 *  | PMU | PMU |
 *  +-----+-----+
 *
 * */
case class RowStrip (
  switchParam:SwitchParam=SwitchParam(),
  pcuParam:PCUParam=PCUParam(),
  pmuParam:PMUParam=PMUParam()
) extends GridCentrolPattern {
  def cuAt(i:Int, j:Int)(implicit top:MeshTop):BundleSet = {
    val param = if (i % 2 == 0) pcuParam else pmuParam 
    BundleSet(param=param, coord=Some(cuCoord(i,j)))
  }
}
/*
 *
 *  +-----+-----+
 *  | PCU | PMU |
 *  +-----+-----+
 *  | SCU | SCU |
 *  +-----+-----+
 *
 * */
case class MixAll (
  switchParam:SwitchParam=SwitchParam(),
  pcuParam:PCUParam=PCUParam(),
  pmuParam:PMUParam=PMUParam(),
  scuParam:SCUParam=SCUParam()
) extends GridCentrolPattern {
  def cuAt(i:Int, j:Int)(implicit top:MeshTop):BundleSet = {
    val param = if (i % 2 == 0) {
      if (j % 2 == 0) pcuParam else pmuParam
    } else scuParam
    BundleSet(param=param, coord=Some(cuCoord(i,j)))
  }
}
/*
 *
 *  +-----+-----+
 *  | PCU | PMU |
 *  +-----+-----+
 *  | PMU | SCU |
 *  +-----+-----+
 *
 * */
case class HalfAndHalf (
  switchParam:SwitchParam=SwitchParam(),
  pcuParam:PCUParam=PCUParam(),
  pmuParam:PMUParam=PMUParam(),
  scuParam:SCUParam=SCUParam()
) extends GridCentrolPattern {
  def cuAt(i:Int, j:Int)(implicit top:MeshTop):BundleSet = {
    val param = if (i % 2 == 0) if (j % 2 == 0) pcuParam else pmuParam
                else if (j % 2 == 0) pmuParam else scuParam
    BundleSet(param=param, coord=Some(cuCoord(i,j)))
  }
}
