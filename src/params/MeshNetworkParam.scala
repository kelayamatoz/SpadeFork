package spade.params

import spade.node._

import prism.enums._
import prism.node._
import prism.collection.mutable.Table

import scala.reflect._

abstract class MeshNetworkParam[B<:BundleType:ClassTag] extends Parameter {
  val bct = implicitly[ClassTag[B]]
  val numRows:Int
  val numCols:Int
  val pattern:GridPattern
  val argFringeParam:ArgFringeParam
  val channelWidth:ChannelWidth
}

trait ChannelWidth extends Table[String, String, Int]
object ChannelWidth {
  def empty = new Table[String, String, Int] (
    values=Map(
      "pos"->List("left", "right","center","top","bottom"), 
      "src"->List("arg", "pcu", "ocu", "pmu", "mu", "scu", "mc", "sb"), 
      "dst"->List("arg", "pcu", "ocu", "pmu", "mu", "scu", "mc", "sb"), 
      "srcDir"->GridBundle.eightDirections, 
      "dstDir"->GridBundle.eightDirections
    ), 
    default=Some(0)
  ) with ChannelWidth
}

case class MeshControlNetworkParam(
  numRows:Int,
  numCols:Int,
  pattern:GridPattern,
  argFringeParam:ArgFringeParam,
  channelWidth:ChannelWidth=ChannelWidth.empty
) extends MeshNetworkParam[Bit] {
  // switch to switch channel width
  channelWidth("src"->"sb", "dst"->"sb") = 6

  // switch to CU channel width
  channelWidth("pos"->"center", "src"->"sb", "dst"->List("pcu", "mu", "pmu", "scu")) = 1

  // CU to Switch channel width
  channelWidth("pos"->"center", "src"->List("pcu", "mu", "pmu", "scu"), "dst"->"sb") = 2

  // DAG to switch channel width
  channelWidth("pos"->List("left", "right"), "src"->"scu", "dst"->"sb") = 1

  // switch to DAG channel width
  channelWidth("pos"->List("left", "right"), "src"->"sb", "dst"->"scu") = 1

  // switch to SAG channel width
  channelWidth("pos"->List("left", "right"), "src"->"sb", "dst"->"pcu") = 2

  // SAG to switch channel width
  channelWidth("pos"->List("left", "right"), "src"->"pcu", "dst"->"sb") = 2 

  // switch to MC channel width
  channelWidth("pos"->List("left", "right"), "src"->"sb", "dst"->"mc") = 1

  // MC to switch channel width
  channelWidth("pos"->List("left", "right"), "src"->"mc", "dst"->"sb") = 2

  // MC to DAG channel width
  channelWidth("pos"->List("left", "right"), "src"->"mc", "dst"->"scu") = 2
    
  // OCU to switch channel width
  channelWidth("pos"->"center", "src"->"ocu", "dst"->"sb") = 2
  // switch to OCU channel width
  channelWidth("pos"->"center", "src"->"sb", "dst"->"ocu") = 4

  // Top to switch channel width
  channelWidth("pos"->List("top", "bottom"), "src"->"arg", "dst"->"sb") = 1
  // switch to Top channel width
  channelWidth("pos"->List("top", "bottom"), "src"->"sb", "dst"->"arg") = 1
}

case class MeshScalarNetworkParam(
  numRows:Int,
  numCols:Int,
  pattern:GridPattern,
  argFringeParam:ArgFringeParam,
  channelWidth:ChannelWidth=ChannelWidth.empty
) extends MeshNetworkParam[Word] {
  // switch to switch channel width
  channelWidth("src"->"sb", "dst"->"sb") = 4

  // switch to PCU channel width
  channelWidth("pos"->"center", "src"->"sb", "dst"->List("pcu", "scu")) = 1//roundUp(pcuSins / 4.0) 

  // PCU to Switch channel width
  channelWidth("pos"->"center", "src"->List("pcu", "scu"), "dst"->"sb") = 1//roundUp(pcuSouts / 4.0)

  // switch to MCU channel width
  channelWidth("pos"->"center", "src"->"sb", "dst"->List("pmu")) = 1//roundUp(pmuSins / 4.0) 

  // MCU to Switch channel width
  channelWidth("pos"->"center", "src"->List("pmu"), "dst"->"sb") = 1//roundUp(pmuSouts / 4.0)
  
  // switch to DAG channel width
  channelWidth("pos"->List("left", "right"), "src"->"sb", "dst"->"scu") = 1//roundUp(ucuSins)

  // DAG to switch channel width
  channelWidth("pos"->List("left", "right"), "src"->"scu", "dst"->"sb") = 1//roundUp(ucuSouts) - 2

  // switch to SAG channel width
  channelWidth("pos"->List("left", "right"), "src"->"sb", "dst"->"pcu") = 4 

  // SAG to switch channel width
  channelWidth("pos"->List("left", "right"), "src"->"pcu", "dst"->"sb") = 2 

  // switch to MC channel width
  channelWidth("pos"->List("left", "right"), "src"->"sb", "dst"->"mc") = 3

  // MC to switch channel width
  channelWidth("pos"->List("left", "right"), "src"->"mc", "dst"->"sb") = 1
    
  // DAG to MC channel width
  channelWidth("pos"->List("left", "right"), "src"->"scu", "dst"->"mc") = 2
  
  //// switch to OCU channel width
  channelWidth("pos"->"center", "src"->"sb", "dst"->"ocu") = 5
  
  //// Top to switch channel width
  channelWidth("pos"->List("top", "bottom"), "src"->"arg", "dst"->"sb") = 1

  //// switch to Top channel width
  channelWidth("pos"->List("top", "bottom"), "src"->"sb", "dst"->"arg") = 1
}

case class MeshVectorNetworkParam(
  numRows:Int,
  numCols:Int,
  pattern:GridPattern,
  argFringeParam:ArgFringeParam,
  channelWidth:ChannelWidth=ChannelWidth.empty
) extends MeshNetworkParam[Vector] {
  // switch to switch channel width
  channelWidth("src"->"sb", "dst"->"sb") = 4

  // switch to PCU channel width
  channelWidth("pos"->"center", "src"->"sb", "dst"->List("pcu")) = 1//roundUp(pcuVins / 4.0)

  // PCU to Switch channel width
  channelWidth("pos"->"center", "src"->List("pcu"), "dst"->"sb") = 1//roundUp(pcuVouts / 4.0)

  // switch to MCU channel width
  channelWidth("pos"->"center", "src"->"sb", "dst"->List("pmu")) = 1//roundUp(pmuVins / 4.0) 

  // MCU to Switch channel width
  channelWidth("pos"->"center", "src"->List("pmu"), "dst"->"sb") = 1//roundUp(pmuVouts / 4.0)

  // switch to SAG channel width
  channelWidth("pos"->List("left", "right"), "src"->"sb", "dst"->"pcu") = 4 

  // SAG to switch channel width
  channelWidth("pos"->List("left", "right"), "src"->"pcu", "dst"->"sb") = 2 

  // switch to MC channel width
  channelWidth("pos"->List("left", "right"), "src"->"sb", "dst"->"mc") = 1
    
  // MC to switch channel width
  channelWidth("pos"->List("left", "right"), "src"->"mc", "dst"->"sb") = 1
}

