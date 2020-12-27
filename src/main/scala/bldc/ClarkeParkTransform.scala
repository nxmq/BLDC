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

import chisel3._
import bldc.util.RotationCordic

class ClarkeParkTransform(val inputBits: Int, val phaseBits: Int, val outBits: Int) extends Module {
  val inscale: Long = Math.round(((1 << (inputBits-1)) - 1)/Math.sqrt(3))
  val io = IO(new Bundle {
    val iu: SInt = Input(SInt(inputBits.W))
    val iv: SInt = Input(SInt(inputBits.W))
    val phase: UInt = Input(UInt(phaseBits.W))
    val iq: SInt = Output(SInt(outBits.W))
    val id: SInt = Output(SInt(outBits.W))
    val ivalid: Bool = Input(Bool())
    val ovalid: Bool = Output(Bool())
  })
  val cordic: RotationCordic = Module(new RotationCordic(inputBits,outBits+2,outBits,phaseBits,outBits))
  val outscale: Long = Math.round((1.0*((1 << (outBits-1)) - 1))/cordic.getCordicGain)
  val ia: SInt = WireDefault(io.iu)
  val ib: SInt = WireDefault((inscale.S(inputBits.W) * (io.iu + (io.iv << 1)))(2*inputBits-2,inputBits-1).asSInt())
  cordic.io.ce := io.ivalid
  cordic.io.iaux := io.ivalid
  io.ovalid := cordic.io.oaux
  cordic.io.ix := ib
  cordic.io.iy := ia
  cordic.io.ph := io.phase
  io.iq := (cordic.io.oy * outscale.S(outBits.W))(2*outBits-1,outBits).asSInt()
  io.id := (cordic.io.ox * outscale.S(outBits.W))(2*outBits-1,outBits).asSInt()
}
