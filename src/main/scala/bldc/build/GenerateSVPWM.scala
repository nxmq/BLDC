package bldc.build

import bldc.SpaceVectorPWM
import chisel3.stage.ChiselStage

object GenerateSVPWM extends App {
(new ChiselStage).emitVerilog(new SpaceVectorPWM())
}
