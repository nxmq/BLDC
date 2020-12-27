package bldc.bbox.ice40

import chisel3._
import chisel3.experimental.Analog

class SB_IO extends BlackBox(Map("PIN_TYPE" -> "b000000",
                                 "PULLUP" ->"b0",
                                 "NEG_TRIGGER" -> "b0",
                                 "IO_STANDARD" ->"SB_LVCMOS")) {
  var io = IO(new Bundle {
    val PACKAGE_PIN = IO(Analog(1.W))
    val LATCH_INPUT_VALUE = Input(Bool())
    val CLOCK_ENABLE = Input(Bool())
    val INPUT_CLK = Input(Bool())
    val OUTPUT_CLK = Input(Bool())
    val OUTPUT_ENABLE = Input(Bool())
    val D_OUT_0 = Input(Bool())
    val D_OUT_1 = Input(Bool())
    val D_IN_0 = Output(Bool())
    val D_IN_1 = Output(Bool())
  })
}
