package spade

import spade.util._
import spade.codegen._
import spade.pass._
import spade.params._
import spade.node._

import prism._
import prism.util._

import scala.language.implicitConversions
import scala.collection.mutable.Map
import scala.collection.mutable.ListBuffer
import java.io._

trait Spade extends Compiler {

  override def toString = getClass().getSimpleName().replace("$", "")

  val configs = List(Config, SpadeConfig)

  var top:SpadeDesign = _ 
  lazy val spademeta:SpadeMetadata = top.spademeta

  override def reset = {
    super[Compiler].reset
    top = null
  }

  def handle(e:Exception):Unit = {
    //logger.close
    throw e
  }

  def load = SpadeConfig.loadDesign
  def save = SpadeConfig.saveDesign

  val designPath = s"${outDir}${File.separator}${name}.spade"

  lazy val topParam = MeshDesignParam()()
  def loadDesign = top = loadFromFile[SpadeDesign](designPath)

  def newDesign = {
    top = Factory.create(topParam)
  }

  def saveDesign:Unit = saveToFile(top, designPath)

  /* Analysis */
  //TODO: Area model

  /* Passes */
  //lazy val areaModel = new AreaModel()

  /* Codegen */
  //lazy val spadeNetworkCodegen = new SpadeNetworkCodegen()
  //lazy val spadeParamCodegen = new SpadeParamCodegen()

  /* Debug */
  //lazy val logger = new Logger() { override lazy val stream = newStream(s"spade.log") }
  //lazy val spadePrinter = new SpadePrinter()
  //lazy val plasticineVecDotPrinter = new PlasticineVectorDotPrinter()
  //lazy val plasticineScalDotPrinter = new PlasticineScalarDotPrinter()
  //lazy val plasticineCtrlDotPrinter = new PlasticineCtrlDotPrinter()

  override def initSession = {
    super.initSession
    import session._
    // Pass
    //addPass(areaModel)

    // Debug
    //addPass(spadePrinter)
    //addPass(plasticineVecDotPrinter)
    //addPass(plasticineScalDotPrinter)
    //addPass(plasticineCtrlDotPrinter)

    // Codegen
    //addPass(spadeNetworkCodegen)
    //addPass(spadeParamCodegen)
  }

  override def runSession = {
    super.runSession
    //if (SpadeConfig.openDot) {
      //plasticineVecDotPrinter.open
      //plasticineScalDotPrinter.open
      //plasticineCtrlDotPrinter.open
    //}
  }

}
