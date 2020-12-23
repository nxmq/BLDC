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
package bldc.util

import chisel3._
import chisel3.util._

class CORDIC(inputBits: Int, workingBits: Int, outputBits: Int,phaseBits: Int, stageCount: Int) extends Module {
  var io = IO(new Bundle{
    val ce: Bool = Input(Bool())
    val ph: UInt = Input(UInt(phaseBits.W))
    val ix: SInt = Input(SInt(inputBits.W))
    val iy: SInt = Input(SInt(inputBits.W))
    val ox: SInt = Output(SInt(outputBits.W))
    val oy: SInt = Output(SInt(outputBits.W))
    val iaux: Bool = Input(Bool())
    val oaux: Bool = Output(Bool())
  })

  def genAngleTable() : Vec[UInt] = {
    VecInit({(0 to stageCount).map { k =>
      var x = Math.atan2(1.0,Math.pow(2,k+1))
      x *= (4.0 * (1<<(phaseBits-2))) / (Math.PI * 2.0)
      Math.round(x).U(phaseBits.W)
    }})
  }


  val wx: Vec[SInt] = RegInit(VecInit(Seq.fill(stageCount+1)(0.S(workingBits.W))))
  val wy: Vec[SInt] = RegInit(VecInit(Seq.fill(stageCount+1)(0.S(workingBits.W))))
  val wph: Vec[UInt] = RegInit(VecInit(Seq.fill(stageCount+1)(0.U(phaseBits.W))))
  val ax: UInt = RegInit(0.U((stageCount+1).W))

  val ex: SInt = WireDefault(Cat(io.ix(inputBits-1), io.ix, 0.S((workingBits-inputBits-1))).asSInt())
  val ey: SInt = WireDefault(Cat(io.iy(inputBits-1), io.iy, 0.S((workingBits-inputBits-1))).asSInt())

  val px: SInt = WireDefault(wx(stageCount) + Cat(0.S(outputBits),wx(stageCount)(workingBits-outputBits),Fill(workingBits-outputBits-1,!wx(stageCount)(workingBits-outputBits))).asSInt())
  val py: SInt = WireDefault(wy(stageCount) + Cat(0.S(outputBits),wy(stageCount)(workingBits-outputBits),Fill(workingBits-outputBits-1,!wy(stageCount)(workingBits-outputBits))).asSInt())

  val ox: SInt = RegNext(px(workingBits-1,workingBits-outputBits).asSInt(),0.S(outputBits).asSInt())
  val oy: SInt = RegNext(px(workingBits-1,workingBits-outputBits).asSInt(),0.S(outputBits).asSInt())
  val oaux: Bool = RegNext(ax(stageCount),0.B)
  val angleTable: Vec[UInt] = WireDefault(genAngleTable())
  io.ox := ox
  io.oy := oy
  io.oaux := oaux

  when(io.ce) {
    for(i <- 0 until stageCount) {
      if(i >=workingBits) {
        wx(i+1) := wx(i)
        wy(i+1) := wy(i)
        wph(i+1) := wph(i)
      } else {
        when(angleTable(i) === 0.U) {
          wx(i+1) := wx(i)
          wy(i+1) := wy(i)
          wph(i+1) := wph(i)
        }.elsewhen(wph(i)(phaseBits-1)) {
          wx(i+1) := wx(i) + (wy(i)>>(1+i))
          wy(i+1) := wy(i) - (wx(i)>>(1+i))
          wph(i+1) := wph(i) + angleTable(i)
        }.otherwise {
          wx(i+1) := wx(i) - (wy(i)>>(1+i))
          wy(i+1) := wy(i) + (wx(i)>>(1+i))
          wph(i+1) := wph(i) - angleTable(i)
        }
      }
    }

    switch(io.ph(phaseBits-1,phaseBits-3)){
      is(0.U) {
        wx(0) := ex
        wy(0) := ey
        wph(0) := io.ph
      }
      is(1.U) {
        wx(0) := -ey
        wy(0) := ex
        wph(0) := io.ph - (1 << (phaseBits-2)).U
      }
      is(2.U) {
        wx(0) := -ey
        wy(0) := ex
        wph(0) := io.ph - (1 << (phaseBits-2)).U
      }
      is(3.U) {
        wx(0) := -ex
        wy(0) := -ey
        wph(0) := io.ph - (2 << (phaseBits-2)).U
      }
      is(4.U) {
        wx(0) := -ex
        wy(0) := -ey
        wph(0) := io.ph - (2 << (phaseBits-2)).U
      }
      is(5.U) {
        wx(0) := ey
        wy(0) := -ex
        wph(0) := io.ph - (3 << (phaseBits-2)).U
      }
      is(6.U) {
        wx(0) := ey
        wy(0) := -ex
        wph(0) := io.ph - (3 << (phaseBits-2)).U
      }
      is(7.U) {
        wx(0) := ex
        wy(0) := ey
        wph(0) := io.ph
      }
    }
  }
}
