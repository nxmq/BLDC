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

import bldc.util.VectorCordic
import chisel3._

class InverseParkTransform(val inputBits: Int, val phaseBits: Int, val outBits: Int)  extends Module {
  val io = IO(new Bundle {
    val vq: SInt = Input(SInt(inputBits.W))
    val vd: SInt = Input(SInt(inputBits.W))
    val motorPhase: UInt = Input(UInt(phaseBits.W))
    val modulationVoltage: UInt = Output(UInt(outBits.W))
    val modulationPhase: UInt = Output(UInt(phaseBits.W))
    val ivalid: Bool = Input(Bool())
    val ovalid: Bool = Output(Bool())
  })
  val outVoltage: UInt = Reg(UInt(outBits.W))
  val outPhase: UInt = Reg(UInt(phaseBits.W))
  val vecCordic: VectorCordic = Module(new VectorCordic(outBits,outBits+2,outBits,outBits,outBits))
  val outscale: Long = Math.round((1.0*((1 << (outBits-1)) - 1))/vecCordic.getCordicGain)
  vecCordic.io.ce := io.ivalid
  vecCordic.io.iaux := io.ivalid
  io.ovalid := RegNext(vecCordic.io.oaux)
  vecCordic.io.ix := io.vd
  vecCordic.io.iy := io.vq
  when(vecCordic.io.oaux) {
    outPhase := (((vecCordic.io.oph + (vecCordic.io.oph << 1)) >> 2) + io.motorPhase)
    outVoltage := (vecCordic.io.omg.asUInt() * outscale.U(outBits.W)) (2 * outBits - 1, outBits)
  }
  io.modulationVoltage := outVoltage
  io.modulationPhase := outPhase
}
