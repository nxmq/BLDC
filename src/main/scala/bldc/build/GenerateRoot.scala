package bldc.build

import bldc.Root
import chisel3.stage.{ChiselGeneratorAnnotation, ChiselStage}

object GenerateRoot extends App {
  (new ChiselStage).execute(Array("-X", "verilog","--target-dir", "genrtl"),Seq(ChiselGeneratorAnnotation(() => new Root())))
}

