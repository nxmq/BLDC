package bldc.build

import bldc.util.RotationCordic
import chisel3.stage.ChiselStage

object GenerateCordic extends App {
  (new ChiselStage).emitVerilog(new RotationCordic(13,15,13,11,16))
}
