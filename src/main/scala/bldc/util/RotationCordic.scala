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

package bldc.util

import chisel3._
import chisel3.util._

class RotationCordic(val inputBits: Int, val workingBits: Int, val outputBits: Int, val phaseBits: Int, val stageCount: Int) extends Module {
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

  def getCordicGain: Double = {
    var gain = 1.0
    for(k <- 0 until stageCount) {
      var dgain = .0
      dgain = 1.0 + Math.pow(2.0, -2.0 * (k + 1))
      dgain = Math.sqrt(dgain)
      gain = gain * dgain
    }
    gain
  }

  def genAngleTable() : Vec[UInt] = {
    VecInit({(0 until stageCount).map { k =>
      var x = Math.atan2(1.0,Math.pow(2,k+1))
      x *= (4.0 * (1<<(phaseBits-2))) / (Math.PI * 2.0)
      Math.round(x).U(phaseBits.W)
    }})
  }


  val wx: Vec[SInt] = RegInit(VecInit(Seq.fill(stageCount + 1)(0.S(workingBits.W))))
  val wy: Vec[SInt] = RegInit(VecInit(Seq.fill(stageCount + 1)(0.S(workingBits.W))))
  val wph: Vec[UInt] = RegInit(VecInit(Seq.fill(stageCount + 1)(0.U(phaseBits.W))))
  val aux: UInt = RegInit(0.U((stageCount+1).W))

  val ex: SInt = WireDefault(0.S(workingBits.W))
  val ey: SInt = WireDefault(0.S(workingBits.W))

  val px: SInt = WireDefault(0.S(workingBits.W))
  val py: SInt = WireDefault(0.S(workingBits.W))

  val angleTable: Vec[UInt] = WireDefault(genAngleTable())
  io.ox := px(workingBits - 1,workingBits - outputBits).asSInt()
  io.oy := py(workingBits - 1,workingBits - outputBits).asSInt()
  io.oaux := aux(stageCount)

  when(io.ce) {
    ex := Cat(io.ix(inputBits - 1).asSInt(), io.ix, 0.S((workingBits - inputBits - 1).W)).asSInt()
    ey := Cat(io.iy(inputBits - 1).asSInt(), io.iy, 0.S((workingBits - inputBits - 1).W)).asSInt()
    px := wx(stageCount) + Cat(0.S(outputBits.W), wx(stageCount)(workingBits-outputBits).asSInt(), Fill(workingBits - outputBits - 1,!wx(stageCount)(workingBits - outputBits)).asSInt()).asSInt()
    py := wy(stageCount) + Cat(0.S(outputBits.W), wy(stageCount)(workingBits-outputBits).asSInt(), Fill(workingBits - outputBits - 1,!wy(stageCount)(workingBits - outputBits)).asSInt()).asSInt()
    aux := aux(stageCount-1,0) ## io.iaux

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
        }.elsewhen(wph(i)(phaseBits-1) === 1.U) {
          wx(i+1) := wx(i) + (wy(i)>>(1+i))
          wy(i+1) := wy(i) - (wx(i)>>(1+i)  )
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
  }.otherwise {
    aux := 0.U(stageCount.W)
  }
}
