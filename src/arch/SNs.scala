package spade.arch
                          
import spade.network._
import spade.node._
import spade._

import pirc.enums._

import scala.language.implicitConversions
import scala.language.reflectiveCalls
import scala.reflect.runtime.universe._

class SN(numRows:Int=2, numCols:Int=2, numArgIns:Int=3, numArgOuts:Int=3, pattern:Pattern=MixAll) extends Spade {
  override def toString = s"SN${numRows}x${numCols}"
  override val topParam = new TopParam(
    numRows=numRows, 
    numCols=numCols, 
    numArgIns=numArgIns, 
    numArgOuts=numArgOuts, 
    pattern=pattern
  )
  config
}

abstract class SN_LD(numRows:Int=2, numCols:Int=2, numArgIns:Int=3, numArgOuts:Int=3, pattern:Pattern=MixAll) extends Spade(
) with PreLoadSpadeParam {
  override def toString = s"SN${numRows}x${numCols}_LD"

  override val topParam = new PreloadTopParam(
    numRows=numRows, 
    numCols=numCols, 
    numArgIns=numArgIns, 
    numArgOuts=numArgOuts, 
    pattern=pattern
  )

  override def pcuAt(i:Int, j:Int) = PreloadPatternComputeParam(numCtrs=12)

  override def mcuAt(i:Int, j:Int) = PreloadMemoryComputeParam(numCtrs=12, numRegs=23)

  override def scuAt(i:Int, j:Int) = PreloadScalarComputeParam()
}

object SN1x1 extends SN(numRows=1, numCols=1, numArgIns=3, numArgOuts=3, pattern=Checkerboard) 
object SN1x2 extends SN(numRows=1, numCols=2, numArgIns=3, numArgOuts=3, pattern=Checkerboard) 
object SN2x2 extends SN(numRows=2, numCols=2, numArgIns=3, numArgOuts=3, pattern=Checkerboard) 
object SN2x3 extends SN(numRows=2, numCols=3, numArgIns=3, numArgOuts=3, pattern=Checkerboard) 
object SN4x4 extends SN(numRows=4, numCols=4, numArgIns=3, numArgOuts=3, pattern=Checkerboard) 
object SN8x8 extends SN(numRows=8, numCols=8, numArgIns=3, numArgOuts=3, pattern=Checkerboard) 

object SN2x2Test extends Spade {
  override val topParam = new TopParam(numRows=2, numCols=2, numArgIns=3, numArgOuts=3)

  override def ctrlNetwork = new CtrlNetwork {
    channelWidth("pos"->List("left", "right")) = 0
  }

  override def vectorNetwork = new VectorNetwork {
    channelWidth("pos"->List("left", "right")) = 0
  }

  override def scalarNetwork = new ScalarNetwork {
    channelWidth("pos"->List("left", "right")) = 0
  }
  config
}


object SN8x8_LD extends SN_LD(numRows=8, numCols=8, numArgIns=12, numArgOuts=5, pattern=Checkerboard) {
  override def scalarNetwork = new ScalarNetwork() {
    // switch to switch channel width
    channelWidth("src"->"sb", "dst"->"sb") = 6
  }
  config
}

object SN16x8_LD extends SN_LD(numRows=16, numCols=8, numArgIns=12, numArgOuts=5, pattern=Checkerboard) {
  override def scalarNetwork = new ScalarNetwork() {
    // switch to switch channel width
    channelWidth("src"->"sb", "dst"->"sb") = 6
  }
  override def ctrlNetwork = new CtrlNetwork() {

    // switch to switch channel width
    channelWidth("src"->"sb", "dst"->"sb") = 12

    // switch to CU channel width
    channelWidth("pos"->"center", "src"->"sb", "dst"->List("pcu", "mu", "mcu")) = 4

    // CU to Switch channel width
    channelWidth("pos"->"center", "src"->List("pcu", "mu", "mcu"), "dst"->"sb") = 2
      
    // OCU to switch channel width
    channelWidth("pos"->"center", "src"->"ocu", "dst"->"sb") = 4

    // switch to OCU channel width
    channelWidth("pos"->"center", "src"->"sb", "dst"->"ocu") = 4
  }
  config
}

object SN16x13_LD extends SN_LD(numRows=16, numCols=13, numArgIns=15, numArgOuts=5, pattern=Checkerboard) {
  override def scalarNetwork = new ScalarNetwork() {
    // switch to switch channel width
    channelWidth("src"->"sb", "dst"->"sb") = 6
  }
  config
}

object SN16x12_LD_HH extends SN_LD(numRows=16, numCols=12, numArgIns=15, numArgOuts=5, pattern=HalfHalf) {
  override def scalarNetwork = new ScalarNetwork() {
    // switch to switch channel width
    channelWidth("src"->"sb", "dst"->"sb") = 6
  }
  config
}
