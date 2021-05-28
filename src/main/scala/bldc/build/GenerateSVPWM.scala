package bldc.build

import bldc.SpaceVectorPWM
import chisel3.stage.{ChiselGeneratorAnnotation, ChiselStage}

object GenerateSVPWM extends App {
  (new ChiselStage).execute(Array("-X", "verilog","--target-dir", "genrtl"),Seq(ChiselGeneratorAnnotation(() => new SpaceVectorPWM())))
}
