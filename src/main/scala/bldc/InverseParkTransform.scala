//SPDX-License-Identifier: 0BSD
//Copyright (c) 2020 Nicolas Machado
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

import bldc.util.{RotationCordic, VectorCordic}
import chisel3._

class InverseParkTransform(val inputBits: Int, val phaseBits: Int, val outBits: Int)  extends Module {
  val io = IO(new Bundle {
    val vq: SInt = Input(SInt(inputBits.W))
    val vd: SInt = Input(SInt(inputBits.W))
    val motorPhase: UInt = Input(UInt(phaseBits.W))
    val modulationVoltage: UInt = Output(UInt(outBits.W))
    val modulationPhase: UInt = Output(UInt(outBits.W))
    val ivalid: Bool = Input(Bool())
    val ovalid: Bool = Output(Bool())
  })
  val rotCordic: RotationCordic = Module(new RotationCordic(inputBits,outBits+2,outBits,phaseBits,outBits))
  val vecCordic: VectorCordic = Module(new VectorCordic(outBits,outBits+2,outBits,outBits,outBits))

  val outscale: Long = Math.round((1.0*((1 << (outBits-1)) - 1))/rotCordic.getCordicGain)
  rotCordic.io.ce := io.ivalid
  rotCordic.io.iaux := io.ivalid
  vecCordic.io.iaux := rotCordic.io.oaux
  io.ovalid := vecCordic.io.oaux
  rotCordic.io.ix := io.vd
  rotCordic.io.iy := io.vq
  rotCordic.io.ph := io.motorPhase
  vecCordic.io.ce := rotCordic.io.oaux
  vecCordic.io.ix := (rotCordic.io.ox * outscale.S(outBits.W)) (2 * outBits - 1, outBits).asSInt()
  vecCordic.io.iy := (rotCordic.io.oy * outscale.S(outBits.W)) (2 * outBits - 1, outBits).asSInt()
  io.modulationVoltage := (vecCordic.io.omg.asUInt() * outscale.U(outBits.W)) (2 * outBits - 1, outBits)
  io.modulationPhase := ((vecCordic.io.oph + vecCordic.io.oph + vecCordic.io.oph) >> 2)

}
