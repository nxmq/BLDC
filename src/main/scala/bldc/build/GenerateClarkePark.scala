package bldc.build

import bldc.ClarkeParkTransform
import chisel3.stage.ChiselStage

object GenerateClarkePark extends App {
  (new ChiselStage).emitVerilog(new ClarkeParkTransform(12,11,14))
}
