package spade.newnode
                          
import spade._
import prism.node._
import pirc.enums._

import scala.language.reflectiveCalls
import scala.reflect._

import scala.collection.mutable.ListBuffer

sealed trait Pattern

trait GridPattern extends Pattern {
  def cuAt(i:Int, j:Int):CUParam
}

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
  pcuParam:PCUParam,
  pmuParam:PMUParam
) extends GridPattern {
  def cuAt(i:Int, j:Int):CUParam = {
    if ((i+j) % 2 == 0) pcuParam else pmuParam 
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
  pcuParam:PCUParam,
  pmuParam:PMUParam
) extends GridPattern {
  def cuAt(i:Int, j:Int):CUParam = {
    if (j % 2 == 0) pcuParam else pmuParam 
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
  pcuParam:PCUParam,
  pmuParam:PMUParam
) extends GridPattern {
  def cuAt(i:Int, j:Int):CUParam = {
    if (i % 2 == 0) pcuParam else pmuParam 
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
  pcuParam:PCUParam,
  pmuParam:PMUParam,
  scuParam:SCUParam
) extends GridPattern {
  def cuAt(i:Int, j:Int):CUParam = {
    if (i % 2 == 0) {
      if (j % 2 == 0) pcuParam
      else pmuParam
    } else scuParam
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
  pcuParam:PCUParam,
  pmuParam:PMUParam,
  scuParam:SCUParam
) extends GridPattern {
  def cuAt(i:Int, j:Int):CUParam = {
    if (i % 2 == 0) if (j % 2 == 0) pcuParam else pmuParam
    else if (j % 2 == 0) pmuParam else scuParam
  }
}
