package bldc.util

import chisel3._
import chisel3.util.HasBlackBoxInline
import firrtl.ir.Type

import scala.collection.mutable

/*
 * This is inspired by Angie Wang's excellent UIntLut2D
 * The main difference comes from the fact that Xilinx can infer BRAMs for ROMs.
 * It can do single and dual port BRAMs. Xilinx publishes guidelines for how to write
 * verilog/vhdl in a way that allows the tool to infer BRAMs.
 * The basic idea is that it should be written using a case statement and the output should be registered
 */

class SyncROM2R(val blackboxName: String, val table: Seq[BigInt], val widthOverride: Option[Int] = None)
  extends Module {
  val dataWidth = SyncROM2RBlackBox.dataWidth(table, widthOverride)

  val addrWidth = SyncROM2RBlackBox.addrWidth(table)

  val io = IO(new SyncROM2RIO(addrWidth=addrWidth, dataWidth=dataWidth))

  val rom = Module(new SyncROM2RBlackBox(blackboxName, table, widthOverride))

  rom.io.clock := clock
  rom.io.addrA := io.addrA
  rom.io.addrB := io.addrB
  io.dataA     := rom.io.dataA
  io.dataB     := rom.io.dataB
}

class SyncROM2RIO(val addrWidth: Int, val dataWidth: Int) extends Bundle {
  val addrA  = Input(UInt(addrWidth.W))
  val addrB  = Input(UInt(addrWidth.W))
  val dataA  = Output(UInt(dataWidth.W))
  val dataB  = Output(UInt(dataWidth.W))
}

class SyncROM2RBlackBox(blackboxName: String, table: Seq[BigInt], widthOverride: Option[Int] = None)
  extends BlackBox with HasBlackBoxInline {
  val dataWidth = SyncROM2RBlackBox.dataWidth(table, widthOverride)

  val addrWidth = SyncROM2RBlackBox.addrWidth(table)

  val io = IO(new SyncROM2RIO(addrWidth=addrWidth, dataWidth = dataWidth) with HasBlackBoxClock)

  override def desiredName: String = blackboxName

  def tableEntry2InitStr(value: BigInt, addr: BigInt): String = {
    s"mem[$addrWidth'b${addr.toString(2)}] = $dataWidth'h${value.toString(16)};"
  }
  val tableStrings = table.zipWithIndex.map { case (t, i) => tableEntry2InitStr(t, BigInt(i))}
  val tableString  = tableStrings.foldLeft("\n") { case (str, entry) => str + "      " + entry + "\n"}

  val verilog =
    s"""
       |module $name(
       |  input clock,
       |  input  [${(addrWidth - 1).max(0)}:0] addrA,
       |  input  [${(addrWidth - 1).max(0)}:0] addrB,
       |  output [${(dataWidth - 1).max(0)}:0] dataA,
       |  output [${(dataWidth - 1).max(0)}:0] dataB
       |);
       |  reg [${(dataWidth - 1).max(0)}:0] mem [${table.length-1}:0];
       |  assign dataA = mem[addrA];
       |  assign dataB = mem[addrB];
       |  initial begin$tableString
       |  end
       |endmodule
     """.stripMargin

  setInline(s"$name.v", verilog)

  SyncROM2RBlackBox.interpreterMap.update(name, (table, dataWidth))
}

object SyncROM2RBlackBox {
  def addrWidth(table: Seq[BigInt]): Int = {
    BigInt(table.length - 1).bitLength
  }
  def dataWidth(table: Seq[BigInt], widthOverride: Option[Int]): Int = {
    val w = widthOverride.getOrElse(table.map{_.bitLength}.max)
    require(w >= table.map{_.bitLength}.max, s"width of ${w} too small for table. Width of ${table.map{_.bitLength}.max} required.")
    w
  }
  private [util] val interpreterMap = mutable.Map[String, (Seq[BigInt], Int)]()
}

// implementation for firrtl interpreter
class SyncROM2RBlackBoxImplementation(val name: String, val table: Seq[BigInt], dataWidth: Int, default: BigInt = 0)
  extends firrtl_interpreter.BlackBoxImplementation {
  import firrtl_interpreter._

  var lastCycleAddrA: BigInt    = BigInt(0)
  var currentCycleAddrA: BigInt = BigInt(0)
  var lastCycleAddrB: BigInt    = BigInt(0)
  var currentCycleAddrB: BigInt = BigInt(0)
  override def cycle(): Unit = {
    println("cycle got called")
    lastCycleAddrA = currentCycleAddrA
    lastCycleAddrB = currentCycleAddrB
  }

  override def execute(inputValues: Seq[Concrete], tpe: Type, outputName: String): Concrete = {
    require(outputName == "dataA" || outputName == "dataB", s"Only outputs should be data ports, got $outputName")
    if(outputName == "dataA")
      currentCycleAddrA = inputValues.head.value
    else if(outputName == "dataB")
      currentCycleAddrB = inputValues.head.value
    println(s"execute got called on $outputName for addr $inputValues.head.value")
    val tableValue = if(outputName == "dataA") {
      if (lastCycleAddrA.toInt < table.length) {
        table(lastCycleAddrA.toInt)
      } else {
        default
      }
    } else if(outputName == "dataB") {
      if (lastCycleAddrB.toInt < table.length) {
        table(lastCycleAddrB.toInt)
      } else {
        default
      }
    } else {
      default
    }
    ConcreteUInt(tableValue, dataWidth, inputValues.head.poisoned)
  }

  override def outputDependencies(outputName: String): Seq[String] = {
    if(outputName == "dataA") {
      Seq("addrA")
    } else if (outputName == "dataB") {
      Seq("addrB")
    } else {
      Seq()
    }
  }
}

class SyncROM2RBlackBoxFactory extends firrtl_interpreter.BlackBoxFactory {
  override def createInstance(instanceName: String, blackBoxName: String) =
    SyncROM2RBlackBox.interpreterMap.get(blackBoxName).map {
      case (table, dataWidth) => new SyncROM2RBlackBoxImplementation(instanceName, table, dataWidth)
    }
}