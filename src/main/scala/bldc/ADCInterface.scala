//SPDX-License-Identifier: 0BSD
//Copyright (c) 2020-2021 Nicolas Machado
//
//Permission to use, copy, modify, and/or distribute this software for any
//purpose with or without fee is hereby granted.
//
//THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES WITH
//REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF MERCHANTABILITY
//AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY SPECIAL, DIRECT,
//INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES WHATSOEVER RESULTING FROM
//LOSS OF USE, DATA OR PROFITS, WHETHER IN AN ACTION OF CONTRACT, NEGLIGENCE OR
//OTHER TORTIOUS ACTION, ARISING OUT OF OR IN CONNECTION WITH THE USE OR
//PERFORMANCE OF THIS SOFTWARE.

package bldc

import chisel3._
import chisel3.util.log2Ceil


class ADCInterface(val currentBits: Int = 10) extends Module {
  val adcWaitTimeNS : Int = 400
  val waitClocks : Long = Math.round(Math.ceil((adcWaitTimeNS*Math.pow(10,-9)) * (ConfigData.systemClockFreqMHZ * Math.pow(10,6))))
  var io = IO(new Bundle {
    val adcCNVST: Bool = Output(Bool())
    val adcSck: Bool = Output(Bool())
    val adcDoutA: Bool = Input(Bool())
    val adcDoutB: Bool = Input(Bool())
    val phaseCurrentU: SInt = Output(SInt(currentBits.W))
    val phaseCurrentV: SInt = Output(SInt(currentBits.W))
    val triggerSample: Bool = Input(Bool())
    val outputValid: Bool = Output(Bool())
  })
  val outputValid : Bool = RegInit(false.B)
  val adcCNVST : Bool = RegInit(true.B)
  val waitCounter : UInt = Reg(UInt(log2Ceil(waitClocks+currentBits).W))
  val adcDataA : UInt = RegInit(0.U(currentBits.W))
  val adcDataB : UInt = RegInit(0.U(currentBits.W))
  val phaseCurrentU : SInt = RegInit(0.S(currentBits.W))
  val phaseCurrentV : SInt = RegInit(0.S(currentBits.W))
  val adcClkReg : Bool = RegInit(true.B)
  adcClkReg := !adcClkReg
  io.adcSck := adcClkReg
  io.outputValid := outputValid
  io.adcCNVST := adcCNVST
  io.phaseCurrentU := phaseCurrentU
  io.phaseCurrentV := phaseCurrentV
  when(io.triggerSample & outputValid) {
    outputValid := false.B
    io.adcCNVST := false.B
    waitCounter := 0.U
  }
  when(!adcCNVST) {
    waitCounter := waitCounter + 1.U
  }.elsewhen(!adcCNVST & waitCounter === waitClocks.U) {
    io.adcCNVST := true.B
  }
  when(waitCounter < (waitClocks + currentBits).U) {
    adcDataA := adcDataA(currentBits-2,0) ## io.adcDoutA
    adcDataB := adcDataB(currentBits-2,0) ## io.adcDoutB
    waitCounter := waitCounter + 1.U
  }
  when(waitCounter === (waitClocks + currentBits).U) {
    outputValid := true.B
    phaseCurrentU := adcDataA.asSInt()
    phaseCurrentV := adcDataB.asSInt()
  }
}
