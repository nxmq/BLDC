package bldc.build

import bldc.Root
import chisel3.stage.ChiselStage

object GenerateRoot extends App {
  (new ChiselStage).emitVerilog(new Root())
}

